package exchange.lob.fix;

import exchange.lob.acceptance.AcceptanceTestCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FixGatewayAcceptanceTest extends AcceptanceTestCase
{
    @Test
    public void shouldLogin()
    {
        admin.addUser("username: trader", "password: strongPassword");
        fix.login("trader", "strongPassword");
    }

    @Test
    public void shouldLogoutGracefully()
    {
        admin.addUser("username: trader", "password: strongPassword");
        fix.login("trader", "strongPassword");
        fix("trader").logout();
    }

    @Test
    public void shouldGetALogoutWhenUserNonExistent()
    {
        fix.login("trader", "strongPassword", "authenticationFailure: true");
    }

    @Test
    public void shouldGetALogoutWhenPasswordInvalid()
    {
        admin.addUser("username: trader", "password: strongPassword");
        fix.login("trader", "invalidPassword", "authenticationFailure: true");
    }

    @Test
    public void shouldPlaceLimitBidOrder()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");
        admin.updateBalance("username: trader", "asset: USD", "amount: 100000");

        fix.login("trader", "strongPassword");

        fix("trader").placeOrder(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Buy",
            "price: 10000.51",
            "orderQty: 1"
        );

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "ordStatus: New",
            "execType: New",
            "side: Buy",
            "price: 10000.51",
            "orderQty: 1"
        );

        fix("trader").noMoreExecutionReports();
    }

    @Test
    @Disabled
    public void shouldRejectLimitOrderWithoutPrice()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");
        admin.updateBalance("username: trader", "asset: USD", "amount: 100000");

        fix.login("trader", "strongPassword");

        fix("trader").placeOrder(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Buy",
            "orderQty: 1"
        );

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "ordStatus: New",
            "execType: New",
            "side: Buy",
            "price: 10000.51",
            "orderQty: 1"
        );

        fix("trader").noMoreExecutionReports();
    }

    @Test
    public void shouldPlaceLimitAskOrder()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");
        admin.updateBalance("username: trader", "asset: BTC", "amount: 1");

        fix.login("trader", "strongPassword");

        fix("trader").placeOrder(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Sell",
            "price: 10000.00",
            "orderQty: 1"
        );

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "ordStatus: New",
            "execType: New",
            "side: Sell",
            "price: 10000",
            "orderQty: 1"
        );

        fix("trader").noMoreExecutionReports();
    }

    @Test
    public void shouldMatchLimitOrdersAndReturnExecutionReports()
    {
        admin.addUser("username: maker", "password: strongPassword") ;
        admin.addUser("username: taker", "password: strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");

        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");

        admin.updateBalance("username: maker", "asset: BTC", "amount: 10");
        admin.updateBalance("username: taker", "asset: USD", "amount: 20000");

        fix.login("maker", "strongPassword");
        fix.login("taker", "strongPassword");

        fix("maker").placeOrder(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Sell",
            "price: 10000",
            "orderQty: 1"
        );

        fix("maker").verifyExecutionReport(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "ordStatus: New",
            "execType: New",
            "side: Sell",
            "price: 10000",
            "orderQty: 1"
        );

        fix("taker").placeOrder(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Buy",
            "price: 10000.00",
            "orderQty: 1"
        );

        fix("maker").verifyExecutionReport(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "ordStatus: Filled",
            "execType: Trade",
            "side: Sell",
            "price: 10000",
            "orderQty: 1"
        );

        fix("taker").verifyExecutionReport(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "ordStatus: Filled",
            "execType: Trade",
            "side: Buy",
            "price: 10000",
            "orderQty: 1"
        );

        fix("maker").noMoreExecutionReports();
        fix("taker").noMoreExecutionReports();
    }

    @Test
    public void shouldMatchLimitAndMarketOrderAndReturnExecutionReports()
    {
        admin.addUser("username: maker", "password: strongPassword");
        admin.addUser("username: taker", "password: strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");

        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");

        admin.updateBalance("username: maker", "asset: BTC", "amount: 10");
        admin.updateBalance("username: taker", "asset: USD", "amount: 20000");

        fix.login("maker", "strongPassword");
        fix.login("taker", "strongPassword");

        fix("maker").placeOrder("clientOrderId: limitAsk", "product: BTCUSD", "ordType: Limit", "side: Sell", "price: 10000.00", "orderQty: 1");

        fix("maker").verifyExecutionReport(
            "clientOrderId: limitAsk", "product: BTCUSD", "ordStatus: New", "execType: New", "side: Sell", "price: 10000", "orderQty: 1"
        );

        fix("taker").placeOrder("clientOrderId: mktBid", "product: BTCUSD", "ordType: Market", "side: BUY", "orderQty: 1");

        fix("maker").verifyExecutionReport(
            "clientOrderId: limitAsk", "product: BTCUSD", "ordStatus: Filled", "execType: Trade", "side: Sell", "price: 10000", "orderQty: 1"
        );

        fix("taker").verifyExecutionReport(
            "clientOrderId: mktBid", "product: BTCUSD", "ordStatus: Filled", "execType: Trade", "side: Buy", "price: 10000", "orderQty: 1"
        );

        fix("maker").noMoreExecutionReports();
        fix("taker").noMoreExecutionReports();
    }

    @Test
    public void shouldCancelOrderInFull()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");
        admin.updateBalance("username: trader", "asset: BTC", "amount: 1");

        fix.login("trader", "strongPassword");

        fix("trader").placeOrder(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Sell",
            "price: 10000.00",
            "orderQty: 1"
        );

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitAsk", "product: BTCUSD", "ordStatus: New", "execType: New", "side: Sell", "price: 10000", "orderQty: 1"
        );

        fix("trader").cancelOrder("clientOrderId: limitAsk", "product: BTCUSD", "orderQty: 1");

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitAsk", "product: BTCUSD", "ordStatus: Cancelled", "execType: Cancelled", "side: Sell", "price: 10000", "orderQty: 1"
        );

        fix("trader").noMoreExecutionReports();
    }

    @Test
    public void shouldCancelOrderPartially()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");
        admin.updateBalance("username: trader", "asset: BTC", "amount: 1");

        fix.login("trader", "strongPassword");

        fix("trader").placeOrder(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Sell",
            "price: 10000",
            "orderQty: 1"
        );

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitAsk", "product: BTCUSD", "ordStatus: New", "execType: New", "side: Sell", "price: 10000", "orderQty: 1"
        );

        fix("trader").cancelOrder("clientOrderId: limitAsk", "product: BTCUSD", "orderQty: 0.5");

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitAsk", "product: BTCUSD", "ordStatus: New", "execType: Cancelled", "side: Sell", "price: 10000", "orderQty: 0.5"
        );

        fix("trader").noMoreExecutionReports();
    }

    @Test
    public void shouldRejectWhenUserBalanceInsufficient()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");
        admin.updateBalance("username: trader", "asset: BTC", "amount: 1");

        fix.login("trader", "strongPassword");

        fix("trader").placeOrder(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Sell",
            "price: 10000",
            "orderQty: 2"
        );

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "ordStatus: Rejected",
            "execType: Rejected",
            "side: Sell",
            "rejectionReason: BROKER_OPTION",
            "text: INSUFFICIENT_BALANCE"
        );

        fix("trader").noMoreExecutionReports();
    }

    @Test
    public void shouldRejectMktOrderWhenLiquidityInsufficient()
    {
        admin.addUser("username: maker", "password: strongPassword");
        admin.addUser("username: taker", "password: strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");

        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");

        admin.updateBalance("username: maker", "asset: BTC", "amount: 10");
        admin.updateBalance("username: taker", "asset: USD", "amount: 20000");

        fix.login("maker", "strongPassword");
        fix.login("taker", "strongPassword");

        fix("maker").placeOrder("clientOrderId: limitAsk", "product: BTCUSD", "ordType: Limit", "side: Sell", "price: 10000", "orderQty: 1");

        fix("maker").verifyExecutionReport(
            "clientOrderId: limitAsk", "product: BTCUSD", "ordStatus: New", "execType: New", "side: Sell", "price: 10000", "orderQty: 1"
        );

        fix("taker").placeOrder("clientOrderId: mktBid", "product: BTCUSD", "ordType: Market", "side: Buy", "orderQty: 2");

        fix("taker").verifyExecutionReport(
            "clientOrderId: mktBid",
            "product: BTCUSD",
            "ordStatus: Rejected",
            "execType: Rejected",
            "side: Buy",
            "rejectionReason: BROKER_OPTION",
            "text: INSUFFICIENT_LIQUIDITY"
        );

        fix("maker").noMoreExecutionReports();
        fix("taker").noMoreExecutionReports();
    }

    @Test
    public void shouldRejectOrderForInvalidProduct()
    {
        admin.addUser("username: trader", "password: strongPassword");

        fix.login("trader", "strongPassword");

        fix("trader").placeOrder("clientOrderId: limitBid", "product: <LOLOMG>", "ordType: Limit", "side: Buy", "price: 10000", "orderQty: 1");

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitBid",
            "product: <LOLOMG>",
            "ordStatus: Rejected",
            "execType: Rejected",
            "side: Buy",
            "rejectionReason: UNKNOWN_SYMBOL"
        );

        fix("trader").noMoreExecutionReports();
    }

    @Test
    public void shouldRejectDuplicateOrder()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");
        admin.updateBalance("username: trader", "asset: USD", "amount: 200000");

        fix.login("trader", "strongPassword");

        fix("trader").placeOrder("clientOrderId: limitBid", "product: BTCUSD", "ordType: Limit", "side: Buy", "price: 10000", "orderQty: 1");
        fix("trader").placeOrder("clientOrderId: limitBid", "product: BTCUSD", "ordType: Limit", "side: Buy", "price: 20000", "orderQty: 1");

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "ordStatus: New",
            "execType: New",
            "side: Buy",
            "price: 10000",
            "orderQty: 1"
        );

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitBid",
            "product: BTCUSD",
            "ordStatus: Rejected",
            "execType: Rejected",
            "side: Buy",
            "rejectionReason: DUPLICATE_ORDER"
        );

        fix("trader").noMoreExecutionReports();
    }

    @Test
    public void shouldBeAbleToTradeTheProductAddedAfterFixSessionLoggedIn()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.updateBalance("username: trader", "asset: USD", "amount: 100000");

        fix.login("trader", "strongPassword");

        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");

        fix("trader").placeOrder("clientOrderId: limitBid", "product: BTCUSD", "ordType: Limit", "side: Buy", "price: 10000.5", "orderQty: 1");

        fix("trader").verifyExecutionReport(
            "clientOrderId: limitBid", "product: BTCUSD", "ordStatus: New", "execType: New", "side: Buy", "price: 10000.5", "orderQty: 1"
        );

        fix("trader").noMoreExecutionReports();
    }

    @Test
    public void shouldRejectUnknownOrderCancellationOnAnExistingProduct()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");

        fix.login("trader", "strongPassword");

        fix("trader").cancelOrder("clientOrderId: bogus", "product: BTCUSD", "orderQty: 1");

        fix("trader").verifyCancelReject(
            "clientOrderId: bogus",
            "rejectionReason: UNKNOWN_ORDER"
        );

        fix("trader").noMoreExecutionReports();
        fix("trader").noMoreCancelRejects();
    }

    @Test
    @Disabled
    public void shouldRejectNewOrderWithoutOrderQty()
    {

    }

    @Test
    @Disabled
    public void shouldRejectNewOrderWithoutSide()
    {

    }

    @Test
    @Disabled
    public void shouldRejectNewOrderWithoutProduct()
    {

    }

    @Test
    @Disabled
    public void shouldRejectNewOrderWithoutOrderType()
    {

    }

    @Test
    @Disabled
    public void shouldRejectNewOrderWithDuplicateClientOrderId()
    {

    }
}
