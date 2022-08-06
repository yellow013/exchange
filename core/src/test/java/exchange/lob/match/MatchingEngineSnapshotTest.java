package exchange.lob.match;

import exchange.lob.api.sbe.ExchangeStateDecoder;
import exchange.lob.api.sbe.ExchangeStateEncoder;
import exchange.lob.domain.OrderType;
import exchange.lob.domain.Side;
import exchange.lob.events.trading.NoOpOrderBookEvents;
import exchange.lob.match.execution.ExecutionSettler;
import exchange.lob.product.Asset;
import exchange.lob.product.Product;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MatchingEngineSnapshotTest
{

    private MatchingEngine matchingEngine;
    ExecutionSettler executionSettler = e -> {};

    @BeforeEach
    public void setUp()
    {
        matchingEngine = MatchingEngine.withOrderBookEvents(NoOpOrderBookEvents.INSTANCE);
    }

    private static Product createTestProduct()
    {
        return Product.builder()
            .productId(0)
            .baseAsset(Asset.builder().assetId(0L).scale((byte) 2).symbol("LOL").build())
            .counterAsset(Asset.builder().assetId(1L).scale((byte) 2).symbol("OMG").build())
            .build();
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

        Product product = createTestProduct();

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
}
