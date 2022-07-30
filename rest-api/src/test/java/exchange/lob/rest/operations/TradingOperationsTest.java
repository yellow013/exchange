package exchange.lob.rest.operations;

import exchange.lob.acceptance.AcceptanceTestCase;
import org.junit.jupiter.api.Test;

public class TradingOperationsTest extends AcceptanceTestCase
{

    @Test
    public void shouldBeAbleToTrade()
    {
        admin.addUser("username: maker", "password: strongPassword");
        admin.addUser("username: taker", "password: strongPassword");

        fix.login("maker", "strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: 0", "takerFee: 0");

        admin.updateBalance("username: taker", "asset: USD", "amount: 100");
        admin.updateBalance("username: maker", "asset: BTC", "amount: 1");

        fix("maker").placeOrder(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Sell",
            "price: 100",
            "orderQty: 1"
        );

        restAPI.authUser("username: taker", "password: strongPassword");

        restAPI.placeOrder("username: taker", "product: BTCUSD", "side: BID", "price: 100", "amount: 1");

        restAPI.verifyBalances(
            "username: taker",
            "asset: BTC", "amount: 1",
            "asset: USD", "amount: 0"
        );
    }

    @Test
    public void shouldBeAbleToTradeInSuccession()
    {
        admin.addUser("username: maker", "password: strongPassword");
        admin.addUser("username: taker", "password: strongPassword");

        fix.login("maker", "strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: 0", "takerFee: 0");

        admin.updateBalance("username: taker", "asset: USD", "amount: 100");
        admin.updateBalance("username: maker", "asset: BTC", "amount: 1");

        fix("maker").placeOrder(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Sell",
            "price: 100",
            "orderQty: 1"
        );

        restAPI.authUser("username: taker", "password: strongPassword");

        restAPI.placeOrder("username: taker", "product: BTCUSD", "side: BID", "price: 100", "amount: 0.5");
        restAPI.placeOrder("username: taker", "product: BTCUSD", "side: BID", "price: 100", "amount: 0.5");

        restAPI.verifyBalances(
            "username: taker",
            "asset: BTC", "amount: 1",
            "asset: USD", "amount: 0"
        );
    }
}