package exchange.lob.match;

import exchange.lob.api.sbe.ExchangeStateDecoder;
import exchange.lob.api.sbe.ExchangeStateEncoder;
import exchange.lob.domain.OrderType;
import exchange.lob.domain.RejectionReason;
import exchange.lob.domain.Side;
import exchange.lob.events.trading.OrderBookEvents;
import exchange.lob.match.execution.ExecutionSettler;
import exchange.lob.match.execution.RejectionExecutionReport;
import exchange.lob.node.Stateful;
import exchange.lob.product.Product;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.collections.MutableLong;
import org.agrona.collections.Object2ObjectHashMap;
import org.agrona.collections.ObjectHashSet;

import java.util.Collection;
import java.util.Objects;

import static exchange.lob.match.OrderBook.ASK_ORDER_COMPARATOR;
import static exchange.lob.match.OrderBook.BID_ORDER_COMPARATOR;


public class MatchingEngine
{

    private static final long MISSING_VALUE = -1L;

    private final OrderBookEvents orderBookEvents;
    final Long2ObjectHashMap<OrderBook> orderBooks;
    final MutableLong executionId;
    final MutableLong orderId;
    final Long2ObjectHashMap<ObjectHashSet<String>> clientOrderIdsByUserId;

    public static final Codec CODEC = new Codec();

    MatchingEngine(
        final OrderBookEvents orderBookEvents,
        final Long2ObjectHashMap<OrderBook> orderBooks,
        final MutableLong executionId,
        final MutableLong orderId,
        final Long2ObjectHashMap<ObjectHashSet<String>> clientOrderIdsByUserId
    )
    {
        this.orderBookEvents = orderBookEvents;
        this.orderBooks = orderBooks;
        this.executionId = executionId;
        this.orderId = orderId;
        this.clientOrderIdsByUserId = clientOrderIdsByUserId;
    }

    public static MatchingEngine withOrderBookEvents(final OrderBookEvents orderBookEvents)
    {
        return MatchingEngine.builder()
            .orderBookEvents(orderBookEvents)
            .orderBooks(new Long2ObjectHashMap<>())
            .executionId(new MutableLong(MISSING_VALUE))
            .orderId(new MutableLong(MISSING_VALUE))
            .clientOrderIdsByUserId(new Long2ObjectHashMap<>())
            .build();
    }

    public static MatchingEngineBuilder builder()
    {
        return new MatchingEngineBuilder();
    }

    public long newExecutionId()
    {
        return executionId.incrementAndGet();
    }

    public void onAddProduct(long productId)
    {
        orderBooks.put(productId, OrderBook.ofProduct(productId)
            .currentExecutionId(executionId)
            .currentOrderId(orderId)
            .orderBookEvents(orderBookEvents)
            .build());
    }

    public void handleOrderPlacement(
        final long correlationId,
        final String clientOrderId,
        final long userId,
        final Product product,
        final OrderType orderType,
        final Side side,
        final long price,
        final long amount,
        final long reservedBalance,
        final ExecutionSettler executionSettler
    )
    {
        ObjectHashSet<String> clientOrderIds = clientOrderIdsByUserId.get(userId);
        if (clientOrderIds == null)
        {
            clientOrderIds = new ObjectHashSet<>();
            clientOrderIdsByUserId.put(userId, clientOrderIds);
        }

        if (clientOrderIds.contains(clientOrderId))
        {
            rejectDuplicateOrderId(correlationId, clientOrderId, userId, product, side, reservedBalance, executionSettler);
            return;
        }

        clientOrderIds.add(clientOrderId);

        orderBooks
            .get(product.getProductId())
            .place(
                correlationId,
                clientOrderId,
                product,
                userId,
                orderType,
                side,
                price,
                amount,
                reservedBalance,
                executionSettler
            );
    }

    private void rejectDuplicateOrderId(
        final long correlationId,
        final String clientOrderId,
        final long userId,
        final Product product,
        final Side side,
        final long reservedBalance,
        final ExecutionSettler executionSettler
    )
    {
        final long executionId = this.executionId.incrementAndGet();

        orderBookEvents.onOrderRejected(
            correlationId,
            clientOrderId,
            executionId,
            product.getSymbol(),
            userId,
            exchange.lob.domain.Side.get(side.value()),
            exchange.lob.domain.RejectionReason.DUPLICATE_ORDER
        );

        final RejectionExecutionReport executionReport = new RejectionExecutionReport(
            executionId,
            userId,
            product.getMakerAssetId(side),
            reservedBalance,
            RejectionReason.DUPLICATE_ORDER,
            product
        );

        executionSettler.settle(executionReport);
    }

    public void handleOrderCancellation(
        final long correlationId,
        final String clientOrderId,
        final Product product,
        final long userId,
        final long amount,
        final ExecutionSettler executionSettler
    )
    {
        orderBooks
            .get(product.getProductId())
            .cancel(correlationId, clientOrderId, product, userId, amount, executionSettler);
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
        final MatchingEngine that = (MatchingEngine)o;
        return Objects.equals(orderBooks, that.orderBooks) && Objects.equals(executionId,
            that.executionId) && Objects.equals(orderId, that.orderId) && Objects.equals(clientOrderIdsByUserId, that.clientOrderIdsByUserId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(orderBooks, executionId, orderId, clientOrderIdsByUserId);
    }

    public String toString()
    {
        return "MatchingEngine(orderBookEvents=" + this.orderBookEvents + ", orderBooks=" + this.orderBooks + ", executionId=" + this.executionId + ", " +
            "orderId=" + this.orderId + ", clientOrderIdsByUserId=" + this.clientOrderIdsByUserId + ")";
    }

    public static class Codec implements Stateful<MatchingEngine>
    {

        @Override
        public MatchingEngine decodeState(final ExchangeStateDecoder exchangeStateDecoder)
        {
            final Long2ObjectHashMap<OrderBook> orderBooks = new Long2ObjectHashMap<>();
            final MutableLong currentExecutionId = new MutableLong(MISSING_VALUE);
            final MutableLong currentOrderId = new MutableLong(MISSING_VALUE);
            final Long2ObjectHashMap<ObjectHashSet<String>> clientOrdersIdsByUserId = new Long2ObjectHashMap<>();

            exchangeStateDecoder.orderBooks().forEach(orderBookDecoder -> {
                final long productId = orderBookDecoder.productId();

                if (currentExecutionId.get() == MISSING_VALUE)
                {
                    final long executionId = orderBookDecoder.currentExecutionId();
                    currentExecutionId.set(executionId);
                }

                if (currentOrderId.get() == MISSING_VALUE)
                {
                    final long orderId = orderBookDecoder.currentOrderId();
                    currentOrderId.set(orderId);
                }

                final ObjectRBTreeSet<Order> bids = new ObjectRBTreeSet<>(BID_ORDER_COMPARATOR);
                final ObjectRBTreeSet<Order> asks = new ObjectRBTreeSet<>(ASK_ORDER_COMPARATOR);
                final Long2ObjectHashMap<Order> orders = new Long2ObjectHashMap<>();
                final Object2ObjectHashMap<String, Order> ordersByClientOrderId = new Object2ObjectHashMap<>();

                final OrderBook.OrderBookBuilder orderBookBuilder = OrderBook.builder()
                    .productId(productId)
                    .currentOrderId(currentOrderId)
                    .currentExecutionId(currentExecutionId);

                orderBookDecoder.bids().forEach(bidDecoder -> {
                    final Order bid = new Order(
                        bidDecoder.orderId(),
                        bidDecoder.clientOrderId(),
                        bidDecoder.userId(),
                        productId,
                        exchange.lob.domain.OrderStatus.get(bidDecoder.orderStatus().value()),
                        exchange.lob.domain.OrderType.get(bidDecoder.orderType().value()),
                        exchange.lob.domain.Side.get(bidDecoder.side().value()),
                        bidDecoder.price(),
                        bidDecoder.amount()
                    );

                    bids.add(bid);
                    orders.put(bid.getOrderId(), bid);
                    ordersByClientOrderId.put(bid.getClientOrderId(), bid);
                });

                orderBookDecoder.asks().forEach(askDecoder -> {
                    final Order ask = new Order(
                        askDecoder.orderId(),
                        askDecoder.clientOrderId(),
                        askDecoder.userId(),
                        productId,
                        exchange.lob.domain.OrderStatus.get(askDecoder.orderStatus().value()),
                        exchange.lob.domain.OrderType.get(askDecoder.orderType().value()),
                        exchange.lob.domain.Side.get(askDecoder.side().value()),
                        askDecoder.price(),
                        askDecoder.amount()
                    );

                    asks.add(ask);
                    orders.put(ask.getOrderId(), ask);
                    ordersByClientOrderId.put(ask.getClientOrderId(), ask);
                });

                final OrderBook orderBook = orderBookBuilder
                    .bids(bids)
                    .asks(asks)
                    .orders(orders)
                    .ordersByClientOrderId(ordersByClientOrderId)
                    .liquidityCache(LiquidityCache.fromBidsAndAsks(bids, asks))
                    .build();

                orderBooks.put(productId, orderBook);
            });

            exchangeStateDecoder.clientOrderIds().forEach(clientOrderIdsDecoder -> {
                final long userId = clientOrderIdsDecoder.userId();
                final String clientOrderId = clientOrderIdsDecoder.clientOrderId();
                if (!clientOrdersIdsByUserId.containsKey(userId))
                {
                    final ObjectHashSet<String> clientOrderIds = new ObjectHashSet<>();
                    clientOrderIds.add(clientOrderId);
                    clientOrdersIdsByUserId.put(userId, clientOrderIds);
                }
                else
                {
                    clientOrdersIdsByUserId.get(userId).add(clientOrderId);
                }
            });

            return MatchingEngine.builder()
                .executionId(currentExecutionId)
                .orderId(currentOrderId)
                .orderBooks(orderBooks)
                .clientOrderIdsByUserId(clientOrdersIdsByUserId)
                .build();
        }

        @Override
        public void encodeState(final MatchingEngine matchingEngine, final ExchangeStateEncoder exchangeStateEncoder)
        {
            final Long2ObjectHashMap<OrderBook> orderBooks = matchingEngine.orderBooks;
            final ExchangeStateEncoder.OrderBooksEncoder orderBooksEncoder = exchangeStateEncoder.orderBooksCount(orderBooks.size());

            orderBooks.forEach((productId, orderBook) -> OrderBook.encodeOrderBook(orderBooksEncoder, productId, orderBook));

            final Long2ObjectHashMap<ObjectHashSet<String>> clientOrderIdsByUserId = matchingEngine.clientOrderIdsByUserId;
            final ExchangeStateEncoder.ClientOrderIdsEncoder clientOrderIdsEncoder = exchangeStateEncoder.clientOrderIdsCount(
                clientOrderIdsByUserId.values().stream().mapToInt(Collection::size).sum());

            clientOrderIdsByUserId.forEach((userId, clientOrderIds) -> clientOrderIds
                .forEach(clientOrderId -> clientOrderIdsEncoder.next()
                    .userId(userId)
                    .clientOrderId(clientOrderId)));
        }
    }

    public static class MatchingEngineBuilder
    {
        private OrderBookEvents orderBookEvents;
        private Long2ObjectHashMap<OrderBook> orderBooks;
        private MutableLong executionId;
        private MutableLong orderId;
        private Long2ObjectHashMap<ObjectHashSet<String>> clientOrderIdsByUserId;

        MatchingEngineBuilder()
        {
        }

        public MatchingEngineBuilder orderBookEvents(OrderBookEvents orderBookEvents)
        {
            this.orderBookEvents = orderBookEvents;
            return this;
        }

        public MatchingEngineBuilder orderBooks(Long2ObjectHashMap<OrderBook> orderBooks)
        {
            this.orderBooks = orderBooks;
            return this;
        }

        public MatchingEngineBuilder executionId(MutableLong executionId)
        {
            this.executionId = executionId;
            return this;
        }

        public MatchingEngineBuilder orderId(MutableLong orderId)
        {
            this.orderId = orderId;
            return this;
        }

        public MatchingEngineBuilder clientOrderIdsByUserId(Long2ObjectHashMap<ObjectHashSet<String>> clientOrderIdsByUserId)
        {
            this.clientOrderIdsByUserId = clientOrderIdsByUserId;
            return this;
        }

        public MatchingEngine build()
        {
            return new MatchingEngine(orderBookEvents, orderBooks, executionId, orderId, clientOrderIdsByUserId);
        }

        @Override
        public String toString()
        {
            return "MatchingEngineBuilder{" +
                "orderBookEvents=" + orderBookEvents +
                ", orderBooks=" + orderBooks +
                ", executionId=" + executionId +
                ", orderId=" + orderId +
                ", clientOrderIdsByUserId=" + clientOrderIdsByUserId +
                '}';
        }
    }
}
