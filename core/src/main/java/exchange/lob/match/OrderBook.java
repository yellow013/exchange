package exchange.lob.match;

import exchange.lob.api.sbe.ExchangeStateEncoder;
import exchange.lob.domain.OrderStatus;
import exchange.lob.domain.OrderType;
import exchange.lob.domain.RejectionReason;
import exchange.lob.domain.Side;
import exchange.lob.events.trading.OrderBookEvents;
import exchange.lob.match.execution.*;
import exchange.lob.product.Product;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.collections.MutableLong;
import org.agrona.collections.Object2ObjectHashMap;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

import static exchange.lob.domain.OrderType.LMT;
import static exchange.lob.domain.OrderType.MKT;
import static exchange.lob.domain.Side.ASK;
import static exchange.lob.domain.Side.BID;
import static exchange.lob.match.util.Sides.other;
import static it.unimi.dsi.fastutil.longs.LongComparators.NATURAL_COMPARATOR;
import static it.unimi.dsi.fastutil.longs.LongComparators.OPPOSITE_COMPARATOR;
import static java.util.Comparator.comparing;

/**
 * L3 Limit Order Book implementation.
 * <p>
 * Bids and asks are kept sorted in reverse natural and natural orders of their constituent order's prices and then
 * by their arrival sequence to the orderbook.
 */

public class OrderBook
{
    static final Comparator<Order> BID_ORDER_COMPARATOR = comparing(Order::getPrice, OPPOSITE_COMPARATOR).thenComparing(Order::getOrderId);
    static final Comparator<Order> ASK_ORDER_COMPARATOR = comparing(Order::getPrice, NATURAL_COMPARATOR).thenComparing(Order::getOrderId);

    private final long productId;
    // TODO: this is not the best data structure in terms of data locality.
    final ObjectRBTreeSet<Order> bids;
    final ObjectRBTreeSet<Order> asks;
    final Long2ObjectHashMap<Order> orders;
    final Object2ObjectHashMap<String, Order> ordersByClientOrderId;
    final LiquidityCache liquidityCache;
    final MutableLong currentOrderId;
    final MutableLong currentExecutionId;

    private final OrderBookEvents orderBookEvents;

    OrderBook(
        final long productId,
        final ObjectRBTreeSet<Order> bids,
        final ObjectRBTreeSet<Order> asks,
        final Long2ObjectHashMap<Order> orders,
        final Object2ObjectHashMap<String, Order> ordersByClientOrderId,
        final LiquidityCache liquidityCache,
        final MutableLong currentOrderId,
        final MutableLong currentExecutionId,
        final OrderBookEvents orderBookEvents
    )
    {
        this.productId = productId;
        this.bids = bids;
        this.asks = asks;
        this.orders = orders;
        this.ordersByClientOrderId = ordersByClientOrderId;
        this.liquidityCache = liquidityCache;
        this.currentOrderId = currentOrderId;
        this.currentExecutionId = currentExecutionId;
        this.orderBookEvents = orderBookEvents;
    }

    public static OrderBook.OrderBookBuilder ofProduct(long productId)
    {
        return OrderBook.builder()
            .productId(productId)
            .bids(new ObjectRBTreeSet<>(BID_ORDER_COMPARATOR))
            .asks(new ObjectRBTreeSet<>(ASK_ORDER_COMPARATOR))
            .orders(new Long2ObjectHashMap<>())
            .ordersByClientOrderId(new Object2ObjectHashMap<>())
            .liquidityCache(LiquidityCache.create());
    }

    public static OrderBookBuilder builder()
    {
        return new OrderBookBuilder();
    }

    public void place(
        final long correlationId,
        final String clientOrderId,
        final Product product,
        final long userId,
        final OrderType orderType,
        final Side side,
        final long price,
        final long amount,
        final long reservedBalance,
        final ExecutionSettler executionSettler
    )
    {
        switch (side)
        {
            case BID -> bid(correlationId, clientOrderId, product, userId, orderType, price, amount, reservedBalance, executionSettler);
            case ASK -> ask(correlationId, clientOrderId, product, userId, orderType, price, amount, reservedBalance, executionSettler);
            case NULL_VAL ->
            {
            }
        }
    }

    private void bid(
        final long correlationId,
        final String clientOrderId,
        final Product product,
        final long userId,
        final OrderType orderType,
        final long price,
        final long amount,
        final long reservedBalance,
        final ExecutionSettler executionSettler
    )
    {
        execute(
            correlationId,
            clientOrderId,
            product,
            asks,
            bids,
            BID,
            OPPOSITE_COMPARATOR,
            userId,
            orderType,
            price,
            amount,
            reservedBalance,
            executionSettler
        );
    }

    private void ask(
        final long correlationId,
        final String clientOrderId,
        final Product product,
        final long userId,
        final OrderType orderType,
        final long price,
        final long amount,
        final long reservedBalance,
        final ExecutionSettler executionSettler
    )
    {
        execute(
            correlationId,
            clientOrderId,
            product,
            bids,
            asks,
            ASK,
            NATURAL_COMPARATOR,
            userId,
            orderType,
            price,
            amount,
            reservedBalance,
            executionSettler
        );
    }

    private void execute(
        final long correlationId,
        final String clientOrderId,
        final Product product,
        final ObjectRBTreeSet<Order> makerOrders,
        final ObjectRBTreeSet<Order> takerOrders,
        final Side side,
        final LongComparator priceComparator,
        final long userId,
        final OrderType orderType,
        final long price,
        final long amount,
        final long reservedBalance,
        final ExecutionSettler executionSettler // can be instance variable
    )
    {
        final boolean sufficientLiquidity = validateLiquidity(
            correlationId,
            clientOrderId,
            product,
            side,
            userId,
            orderType,
            amount,
            reservedBalance,
            executionSettler
        );

        if (!sufficientLiquidity)
        {
            return;
        }

        final boolean sufficientBalance = validateEquity(
            product,
            makerOrders,
            side,
            userId,
            orderType,
            amount,
            reservedBalance,
            executionSettler
        );

        if (!sufficientBalance)
        {
            return;
        }

        long toFill = amount;
        long filledAmount = 0L;
        final Iterator<Order> makerOrderIterator = makerOrders.iterator();
        while (toFill > 0 && makerOrderIterator.hasNext())
        {
            Order makerOrder = makerOrderIterator.next();
            if (orderType == LMT && priceComparator.compare(price, makerOrder.getPrice()) > 0)
            {
                break;
            }

            long depleteBy = Math.min(makerOrder.getAmount(), toFill);
            toFill -= depleteBy;
            filledAmount += depleteBy;

            makerOrder.depleteBy(depleteBy);

            if (makerOrder.getAmount() == 0)
            {
                makerOrders.remove(makerOrder);
                orders.remove(makerOrder.getOrderId());
                ordersByClientOrderId.remove(makerOrder.getClientOrderId());
            }

            liquidityCache.onLiquidityDecrease(other(side), depleteBy);

            final OrderStatus makerOrderStatus = inferOrderStatus(makerOrder.getAmount());
            final OrderStatus takerOrderStatus = inferOrderStatus(toFill);
            makerOrder.setStatus(makerOrderStatus);

            currentExecutionId.increment();

            orderBookEvents.onTrade(
                correlationId,
                makerOrder.getClientOrderId(),
                clientOrderId,
                currentExecutionId.get(),
                makerOrder.getUserId(),
                userId,
                makerOrderStatus,
                takerOrderStatus,
                product,
                exchange.lob.domain.Side.get(side.value()),
                makerOrder.getPrice(),
                product.getCounterAsset().getScale(),
                depleteBy,
                product.getBaseAsset().getScale()
            );

            final TradeExecutionReport executionReport = new TradeExecutionReport(
                currentExecutionId.get(),
                makerOrder,
                orderType,
                side,
                depleteBy,
                userId,
                product,
                reservedBalance
            );

            executionSettler.settle(executionReport);
        }

        final OrderStatus orderStatus = inferOrderStatus(amount, toFill);
        currentOrderId.increment();
        if (toFill > 0)
        {
            final Order order = new Order(
                currentOrderId.get(),
                clientOrderId,
                userId,
                productId,
                orderStatus,
                orderType,
                exchange.lob.domain.Side.get(side.value()),
                price,
                toFill
            );

            liquidityCache.onLiquidityIncrease(side, toFill);
            orders.put(currentOrderId.get(), order);
            ordersByClientOrderId.put(clientOrderId, order);
            takerOrders.add(order);

            currentExecutionId.increment();

            orderBookEvents.onOrderPlaced(
                correlationId,
                product.getSymbol(),
                clientOrderId,
                currentExecutionId.get(),
                userId,
                currentOrderId.get(),
                orderStatus,
                exchange.lob.domain.Side.get(side.value()),
                price,
                product.getCounterAsset().getScale(),
                toFill,
                product.getBaseAsset().getScale()
            );

            final PlacementExecutionReport executionReport = new PlacementExecutionReport(
                currentExecutionId.get(),
                side,
                price,
                filledAmount,
                product
            );

            executionSettler.settle(executionReport);
        }
    }

    private OrderStatus inferOrderStatus(final long unfilled)
    {
        return unfilled == 0 ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
    }

    private OrderStatus inferOrderStatus(final long amount, final long toFill)
    {
        if (toFill == amount)
        {
            return OrderStatus.NEW;
        }
        else if (toFill == 0)
        {
            return OrderStatus.FILLED;
        }
        return OrderStatus.PARTIALLY_FILLED;
    }

    private boolean validateEquity(
        final Product product,
        final ObjectRBTreeSet<Order> makerOrders,
        final Side side,
        final long userId,
        final OrderType orderType,
        final long amount,
        final long reservedBalance,
        final ExecutionSettler executionSettler
    )
    {
        if (orderType == MKT && !canAfford(product, reservedBalance, amount, makerOrders.iterator()))
        {
            currentExecutionId.increment();

            final RejectionExecutionReport executionReport = new RejectionExecutionReport(
                currentExecutionId.get(),
                userId,
                product.getMakerAssetId(side),
                reservedBalance,
                RejectionReason.INSUFFICIENT_BALANCE,
                product
            );

            executionSettler.settle(executionReport);

            return false;
        }
        return true;
    }

    private boolean validateLiquidity(
        final long correlationId,
        final String clientOrderId,
        final Product product,
        final Side side,
        final long userId,
        final OrderType orderType,
        final long amount,
        final long reservedBalance,
        final ExecutionSettler executionSettler
    )
    {
        if (orderType == MKT && liquidityCache.getAvailableLiquidity(other(side)) < amount)
        {
            currentExecutionId.increment();
            final RejectionExecutionReport executionReport = new RejectionExecutionReport(
                currentExecutionId.get(),
                userId,
                product.getMakerAssetId(side),
                reservedBalance,
                RejectionReason.INSUFFICIENT_LIQUIDITY,
                product
            );

            executionSettler.settle(executionReport);

            orderBookEvents.onOrderRejected(
                correlationId,
                clientOrderId,
                currentExecutionId.get(),
                product.getSymbol(),
                userId,
                exchange.lob.domain.Side.get(side.value()),
                exchange.lob.domain.RejectionReason.INSUFFICIENT_LIQUIDITY
            );

            return false;
        }
        return true;
    }

    private boolean canAfford(Product product, long reservedBalance, long amount, Iterator<Order> makerOrders)
    {
        // redoing iterations. better way?
        long expenditure = 0L;
        while (makerOrders.hasNext() && amount > 0L)
        {
            Order order = makerOrders.next();
            long depleteBy = Math.min(amount, order.getAmount());
            amount -= depleteBy;
            expenditure += product.make(other(order.getSide()), order.getPrice(), depleteBy);
            if (expenditure > reservedBalance)
            {
                return false;
            }
        }
        return true;
    }

    public void cancel(
        final long correlationId,
        final String clientOrderId,
        final Product product,
        final long userId,
        final long amount,
        final ExecutionSettler executionSettler
    )
    {
        final Order order = ordersByClientOrderId.get(clientOrderId);
        if (order == null || amount > order.getAmount())
        {
            orderBookEvents.onCancellationRejected(
                correlationId,
                clientOrderId,
                userId,
                product.getSymbol(),
                exchange.lob.domain.RejectionReason.UNKNOWN_ORDER
            );
            return;
        }

        final Side side = order.getSide();
        liquidityCache.onLiquidityDecrease(side, amount);
        final boolean fullCancel = amount == order.getAmount();
        final OrderStatus orderStatus = fullCancel ? OrderStatus.CANCELLED : OrderStatus.NEW;

        if (fullCancel)
        {
            (side == BID ? bids : asks).remove(order);
            orders.remove(order.getOrderId());
        }
        else
        {
            order.depleteBy(amount);
        }

        currentExecutionId.increment();

        executionSettler.settle(new CancellationExecutionReport(
            currentExecutionId.get(),
            userId,
            product.getCancellationAssetId(side),
            order.getSide(),
            order.getPrice(),
            product.cancel(side, order.getPrice(), amount),
            product
        ));

        orderBookEvents.onOrderCancelled(
            correlationId,
            order.getClientOrderId(),
            currentExecutionId.get(),
            orderStatus,
            userId,
            product.getSymbol(),
            exchange.lob.domain.Side.get(side.value()),
            order.getPrice(),
            product.getCounterAsset().getScale(),
            amount,
            product.getBaseAsset().getScale()
        );
    }

    public static void encodeOrderBook(final ExchangeStateEncoder.OrderBooksEncoder orderBooksEncoder, final long productId, final OrderBook orderBook)
    {
        orderBooksEncoder.next()
            .productId(productId)
            .currentOrderId(orderBook.currentOrderId.get())
            .currentExecutionId(orderBook.currentExecutionId.get());

        final ExchangeStateEncoder.OrderBooksEncoder.BidsEncoder bidsEncoder = orderBooksEncoder.bidsCount(orderBook.bids.size());

        orderBook.bids.forEach(order -> bidsEncoder.next()
            .clientOrderId(order.getClientOrderId())
            .orderId(order.getOrderId())
            .productId(order.getProductId())
            .userId(order.getUserId())
            .orderStatus(exchange.lob.api.sbe.OrderStatus.get(order.getOrderStatus().value()))
            .orderType(exchange.lob.api.sbe.OrderType.get(order.getOrderType().value()))
            .side(exchange.lob.api.sbe.Side.get(order.getSide().value()))
            .price(order.getPrice())
            .amount(order.getAmount()));

        final ExchangeStateEncoder.OrderBooksEncoder.AsksEncoder asksEncoder = orderBooksEncoder.asksCount(orderBook.asks.size());

        orderBook.asks.forEach(order -> asksEncoder.next()
            .clientOrderId(order.getClientOrderId())
            .orderId(order.getOrderId())
            .productId(order.getProductId())
            .userId(order.getUserId())
            .orderStatus(exchange.lob.api.sbe.OrderStatus.get(order.getOrderStatus().value()))
            .orderType(exchange.lob.api.sbe.OrderType.get(order.getOrderType().value()))
            .side(exchange.lob.api.sbe.Side.get(order.getSide().value()))
            .price(order.getPrice())
            .amount(order.getAmount()));
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final OrderBook orderBook = (OrderBook)o;
        return productId == orderBook.productId && Objects.equals(bids, orderBook.bids) && Objects.equals(asks, orderBook.asks) && Objects.equals(
            orders,
            orderBook.orders
        ) && Objects.equals(ordersByClientOrderId, orderBook.ordersByClientOrderId) && Objects.equals(
            liquidityCache,
            orderBook.liquidityCache
        ) && Objects.equals(currentOrderId, orderBook.currentOrderId) && Objects.equals(
            currentExecutionId,
            orderBook.currentExecutionId
        );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(productId, bids, asks, orders, ordersByClientOrderId, liquidityCache, currentOrderId, currentExecutionId, orderBookEvents);
    }

    @Override
    public String toString()
    {
        return "OrderBook{" +
            "productId=" + productId +
            ", bids=" + bids +
            ", asks=" + asks +
            ", orders=" + orders +
            ", ordersByClientOrderId=" + ordersByClientOrderId +
            ", liquidityCache=" + liquidityCache +
            ", currentOrderId=" + currentOrderId +
            ", currentExecutionId=" + currentExecutionId +
            ", orderBookEvents=" + orderBookEvents +
            '}';
    }

    public static class OrderBookBuilder
    {
        private long productId;
        private ObjectRBTreeSet<Order> bids;
        private ObjectRBTreeSet<Order> asks;
        private Long2ObjectHashMap<Order> orders;
        private Object2ObjectHashMap<String, Order> ordersByClientOrderId;
        private LiquidityCache liquidityCache;
        private MutableLong currentOrderId;
        private MutableLong currentExecutionId;
        private OrderBookEvents orderBookEvents;

        OrderBookBuilder()
        {
        }

        public OrderBookBuilder productId(long productId)
        {
            this.productId = productId;
            return this;
        }

        public OrderBookBuilder bids(ObjectRBTreeSet<Order> bids)
        {
            this.bids = bids;
            return this;
        }

        public OrderBookBuilder asks(ObjectRBTreeSet<Order> asks)
        {
            this.asks = asks;
            return this;
        }

        public OrderBookBuilder orders(Long2ObjectHashMap<Order> orders)
        {
            this.orders = orders;
            return this;
        }

        public OrderBookBuilder ordersByClientOrderId(Object2ObjectHashMap<String, Order> ordersByClientOrderId)
        {
            this.ordersByClientOrderId = ordersByClientOrderId;
            return this;
        }

        public OrderBookBuilder liquidityCache(LiquidityCache liquidityCache)
        {
            this.liquidityCache = liquidityCache;
            return this;
        }

        public OrderBookBuilder currentOrderId(MutableLong currentOrderId)
        {
            this.currentOrderId = currentOrderId;
            return this;
        }

        public OrderBookBuilder currentExecutionId(MutableLong currentExecutionId)
        {
            this.currentExecutionId = currentExecutionId;
            return this;
        }

        public OrderBookBuilder orderBookEvents(OrderBookEvents orderBookEvents)
        {
            this.orderBookEvents = orderBookEvents;
            return this;
        }

        public OrderBook build()
        {
            return new OrderBook(productId, bids, asks, orders, ordersByClientOrderId, liquidityCache, currentOrderId, currentExecutionId, orderBookEvents);
        }

        @Override
        public String toString()
        {
            return "OrderBookBuilder{" +
                "productId=" + productId +
                ", bids=" + bids +
                ", asks=" + asks +
                ", orders=" + orders +
                ", ordersByClientOrderId=" + ordersByClientOrderId +
                ", liquidityCache=" + liquidityCache +
                ", currentOrderId=" + currentOrderId +
                ", currentExecutionId=" + currentExecutionId +
                ", orderBookEvents=" + orderBookEvents +
                '}';
        }
    }
}