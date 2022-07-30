package exchange.lob.risk;

import exchange.lob.api.codecs.internal.ExchangeStateDecoder;
import exchange.lob.api.codecs.internal.ExchangeStateEncoder;
import exchange.lob.api.codecs.internal.OrderType;
import exchange.lob.api.codecs.internal.Side;
import exchange.lob.events.trading.NoOpOrderBookEvents;
import exchange.lob.product.Asset;
import exchange.lob.product.Product;
import exchange.lob.product.ProductService;
import exchange.lob.product.ProductServiceTestUtils;
import exchange.lob.user.UserService;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.collections.Long2LongHashMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static exchange.lob.user.UserService.EXCHANGE_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RiskEngineTest
{

    private static final long MISSING_VALUE = -1L;

    final RiskEngine riskEngine = RiskEngine.withOrderBookEvents(NoOpOrderBookEvents.INSTANCE);
    private final RiskEngineHelper riskEngineHelper = new RiskEngineHelper(riskEngine, UserService.create());

    private final ProductService productService = ProductService.create();

    private static Map.Entry<Long, Long2LongHashMap> emptyBalance(long userId)
    {
        return entry(userId, new Long2LongHashMap(-1L));
    }

    private static Map.Entry<Long, Long2LongHashMap> exchangeBalanceEntry(long assetId)
    {
        Long2LongHashMap balances = new Long2LongHashMap(MISSING_VALUE);
        balances.put(assetId, 0L);
        return entry(EXCHANGE_USER_ID, balances);
    }

    private static Map.Entry<Long, Long2LongHashMap> emptyExchangeBalanceEntry()
    {
        Long2LongHashMap balances = new Long2LongHashMap(MISSING_VALUE);
        return entry(EXCHANGE_USER_ID, balances);
    }

    @Test
    public void shouldAddUsers()
    {
        long userId1 = riskEngineHelper.addUser();
        long userId2 = riskEngineHelper.addUser();
        long userId3 = riskEngineHelper.addUser();

        assertThat(riskEngine.balances).containsOnly(
            emptyExchangeBalanceEntry(),
            emptyBalance(userId1),
            emptyBalance(userId2),
            emptyBalance(userId3)
        );
    }

    @Test
    public void shouldDepositPristine()
    {
        long userId = riskEngineHelper.addUser();
        Asset asset = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset);
        riskEngineHelper.updateBalance(userId, asset.getAssetId(), 1000000L);
        assertThat(riskEngine.balances.get(userId)).containsOnly(entry(asset.getAssetId(), 1000000L));
    }

    @Test
    public void shouldWithdrawFullBalance()
    {
        long userId = riskEngineHelper.addUser();
        Asset asset = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset);
        long assetId = asset.getAssetId();

        riskEngineHelper.updateBalance(
            userId,
            asset.getAssetId(),
            1000000L // £10,000
        );

        assertThat(riskEngine.balances.get(userId)).containsOnly(entry(assetId, 1000000L));

        riskEngineHelper.updateBalance(
            userId,
            assetId,
            -1000000L // £10,000
        );

        assertThat(riskEngine.balances.get(userId)).containsOnly(entry(assetId, 0L));
    }

    @Test
    public void shouldWithdrawPartialBalance()
    {
        long userId = riskEngineHelper.addUser();
        long assetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();

        riskEngineHelper.updateBalance(
            userId,
            assetId,
            1000000L // £10,000
        );

        assertThat(riskEngine.balances.get(userId)).containsOnly(entry(assetId, 1000000L));

        riskEngineHelper.updateBalance(
            userId,
            assetId,
            -500000L // -£5,000
        );

        assertThat(riskEngine.balances).hasSize(2);
        assertThat(riskEngine.balances).contains(
            exchangeBalanceEntry(assetId)
        );
        assertThat(riskEngine.balances.get(userId)).containsOnly(
            entry(assetId, 500000L)
        );
    }

    @Test
    public void shouldNotOverdrawBalance()
    {
        long userId = riskEngineHelper.addUser();
        long assetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();

        // deposit
        riskEngineHelper.updateBalance(
            userId,
            assetId,
            1000000L // £10,000
        );

        assertThat(riskEngine.balances.get(userId)).containsOnly(entry(assetId, 1000000L));

        // withdraw
        riskEngineHelper.updateBalance(
            userId,
            assetId,
            -1500000L // £15,000
        );

        // check balance remains the same
        assertThat(riskEngine.balances.get(userId)).containsOnly(entry(assetId, 1000000L));
    }

    @Test
    public void shouldNotOverdrawNewAssetBalance()
    {
        long userId = riskEngineHelper.addUser();
        long assetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        // withdraw
        riskEngineHelper.updateBalance(
            userId,
            assetId,
            -1000000L // £10,000
        );


        Long2LongHashMap userBalances = new Long2LongHashMap(MISSING_VALUE);
        userBalances.put(assetId, 0L);

        assertThat(riskEngine.balances).containsOnly(
            entry(userId, userBalances),
            exchangeBalanceEntry(assetId)
        );
    }

    @Test
    public void willDepositNonExistentAssetBalance()
    {
        long userId = riskEngineHelper.addUser();
        // deposit of non-existing asset ID will succeed
        // but is impossible in the wider context of the applicaton
        final long bogusAssetId = 123231231L;
        riskEngineHelper.updateBalance(
            userId,
            bogusAssetId, // bogus asset ID
            1000000L
        );

        assertThat(riskEngine.balances.get(userId)).containsOnly(entry(bogusAssetId, 1000000L));
    }

    @Test
    public void shouldNotUpdateBalanceOfNonExistentUserExistentAsset()
    {
        long userId = riskEngineHelper.addUser();
        long bogusUserId = 123L;
        long assetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();

        // deposit will throw because this is exceptional behaviour
        assertThrows(NullPointerException.class, () -> riskEngineHelper.updateBalance(
            bogusUserId,
            assetId,
            1000000L // £10,000
        ));

        Long2LongHashMap userBalances = new Long2LongHashMap(MISSING_VALUE);
        userBalances.put(assetId, 0L);

        assertThat(riskEngine.balances).containsOnly(
            exchangeBalanceEntry(assetId),
            entry(userId, userBalances)
        );
    }

    @Test
    public void shouldNotUpdateBalanceOfNonExistentUserNonExistentAsset()
    {
        assertThrows(NullPointerException.class, () -> riskEngineHelper.updateBalance(
            12341234123L, // bogus
            1234123, // bogus
            1000000L
        ));

        assertThat(riskEngine.balances).containsOnly(emptyExchangeBalanceEntry());
    }

    @Test
    public void shouldEncodeAndDecodeEmptySnapshot()
    {
        MutableDirectBuffer buffer = new ExpandableDirectByteBuffer(4096);
        ExchangeStateEncoder exchangeStateEncoder = new ExchangeStateEncoder().wrap(buffer, 0);

        RiskEngine.CODEC.encodeState(riskEngine, exchangeStateEncoder);
        ExchangeStateDecoder exchangeStateDecoder = new ExchangeStateDecoder().wrap(buffer, 0, 0, 0);
        RiskEngine decodedRiskEngine = RiskEngine.CODEC.decodeState(exchangeStateDecoder);

        assertEquals(riskEngine, decodedRiskEngine);
    }

    @Test
    public void shouldEncodeAndDecodeNonEmptySnapshot()
    {
        riskEngineHelper.addUser();
        riskEngineHelper.addUser();
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();

        riskEngineHelper.updateBalance(userId, baseAssetId, 1000000L);

        MutableDirectBuffer buffer = new ExpandableDirectByteBuffer(4096);
        ExchangeStateEncoder exchangeStateEncoder = new ExchangeStateEncoder().wrap(buffer, 0);
        RiskEngine.CODEC.encodeState(riskEngine, exchangeStateEncoder);
        ExchangeStateDecoder exchangeStateDecoder = new ExchangeStateDecoder()
            .wrap(exchangeStateEncoder.buffer(), exchangeStateEncoder.offset(), 0, 0);

        RiskEngine decodedRiskEngine = RiskEngine.CODEC.decodeState(exchangeStateDecoder);
        assertEquals(riskEngine, decodedRiskEngine);
    }

    @Test
    public void shouldIncludeZeroBalancesWhenDecodingSnapshot()
    {
        riskEngineHelper.addUser();
        riskEngineHelper.addUser();
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();

        riskEngineHelper.updateBalance(userId, baseAssetId, 1000000L);
        riskEngineHelper.updateBalance(userId, baseAssetId, -1000000L);

        MutableDirectBuffer buffer = new ExpandableDirectByteBuffer(4096);
        ExchangeStateEncoder exchangeStateEncoder = new ExchangeStateEncoder().wrap(buffer, 0);
        RiskEngine.CODEC.encodeState(riskEngine, exchangeStateEncoder);
        ExchangeStateDecoder exchangeStateDecoder = new ExchangeStateDecoder()
            .wrap(exchangeStateEncoder.buffer(), exchangeStateEncoder.offset(), 0, 0);
        RiskEngine decodedRiskEngine = RiskEngine.CODEC.decodeState(exchangeStateDecoder);
        // assert that zero balance is not present in decoded version
        assertThat(decodedRiskEngine.balances.get(userId)).isNotEmpty();
    }

    @Test
    public void shouldHandleValidLimitBidOrder()
    {
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        long counterAssetId = ProductServiceTestUtils
            .addAsset(productService, "USD", (byte) 2, riskEngine::addAsset).getAssetId();
        long makerFee = -10L;
        long takerFee = 25L;
        ProductServiceTestUtils.addProduct(productService, baseAssetId, counterAssetId, makerFee, takerFee);
        final Product product = productService.getProduct("GBPUSD");
        riskEngineHelper.updateBalance(userId, counterAssetId, 1000000L);
        riskEngineHelper.handleOrder(
            userId,
            product,
            OrderType.LMT,
            Side.BID,
            200L,
            400000L
        );

        assertThat(riskEngine.balances.get(userId).get(counterAssetId)).isEqualTo(200000L);
    }

    @Test
    public void shouldHandleFullBalanceLimitBidOrder()
    {
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        long counterAssetId = ProductServiceTestUtils
            .addAsset(productService, "USD", (byte) 2, riskEngine::addAsset).getAssetId();
        long makerFee = -10L;
        long takerFee = 25L;
        ProductServiceTestUtils.addProduct(productService, baseAssetId, counterAssetId, makerFee, takerFee);
        final Product product = productService.getProduct("GBPUSD");
        riskEngineHelper.updateBalance(userId, counterAssetId, 1000000L);
        riskEngineHelper.handleOrder(
            userId,
            product,
            OrderType.LMT,
            Side.BID,
            200L,
            500000L
        );

        assertThat(riskEngine.balances.get(userId).get(counterAssetId)).isEqualTo(0L);
    }

    @Test
    public void shouldHandleInvalidLimitBidOrder()
    {
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        long counterAssetId = ProductServiceTestUtils
            .addAsset(productService, "USD", (byte) 2, riskEngine::addAsset).getAssetId();
        long makerFee = -10L;
        long takerFee = 25L;
        ProductServiceTestUtils.addProduct(productService, baseAssetId, counterAssetId, makerFee, takerFee);
        final Product product = productService.getProduct("GBPUSD");
        riskEngineHelper.updateBalance(userId, counterAssetId, 1000000L);
        riskEngineHelper.handleOrder(
            userId,
            product,
            OrderType.LMT,
            Side.BID,
            200L,
            500001L
        );

        assertThat(riskEngine.balances.get(userId).get(counterAssetId)).isEqualTo(1000000L);
    }

    @Test
    public void shouldHandleLimitAskOrder()
    {
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        long counterAssetId = ProductServiceTestUtils
            .addAsset(productService, "USD", (byte) 2, riskEngine::addAsset).getAssetId();
        long makerFee = -10L;
        long takerFee = 25L;
        ProductServiceTestUtils.addProduct(productService, baseAssetId, counterAssetId, makerFee, takerFee);
        final Product product = productService.getProduct("GBPUSD");
        riskEngineHelper.updateBalance(userId, baseAssetId, 1000000L);
        riskEngineHelper.handleOrder(
            userId,
            product,
            OrderType.LMT,
            Side.ASK,
            2L,
            500000L
        );

        assertThat(riskEngine.balances.get(userId).get(baseAssetId)).isEqualTo(500000L);
    }

    @Test
    public void shouldHandleFullBalanceLimitAskOrder()
    {
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        long counterAssetId = ProductServiceTestUtils
            .addAsset(productService, "USD", (byte) 2, riskEngine::addAsset).getAssetId();
        long makerFee = -10L;
        long takerFee = 25L;

        ProductServiceTestUtils.addProduct(productService, baseAssetId, counterAssetId, makerFee, takerFee);
        final Product product = productService.getProduct("GBPUSD");

        riskEngineHelper.updateBalance(userId, baseAssetId, 1000000L);
        riskEngineHelper.handleOrder(
            userId,
            product,
            OrderType.LMT,
            Side.ASK,
            2L,
            1000000L
        );

        assertThat(riskEngine.balances.get(userId).get(baseAssetId)).isEqualTo(0L);
    }

    @Test
    public void shouldHandleInvalidLimitAskOrder()
    {
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        long counterAssetId = ProductServiceTestUtils
            .addAsset(productService, "USD", (byte) 2, riskEngine::addAsset).getAssetId();
        long makerFee = -10L;
        long takerFee = 25L;

        ProductServiceTestUtils.addProduct(productService, baseAssetId, counterAssetId, makerFee, takerFee);
        final Product product = productService.getProduct("GBPUSD");

        riskEngineHelper.updateBalance(userId, baseAssetId, 1000000L);
        riskEngineHelper.handleOrder(
            userId,
            product,
            OrderType.LMT,
            Side.ASK,
            2L,
            1000001L
        );

        assertThat(riskEngine.balances.get(userId).get(baseAssetId)).isEqualTo(1000000L);
    }

    @Test
    public void shouldHandleMarketBidOrder()
    {
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        long counterAssetId = ProductServiceTestUtils
            .addAsset(productService, "USD", (byte) 2, riskEngine::addAsset).getAssetId();
        long makerFee = -10L;
        long takerFee = 25L;

        ProductServiceTestUtils.addProduct(productService, baseAssetId, counterAssetId, makerFee, takerFee);
        final Product product = productService.getProduct("GBPUSD");

        riskEngineHelper.updateBalance(userId, counterAssetId, 1000000L);
        riskEngineHelper.handleOrder(
            userId,
            product,
            OrderType.MKT,
            Side.BID,
            -1L,
            500000L
        );

        assertThat(riskEngine.balances.get(userId).get(counterAssetId)).isEqualTo(0L);
    }

    @Test
    public void shouldHandleMarketAskOrder()
    {
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        long counterAssetId = ProductServiceTestUtils
            .addAsset(productService, "USD", (byte) 2, riskEngine::addAsset).getAssetId();
        long makerFee = -10L;
        long takerFee = 25L;

        ProductServiceTestUtils.addProduct(productService, baseAssetId, counterAssetId, makerFee, takerFee);
        final Product product = productService.getProduct("GBPUSD");

        riskEngineHelper.updateBalance(userId, baseAssetId, 1000000L);
        riskEngineHelper.handleOrder(
            userId,
            product,
            OrderType.MKT,
            Side.ASK,
            -1L,
            500000L
        );

        assertThat(riskEngine.balances.get(userId).get(baseAssetId)).isEqualTo(500000L);
    }

    @Test
    public void shouldHandleFullBalanceMarketAskOrder()
    {
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        long counterAssetId = ProductServiceTestUtils
            .addAsset(productService, "USD", (byte) 2, riskEngine::addAsset).getAssetId();
        long makerFee = -10L;
        long takerFee = 25L;

        ProductServiceTestUtils.addProduct(productService, baseAssetId, counterAssetId, makerFee, takerFee);
        final Product product = productService.getProduct("GBPUSD");

        riskEngineHelper.updateBalance(userId, baseAssetId, 1000000L);
        riskEngineHelper.handleOrder(
            userId,
            product,
            OrderType.MKT,
            Side.ASK,
            -1L,
            1000000L
        );

        assertThat(riskEngine.balances.get(userId).get(baseAssetId)).isEqualTo(0L);
    }

    @Test
    public void shouldHandleInvalidMarketAskOrder()
    {
        long userId = riskEngineHelper.addUser();
        long baseAssetId = ProductServiceTestUtils.addAsset(productService, "GBP", (byte) 2, riskEngine::addAsset)
            .getAssetId();
        long counterAssetId = ProductServiceTestUtils
            .addAsset(productService, "USD", (byte) 2, riskEngine::addAsset).getAssetId();
        long makerFee = -10L;
        long takerFee = 25L;

        ProductServiceTestUtils.addProduct(productService, baseAssetId, counterAssetId, makerFee, takerFee);
        final Product product = productService.getProduct("GBPUSD");

        riskEngineHelper.updateBalance(userId, baseAssetId, 1000000L);
        long reservedBalance = riskEngineHelper.handleOrder(
            userId,
            product,
            OrderType.MKT,
            Side.ASK,
            -1L,
            1000001L
        );

        assertThat(reservedBalance).isEqualTo(Long.MIN_VALUE);
        assertThat(riskEngine.balances.get(userId).get(baseAssetId)).isEqualTo(1000000L);
    }
}
