package exchange.lob.match;

import exchange.lob.api.codecs.internal.OrderStatus;
import exchange.lob.api.codecs.internal.OrderType;
import exchange.lob.api.codecs.internal.Side;
import exchange.lob.events.trading.NoOpOrderBookEvents;
import exchange.lob.match.execution.ExecutionSettler;
import exchange.lob.product.Asset;
import exchange.lob.product.Product;
import org.agrona.collections.MutableLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static exchange.lob.Assertions.assertReflectiveEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class OrderBookTest
{
    private static final long CORRELATION_ID = 123L;

    private static final long MISSING_VALUE = -1L;
    private OrderBook book;

    private static final long PRODUCT_ID = 123L;
    private final ExecutionSettler executionSettler = i -> {};

    private static Product product()
    {
        return Product.builder()
            .productId(PRODUCT_ID)
            .baseAsset(Asset.builder().assetId(0L).scale((byte)2).symbol("LOL").build())
            .counterAsset(Asset.builder().assetId(1L).scale((byte)2).symbol("OMG").build())
            .build();
    }

    private void assertBids(final Order... bids)
    {
        assertReflectiveEquals(book.bids, List.of(bids));
    }

    private void assertAsks(final Order... asks)
    {
        assertReflectiveEquals(book.asks, List.of(asks));
    }


    private static Order newOrder(
        final OrderType orderType,
        final Side side,
        final long price,
        final long amount
    )
    {
        return new Order(0L, "", 1L, PRODUCT_ID, OrderStatus.NEW, orderType, side, price, amount);
    }

    private static Order newOrder(
        final String clientOrderId,
        final OrderType orderType,
        final Side side,
        final long price,
        final long amount
    )
    {
        return new Order(0L, clientOrderId, 1L, PRODUCT_ID, OrderStatus.NEW, orderType, side, price, amount);
    }

    private static Order withStatus(final Order order, final OrderStatus orderStatus)
    {
        return new Order(
            order.getOrderId(),
            order.getClientOrderId(),
            order.getUserId(),
            order.getProductId(),
            orderStatus,
            order.getOrderType(),
            order.getSide(),
            order.getPrice(),
            order.getAmount()
        );
    }

    private static Order withId(final Order order, final long orderId)
    {
        return MatchingEngineTestUtils.orderWithId(order, orderId);
    }

    private final static Order limitBid100At100 = newOrder(OrderType.LMT, Side.BID, 100, 100);
    private final static Order limitBid50At100 = newOrder(OrderType.LMT, Side.BID, 100, 50);
    private final static Order limitBid150At100 = newOrder(OrderType.LMT, Side.BID, 100, 150);
    private final static Order limitBid200At100 = newOrder(OrderType.LMT, Side.BID, 100, 200);
    private final static Order limitBid150At90 = newOrder(OrderType.LMT, Side.BID, 90, 150);
    private final static Order limitBid100At90 = newOrder(OrderType.LMT, Side.BID, 90, 100);

    private final static Order limitBid200At110 = newOrder(OrderType.LMT, Side.BID, 110, 200);
    private final static Order limitAsk100At100 = newOrder(OrderType.LMT, Side.ASK, 100, 100);
    private final static Order limitAsk50At100 = newOrder(OrderType.LMT, Side.ASK, 100, 50);
    private final static Order limitAsk150At100 = newOrder(OrderType.LMT, Side.ASK, 100, 150);
    private final static Order limitAsk200At100 = newOrder(OrderType.LMT, Side.ASK, 100, 200);
    private final static Order limitAsk150At90 = newOrder(OrderType.LMT, Side.ASK, 90, 150);
    private final static Order limitAsk200At110 = newOrder(OrderType.LMT, Side.ASK, 110, 200);
    private final static Order limitAsk150At110 = newOrder(OrderType.LMT, Side.ASK, 110, 150);

    private final static Order marketBid100 = newOrder(OrderType.MKT, Side.BID, Long.MIN_VALUE, 100);
    private final static Order marketBid50 = newOrder(OrderType.MKT, Side.BID, Long.MIN_VALUE, 50);
    private final static Order marketBid150 = newOrder(OrderType.MKT, Side.BID, Long.MIN_VALUE, 150);
    private final static Order marketBid200 = newOrder(OrderType.MKT, Side.BID, Long.MIN_VALUE, 200);

    private final static Order marketAsk100 = newOrder(OrderType.MKT, Side.ASK, Long.MIN_VALUE, 100);
    private final static Order marketAsk50 = newOrder(OrderType.MKT, Side.ASK, Long.MIN_VALUE, 50);
    private final static Order marketAsk150 = newOrder(OrderType.MKT, Side.ASK, Long.MIN_VALUE, 150);
    private final static Order marketAsk200 = newOrder(OrderType.MKT, Side.ASK, Long.MIN_VALUE, 200);

    private static Map.Entry<Long, Order> orderEntry(Order order)
    {
        return entry(order.getOrderId(), order);
    }

    private void placeWithUnlimitedBalance(OrderBook book, Order order)
    {
        book.place(
            CORRELATION_ID,
            order.getClientOrderId(),
            product(),
            1L,
            order.getOrderType(),
            order.getSide(),
            order.getPrice(),
            order.getAmount(),
            Long.MAX_VALUE,
            executionSettler
        );
    }

    private void placeWithUnlimitedBalance(final OrderBook book, final Order order, final String clientOrderId)
    {
        book.place(
            CORRELATION_ID,
            clientOrderId,
            product(),
            1L,
            order.getOrderType(),
            order.getSide(),
            order.getPrice(),
            order.getAmount(),
            Long.MAX_VALUE,
            executionSettler
        );
    }

    private void placeWithBalance(OrderBook book, Order order, long balance)
    {
        book.place(
            CORRELATION_ID,
            order.getClientOrderId(),
            product(),
            1L,
            order.getOrderType(),
            order.getSide(),
            order.getPrice(),
            order.getAmount(),
            balance,
            executionSettler
        );
    }

    @BeforeEach
    public void setUp()
    {
        book = OrderBook.ofProduct(PRODUCT_ID)
            .orderBookEvents(NoOpOrderBookEvents.INSTANCE)
            .currentOrderId(new MutableLong(MISSING_VALUE))
            .currentExecutionId(new MutableLong(MISSING_VALUE))
            .build();
    }

    @Test
    public void testBid()
    {
        placeWithUnlimitedBalance(book, limitBid100At100);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitBid100At100, 0L), OrderStatus.NEW))
        );

        assertBids(
            withStatus(withId(limitBid100At100, 0L), OrderStatus.NEW)
        );
    }

    @Test
    public void testBidOrdering()
    {
        placeWithUnlimitedBalance(book, limitBid100At100);
        placeWithUnlimitedBalance(book, limitBid150At90);
        placeWithUnlimitedBalance(book, limitBid200At110);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitBid100At100, 0L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitBid150At90, 1L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitBid200At110, 2L), OrderStatus.NEW))
        );

        assertBids(
            withStatus(withId(limitBid200At110, 2L), OrderStatus.NEW),
            withStatus(withId(limitBid100At100, 0L), OrderStatus.NEW),
            withStatus(withId(limitBid150At90, 1L), OrderStatus.NEW)
        );
    }

    @Test
    public void testAsk()
    {
        placeWithUnlimitedBalance(book, limitAsk100At100);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitAsk100At100, 0L), OrderStatus.NEW))
        );

        assertAsks(
            withStatus(withId(limitAsk100At100, 0L), OrderStatus.NEW)
        );
    }

    @Test
    public void testAskOrdering()
    {
        placeWithUnlimitedBalance(book, limitAsk100At100);
        placeWithUnlimitedBalance(book, limitAsk150At90);
        placeWithUnlimitedBalance(book, limitAsk200At110);

        assertThat(book.orders).contains(
            orderEntry(withStatus(withId(limitAsk150At90, 1L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitAsk100At100, 0L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitAsk200At110, 2L), OrderStatus.NEW))
        );

        assertAsks(
            withStatus(withId(limitAsk150At90, 1L), OrderStatus.NEW),
            withStatus(withId(limitAsk100At100, 0L), OrderStatus.NEW),
            withStatus(withId(limitAsk200At110, 2L), OrderStatus.NEW)
        );
    }

    @Test
    public void testBothSidesPassivePlacement()
    {
        placeWithUnlimitedBalance(book, limitBid150At90);
        placeWithUnlimitedBalance(book, limitAsk100At100);


        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitBid150At90, 0L)),
            orderEntry(withId(limitAsk100At100, 1L))
        );

        assertBids(withId(limitBid150At90, 0L));
        assertAsks(withId(limitAsk100At100, 1L));
    }

    @Test
    public void testFullTopOfTheBookLimitBidMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitBid150At90);
        placeWithUnlimitedBalance(book, limitAsk100At100);
        placeWithUnlimitedBalance(book, limitAsk200At110);


        // taker placement
        placeWithUnlimitedBalance(book, limitBid100At100);


        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitBid150At90, 0L)),
            orderEntry(withId(limitAsk200At110, 2L))
        );

        assertBids(
            withId(limitBid150At90, 0L)
        );

        assertAsks(
            withId(limitAsk200At110, 2L)
        );
    }

    @Test
    public void testFullTopOfTheBookMarketBidMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitBid150At90);
        placeWithUnlimitedBalance(book, limitAsk100At100);
        placeWithUnlimitedBalance(book, limitAsk200At110);


        // taker placement
        placeWithUnlimitedBalance(book, marketBid100);


        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitBid150At90, 0L)),
            orderEntry(withId(limitAsk200At110, 2L))
        );

        assertBids(
            withId(limitBid150At90, 0L)
        );

        assertAsks(
            withId(limitAsk200At110, 2L)
        );
    }

    @Test
    public void testFullTopOfTheBookLimitAskMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);
        placeWithUnlimitedBalance(book, limitBid100At100);
        placeWithUnlimitedBalance(book, limitBid150At90);

        // taker placement
        placeWithUnlimitedBalance(book, limitAsk100At100);

        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitAsk200At110, 0L)),
            orderEntry(withId(limitBid150At90, 2L))
        );

        assertBids(
            withId(limitBid150At90, 2L)
        );

        assertAsks(
            withId(limitAsk200At110, 0L)
        );
    }

    @Test
    public void testFullTopOfTheBookMarketAskMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);
        placeWithUnlimitedBalance(book, limitBid100At100);
        placeWithUnlimitedBalance(book, limitBid150At90);

        // taker placement
        placeWithUnlimitedBalance(book, marketAsk100);

        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitAsk200At110, 0L)),
            orderEntry(withId(limitBid150At90, 2L))
        );

        assertBids(
            withId(limitBid150At90, 2L)
        );

        assertAsks(
            withId(limitAsk200At110, 0L)
        );
    }

    @Test
    public void testPartialTopOfTheBookLimitBidMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitBid150At90);
        placeWithUnlimitedBalance(book, limitAsk50At100);
        placeWithUnlimitedBalance(book, limitAsk200At110);

        // taker placement
        placeWithUnlimitedBalance(book, limitBid100At100);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitBid150At90, 0L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitBid50At100, 3L), OrderStatus.PARTIALLY_FILLED)),
            orderEntry(withStatus(withId(limitAsk200At110, 2L), OrderStatus.NEW))
        );

        assertBids(
            withStatus(withId(limitBid50At100, 3L), OrderStatus.PARTIALLY_FILLED),
            withStatus(withId(limitBid150At90, 0L), OrderStatus.NEW)
        );

        assertAsks(
            withStatus(withId(limitAsk200At110, 2L), OrderStatus.NEW)
        );
    }

    @Test
    public void testPartialTopOfTheBookMarketBidMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);
        placeWithUnlimitedBalance(book, limitAsk50At100);
        placeWithUnlimitedBalance(book, limitBid150At90);

        // taker placement
        placeWithUnlimitedBalance(book, marketBid100);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitAsk150At110, 0L), OrderStatus.PARTIALLY_FILLED)),
            orderEntry(withStatus(withId(limitBid150At90, 2L), OrderStatus.NEW))
        );

        assertBids(
            withStatus(withId(limitBid150At90, 2L), OrderStatus.NEW)
        );

        assertAsks(
            withStatus(withId(limitAsk150At110, 0L), OrderStatus.PARTIALLY_FILLED)
        );
    }

    @Test
    public void testPartialTopOfTheBookLimitAskMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);
        placeWithUnlimitedBalance(book, limitBid50At100);
        placeWithUnlimitedBalance(book, limitBid150At90);

        // taker placement
        placeWithUnlimitedBalance(book, limitAsk100At100);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitAsk200At110, 0L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitAsk50At100, 3L), OrderStatus.PARTIALLY_FILLED)),
            orderEntry(withStatus(withId(limitBid150At90, 2L), OrderStatus.NEW))
        );

        assertBids(
            withStatus(withId(limitBid150At90, 2L), OrderStatus.NEW)
        );

        assertAsks(
            withStatus(withId(limitAsk50At100, 3L), OrderStatus.PARTIALLY_FILLED),
            withStatus(withId(limitAsk200At110, 0L), OrderStatus.NEW)
        );
    }

    @Test
    public void testPartialTopOfTheBookMarketAskMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);
        placeWithUnlimitedBalance(book, limitBid50At100);
        placeWithUnlimitedBalance(book, limitBid150At90);

        // taker placement
        placeWithUnlimitedBalance(book, marketAsk100);


        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitAsk200At110, 0L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitBid100At90, 2L), OrderStatus.PARTIALLY_FILLED))
        );

        assertBids(
            withStatus(withId(limitBid100At90, 2L), OrderStatus.PARTIALLY_FILLED)
        );

        assertAsks(
            withStatus(withId(limitAsk200At110, 0L), OrderStatus.NEW)
        );
    }

    @Test
    public void testMultiLevelLimitBidMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);
        placeWithUnlimitedBalance(book, limitAsk50At100);
        placeWithUnlimitedBalance(book, limitAsk100At100);

        placeWithUnlimitedBalance(book, limitBid150At90);

        // taker placement
        placeWithUnlimitedBalance(book, limitBid150At100);

        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitAsk200At110, 0L)),
            orderEntry(withId(limitBid150At90, 3L))
        );

        assertBids(
            withId(limitBid150At90, 3L)
        );

        assertAsks(
            withId(limitAsk200At110, 0L)
        );
    }

    @Test
    public void testMultiLevelMarketBidMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);
        placeWithUnlimitedBalance(book, limitAsk50At100);
        placeWithUnlimitedBalance(book, limitAsk100At100);

        placeWithUnlimitedBalance(book, limitBid150At90);


        // taker placement
        placeWithUnlimitedBalance(book, marketBid150);


        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitAsk200At110, 0L)),
            orderEntry(withId(limitBid150At90, 3L))
        );

        assertBids(
            withId(limitBid150At90, 3L)
        );

        assertAsks(
            withId(limitAsk200At110, 0L)
        );
    }

    @Test
    public void testMultiLevelLimitAskMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);

        placeWithUnlimitedBalance(book, limitBid50At100);
        placeWithUnlimitedBalance(book, limitBid100At100);
        placeWithUnlimitedBalance(book, limitBid150At90);


        // taker placement
        placeWithUnlimitedBalance(book, limitAsk150At100);


        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitAsk200At110, 0L)),
            orderEntry(withId(limitBid150At90, 3L))
        );

        assertBids(
            withId(limitBid150At90, 3L)
        );

        assertAsks(
            withId(limitAsk200At110, 0L)
        );
    }

    @Test
    public void testMultiLevelMarketAskMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);

        placeWithUnlimitedBalance(book, limitBid50At100);
        placeWithUnlimitedBalance(book, limitBid100At100);
        placeWithUnlimitedBalance(book, limitBid150At90);


        // taker placement
        placeWithUnlimitedBalance(book, marketAsk150);


        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitAsk200At110, 0L)),
            orderEntry(withId(limitBid150At90, 3L))
        );

        assertBids(
            withId(limitBid150At90, 3L)
        );

        assertAsks(
            withId(limitAsk200At110, 0L)
        );
    }

    @Test
    public void testOverflowLimitBidMatching()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk50At100);
        placeWithUnlimitedBalance(book, limitAsk100At100);

        // taker placement
        placeWithUnlimitedBalance(book, limitBid200At100);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitBid50At100, 2L), OrderStatus.PARTIALLY_FILLED))
        );

        assertBids(
            withStatus(withId(limitBid50At100, 2L), OrderStatus.PARTIALLY_FILLED)
        );

        assertThat(book.asks).isEmpty();
    }

    @Test
    public void testOverflowMarketBidRejected()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk50At100);
        placeWithUnlimitedBalance(book, limitAsk100At100);

        // taker placement
        placeWithUnlimitedBalance(book, marketBid200);


        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitAsk50At100, 0L)),
            orderEntry(withId(limitAsk100At100, 1L))
        );

        assertBids();
        assertAsks(
            withId(limitAsk50At100, 0L),
            withId(limitAsk100At100, 1L)
        );
    }

    @Test
    public void testOverflowLimitAsk()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitBid50At100);
        placeWithUnlimitedBalance(book, limitBid100At100);

        // taker placement
        placeWithUnlimitedBalance(book, limitAsk200At100);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitAsk50At100, 2L), OrderStatus.PARTIALLY_FILLED))
        );

        assertBids();

        assertAsks(
            withStatus(withId(limitAsk50At100, 2L), OrderStatus.PARTIALLY_FILLED)
        );
    }

    @Test
    public void testOverflowMarketAskRejected()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitBid50At100);
        placeWithUnlimitedBalance(book, limitBid100At100);

        // taker placement
        placeWithUnlimitedBalance(book, marketAsk200);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitBid50At100, 0L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitBid100At100, 1L), OrderStatus.NEW))
        );

        assertBids(
            withStatus(withId(limitBid50At100, 0L), OrderStatus.NEW),
            withStatus(withId(limitBid100At100, 1L), OrderStatus.NEW)
        );

        assertThat(book.asks).isEmpty();
    }

    @Test
    public void testUnfilledBidResting()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);
        placeWithUnlimitedBalance(book, limitAsk100At100);
        placeWithUnlimitedBalance(book, limitBid150At90);

        // taker placement
        placeWithUnlimitedBalance(book, limitBid200At100);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitAsk200At110, 0L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitBid150At90, 2L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitBid100At100, 3L), OrderStatus.PARTIALLY_FILLED))
        );

        assertBids(
            withStatus(withId(limitBid100At100, 3L), OrderStatus.PARTIALLY_FILLED),
            withStatus(withId(limitBid150At90, 2L), OrderStatus.NEW)
        );

        assertAsks(
            withStatus(withId(limitAsk200At110, 0L), OrderStatus.NEW)
        );
    }

    @Test
    public void testUnfilledAskResting()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);
        placeWithUnlimitedBalance(book, limitBid100At100);
        placeWithUnlimitedBalance(book, limitBid150At90);


        // taker placement
        placeWithUnlimitedBalance(book, limitAsk200At100);

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitAsk200At110, 0L), OrderStatus.NEW)),
            orderEntry(withStatus(withId(limitAsk100At100, 3L), OrderStatus.PARTIALLY_FILLED)),

            orderEntry(withStatus(withId(limitBid150At90, 2L), OrderStatus.NEW))
        );

        assertBids(
            withStatus(withId(limitBid150At90, 2L), OrderStatus.NEW)
        );

        assertAsks(
            withStatus(withId(limitAsk100At100, 3L), OrderStatus.PARTIALLY_FILLED),
            withStatus(withId(limitAsk200At110, 0L), OrderStatus.NEW)
        );
    }

    @Test
    public void testMarketBidOrderInsufficientBalanceRejected()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);
        placeWithUnlimitedBalance(book, limitAsk50At100);
        placeWithUnlimitedBalance(book, limitAsk100At100);

        placeWithUnlimitedBalance(book, limitBid150At90);

        // taker placement
        placeWithBalance(book, marketBid150, 50L);

        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitAsk200At110, 0L)),
            orderEntry(withId(limitAsk50At100, 1L)),
            orderEntry(withId(limitAsk100At100, 2L)),
            orderEntry(withId(limitBid150At90, 3L))
        );

        assertBids(
            withId(limitBid150At90, 3L)
        );

        assertAsks(
            withId(limitAsk50At100, 1L),
            withId(limitAsk100At100, 2L),
            withId(limitAsk200At110, 0L)
        );
    }

    @Test
    public void testMarketAskOrderInsufficientBalanceRejected()
    {
        // resting orders
        placeWithUnlimitedBalance(book, limitAsk200At110);

        placeWithUnlimitedBalance(book, limitBid50At100);
        placeWithUnlimitedBalance(book, limitBid100At100);
        placeWithUnlimitedBalance(book, limitBid150At90);

        // taker placement
        placeWithBalance(book, marketAsk150, 50L);

        assertThat(book.orders).containsOnly(
            orderEntry(withId(limitAsk200At110, 0L)),
            orderEntry(withId(limitBid50At100, 1L)),
            orderEntry(withId(limitBid100At100, 2L)),
            orderEntry(withId(limitBid150At90, 3L))
        );

        assertBids(
            withId(limitBid50At100, 1L),
            withId(limitBid100At100, 2L),
            withId(limitBid150At90, 3L)
        );

        assertAsks(
            withId(limitAsk200At110, 0L)
        );
    }

    @Test
    public void testFullCancelBid()
    {
        placeWithUnlimitedBalance(book, limitBid50At100, "limitBid50At100");

        book.cancel(
            CORRELATION_ID,
            "limitBid50At100",
            product(),
            1L,
            limitBid50At100.getAmount(),
            executionSettler
        );

        assertThat(book.orders).isEmpty();
        assertBids();
        assertThat(book.asks).isEmpty();
    }

    @Test
    public void testFullCancelAsk()
    {
        placeWithUnlimitedBalance(book, limitAsk50At100, "limitAsk50At100");

        book.cancel(
            CORRELATION_ID,
            "limitAsk50At100",
            product(),
            1L,
            limitAsk50At100.getAmount(),
            executionSettler
        );

        assertThat(book.orders).isEmpty();
        assertBids();
        assertThat(book.asks).isEmpty();
    }

    @Test
    public void testPartialCancelBid()
    {
        placeWithUnlimitedBalance(book, limitBid50At100, "limitBid50At100");
        book.cancel(
            CORRELATION_ID,
            "limitBid50At100",
            product(),
            1L,
            25L,
            executionSettler
        );

        Order resting = newOrder("limitBid50At100", OrderType.LMT, Side.BID, 100L, 25L);

        assertThat(book.orders).containsOnly(orderEntry(withId(resting, 0L)));
        assertBids(withId(resting, 0L));
        assertThat(book.asks).isEmpty();
    }

    @Test
    public void testPartialCancelAsk()
    {
        placeWithUnlimitedBalance(book, limitAsk50At100, "limitAsk50At100");

        book.cancel(
            CORRELATION_ID,
            "limitAsk50At100",
            product(),
            1L,
            25L,
            executionSettler
        );

        Order resting = newOrder("limitAsk50At100", OrderType.LMT, Side.ASK, 100L, 25L);

        assertThat(book.orders).containsOnly(orderEntry(withId(resting, 0L)));
        assertBids();
        assertAsks(withId(resting, 0L));
    }

    @Test
    public void testBadOrderCancelBid()
    {
        placeWithUnlimitedBalance(book, limitBid50At100);

        book.cancel(
            CORRELATION_ID,
            "bogus",
            product(),
            1L,
            limitBid50At100.getAmount(),
            executionSettler
        );

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitBid50At100, 0L), OrderStatus.NEW))
        );

        assertBids(withId(limitBid50At100, 0L));
        assertThat(book.asks).isEmpty();
    }

    @Test
    public void testBadOrderCancelAsk()
    {
        placeWithUnlimitedBalance(book, limitAsk50At100);
        book.cancel(
            CORRELATION_ID,
            "bogus",
            product(),
            1L,
            limitAsk50At100.getAmount(),
            executionSettler
        );

        assertThat(book.orders).containsOnly(
            orderEntry(withStatus(withId(limitAsk50At100, 0L), OrderStatus.NEW))
        );

        assertBids();
        assertAsks(withId(limitAsk50At100, 0L));
    }
}
