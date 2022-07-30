package exchange.lob.match;

import exchange.lob.api.codecs.internal.*;
import exchange.lob.events.trading.NoOpOrderBookEvents;
import exchange.lob.match.execution.ExecutionSettler;
import exchange.lob.product.Asset;
import exchange.lob.product.Product;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.MutableLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static exchange.lob.match.MatchingEngineTestUtils.orderEntry;
import static exchange.lob.match.MatchingEngineTestUtils.orderWithId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class MatchingEngineTest
{

    private static final long MISSING_VALUE = -1L;
    private MatchingEngine matchingEngine;
    ExecutionSettler executionSettler = e -> {};

    @BeforeEach
    public void setUp()
    {
        matchingEngine = MatchingEngine.withOrderBookEvents(NoOpOrderBookEvents.INSTANCE);
    }

    private static Map.Entry<Long, OrderBook> orderBookEntry(Product product)
    {
        return entry(product.getProductId(), OrderBook.ofProduct(product.getProductId())
            .currentOrderId(new MutableLong(MISSING_VALUE))
            .currentExecutionId(new MutableLong(MISSING_VALUE))
            .build());
    }

    private static Product createTestProduct(long productId)
    {
        return Product.builder()
            .productId(productId)
            .baseAsset(Asset.builder().assetId(0L).scale((byte) 2).symbol("LOL").build())
            .counterAsset(Asset.builder().assetId(1L).scale((byte) 2).symbol("OMG").build())
            .build();
    }

    @Test
    public void shouldAddOrderBookWhenAddingProduct()
    {
        Product product = Product.builder().productId(0L).build();
        matchingEngine.onAddProduct(product.getProductId());
        assertThat(matchingEngine.orderBooks).containsExactly(orderBookEntry(product));
    }

    @Test
    public void shouldEncodeAndDecodeEmptySnapshot()
    {
        MutableDirectBuffer buffer = new ExpandableDirectByteBuffer(4096);
        ExchangeStateEncoder exchangeStateEncoder = new ExchangeStateEncoder().wrap(buffer, 0);

        MatchingEngine.CODEC.encodeState(matchingEngine, exchangeStateEncoder);
        ExchangeStateDecoder exchangeStateDecoder = new ExchangeStateDecoder().wrap(buffer, 0, 0, 0);
        MatchingEngine decodedMatchingEngine = MatchingEngine.CODEC.decodeState(exchangeStateDecoder);

        assertEquals(matchingEngine, decodedMatchingEngine);
    }

    @Test
    public void shouldEncodeAndDecodeNonEmptySnapshot()
    {
        MutableDirectBuffer buffer = new ExpandableDirectByteBuffer(4096);
        ExchangeStateEncoder exchangeStateEncoder = new ExchangeStateEncoder().wrap(buffer, 0);

        Product product = createTestProduct(0L);

        matchingEngine.onAddProduct(product.getProductId());

        matchingEngine.handleOrderPlacement(
            1L,
            "order1",
            1L,
            product,
            OrderType.LMT,
            Side.BID,
            100L,
            100L,
            Long.MAX_VALUE,
            executionSettler
        );

        matchingEngine.handleOrderPlacement(
            1L,
            "order2",
            2L,
            product,
            OrderType.LMT,
            Side.ASK,
            100L,
            100L,
            Long.MAX_VALUE,
            executionSettler
        );

        matchingEngine.handleOrderPlacement(
            1L,
            "order3",
            3L,
            product,
            OrderType.LMT,
            Side.BID,
            50L,
            100L,
            Long.MAX_VALUE,
            executionSettler
        );

        matchingEngine.handleOrderPlacement(
            1L,
            "order4",
            4L,
            product,
            OrderType.LMT,
            Side.ASK,
            200L,
            100L,
            Long.MAX_VALUE,
            executionSettler
        );

        MatchingEngine.CODEC.encodeState(matchingEngine, exchangeStateEncoder);
        ExchangeStateDecoder exchangeStateDecoder = new ExchangeStateDecoder().wrap(buffer, 0, 0, 0);
        MatchingEngine decodedMatchingEngine = MatchingEngine.CODEC.decodeState(exchangeStateDecoder);

        assertEquals(matchingEngine, decodedMatchingEngine);
    }

    @Test
    public void shouldPlaceLimitBidOrder()
    {
        long productId = 0L;
        final Product testProduct = createTestProduct(productId);
        final long userId = Long.MAX_VALUE;

        matchingEngine.onAddProduct(productId);

        final Order order = new Order(
            0L,
            "myOrder",
            userId,
            productId,
            OrderStatus.NEW,
            OrderType.LMT,
            Side.BID,
            100L,
            100L
        );

        matchingEngine.handleOrderPlacement(
            1L,
            order.getClientOrderId(),
            userId,
            testProduct,
            order.getOrderType(),
            order.getSide(),
            order.getPrice(),
            order.getAmount(),
            Long.MAX_VALUE,
            executionSettler
        );

        assertThat(matchingEngine.orderBooks.get(productId).orders).containsOnly(
            orderEntry(orderWithId(order, 0L))
        );

        assertThat(matchingEngine.orderBooks.get(productId).bids).containsExactly(
            orderWithId(order, 0L)
        );

        assertThat(matchingEngine.orderBooks.get(productId).asks).isEmpty();
    }

    @Test
    public void shouldPlaceLimitAskOrder()
    {
        long productId = 0L;
        final Product testProduct = createTestProduct(productId);
        matchingEngine.onAddProduct(productId);

        final Order order = new Order(
            0L,
            "myOrder",
            Long.MAX_VALUE,
            productId,
            OrderStatus.NEW,
            OrderType.LMT,
            Side.ASK,
            100L,
            100L
        );

        matchingEngine.handleOrderPlacement(
            order.getUserId(),
            order.getClientOrderId(),
            order.getUserId(),
            testProduct,
            order.getOrderType(),
            order.getSide(),
            order.getPrice(),
            order.getAmount(),
            Long.MAX_VALUE,
            executionSettler
        );

        assertThat(matchingEngine.orderBooks.get(productId).orders).containsOnly(
            orderEntry(orderWithId(order, 0L))
        );

        assertThat(matchingEngine.orderBooks.get(productId).bids).isEmpty();

        assertThat(matchingEngine.orderBooks.get(productId).asks).containsExactly(
            orderWithId(order, 0L)
        );
    }

    @Test
    public void shouldRejectMarketBidOrderWhenLiquidityInsufficient()
    {
        long productId = 0L;
        final Product testProduct = createTestProduct(productId);
        matchingEngine.onAddProduct(productId);

        final Order order = new Order(
            0L,
            "myOrder",
            Long.MAX_VALUE,
            productId,
            OrderStatus.NEW,
            OrderType.MKT,
            Side.BID,
            100L,
            100L
        );

        matchingEngine.handleOrderPlacement(
            1L,
            "rej",
            1L,
            testProduct,
            order.getOrderType(),
            order.getSide(),
            order.getPrice(),
            order.getAmount(),
            Long.MAX_VALUE,
            executionSettler
        );

        assertThat(matchingEngine.orderBooks.get(productId).orders).isEmpty();
        assertThat(matchingEngine.orderBooks.get(productId).bids).isEmpty();
        assertThat(matchingEngine.orderBooks.get(productId).asks).isEmpty();
    }

    @Test
    public void shouldRejectMarketAskOrderWhenLiquidityInsufficient()
    {
        long productId = 0L;
        final Product testProduct = createTestProduct(productId);
        matchingEngine.onAddProduct(productId);

        final Order order = new Order(
            0L,
            "myOrder",
            Long.MAX_VALUE,
            productId,
            OrderStatus.NEW,
            OrderType.MKT,
            Side.ASK,
            100L,
            100L
        );

        matchingEngine.handleOrderPlacement(
            1L,
            "rej",
            1L,
            testProduct,
            order.getOrderType(),
            order.getSide(),
            order.getPrice(),
            order.getAmount(),
            Long.MAX_VALUE,
            executionSettler
        );

        assertThat(matchingEngine.orderBooks.get(productId).orders).isEmpty();
        assertThat(matchingEngine.orderBooks.get(productId).bids).isEmpty();
        assertThat(matchingEngine.orderBooks.get(productId).asks).isEmpty();
    }
}
