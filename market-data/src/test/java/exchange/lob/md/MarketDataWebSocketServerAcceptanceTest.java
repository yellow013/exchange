package exchange.lob.md;

import exchange.lob.acceptance.AcceptanceTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MarketDataWebSocketServerAcceptanceTest extends AcceptanceTestCase
{

    @BeforeEach
    public void setUp()
    {
        super.setUp();

        mdws.connect();

        admin.addUser("username: maker", "password: strongPassword");
        admin.addUser("username: taker", "password: strongPassword");

        fix.login("maker", "strongPassword");
        fix.login("taker", "strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");

        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 20");

        admin.updateBalance("username: taker", "asset: USD", "amount: 1000");
        admin.updateBalance("username: maker", "asset: BTC", "amount: 5");
    }

    @Test
    public void shouldBroadcastPlaceOrderUpdate()
    {
        fix("taker").placeOrder(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "side: Buy",
            "price: 100",
            "orderQty: 1",
            "ordType: Limit"
        );

        mdws.verifyPlaceOrderUpdateReceived("product: BTCUSD", "side: BID", "price: 100", "amount: 1");
        mdws.noMoreUpdates();
    }

    @Test
    public void shouldBroadcastCancelOrderUpdate()
    {
        fix("maker").placeOrder(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "side: Sell",
            "price: 100",
            "orderQty: 1",
            "ordType: Limit"
        );

        fix("maker").cancelOrder("clientOrderId: limitAsk", "product: BTCUSD", "orderQty: 1");

        mdws.verifyPlaceOrderUpdateReceived("product: BTCUSD", "side: ASK", "price: 100", "amount: 1");
        mdws.verifyCancelOrderUpdateReceived("product: BTCUSD", "side: ASK", "price: 100", "amount: 1");
        mdws.noMoreUpdates();
    }

    @Test
    public void shouldBroadcastTradeUpdate()
    {
        fix("maker").placeOrder(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "side: SELL",
            "price: 100",
            "orderQty: 1",
            "ordType: Limit"
        );

        fix("taker").placeOrder(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "side: Buy",
            "price: 100",
            "orderQty: 1",
            "ordType: Limit"
        );

        mdws.verifyPlaceOrderUpdateReceived("product: BTCUSD", "side: ASK", "price: 100", "amount: 1");
        mdws.verifyTradeUpdateReceived("product: BTCUSD", "takerSide: BID", "price: 100", "amount: 1");
        mdws.noMoreUpdates();
    }

    @Test
    public void shouldBroadcastTradeUpdateFollowedByPlaceOrderUpdateWhenLimitTakeOrderOverflows()
    {
        fix("maker").placeOrder(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "side: SELL",
            "price: 100",
            "orderQty: 1",
            "ordType: Limit"
        );

        fix("taker").placeOrder(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "side: Buy",
            "price: 100",
            "orderQty: 2",
            "ordType: Limit"
        );

        mdws.verifyPlaceOrderUpdateReceived("product: BTCUSD", "side: ASK", "price: 100", "amount: 1");
        mdws.verifyTradeUpdateReceived("product: BTCUSD", "takerSide: BID", "price: 100", "amount: 1");
        mdws.verifyPlaceOrderUpdateReceived("product: BTCUSD", "side: BID", "price: 100", "amount: 1");
        mdws.noMoreUpdates();
    }

    @Test
    public void shouldBroadcastUpdatesForNewlyAddedProduct()
    {
        admin.addAsset("symbol: GBP", "scale: 2");

        admin.addProduct("baseAsset: GBP", "counterAsset: USD", "makerFee: -10", "takerFee: 20");

        fix("taker").placeOrder(
            "clientOrderId: limitBid",
            "product: GBPUSD",
            "side: Buy",
            "price: 100",
            "orderQty: 1",
            "ordType: Limit"
        );

        mdws.verifyPlaceOrderUpdateReceived("product: GBPUSD", "side: BID", "price: 100", "amount: 1");
        mdws.noMoreUpdates();
    }
}