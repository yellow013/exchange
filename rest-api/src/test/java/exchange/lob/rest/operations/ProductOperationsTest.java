package exchange.lob.rest.operations;

import exchange.lob.acceptance.AcceptanceTestCase;
import org.junit.jupiter.api.Test;

public class ProductOperationsTest extends AcceptanceTestCase
{

    @Test
    public void shouldRetrieveTradeableProducts()
    {
        admin.addUser("username: trader", "password: strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addAsset("symbol: GBP", "scale: 2");

        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 20");
        admin.addProduct("baseAsset: GBP", "counterAsset: USD", "makerFee: -10", "takerFee: 20");

        restAPI.authUser("username: trader", "password: strongPassword");
        restAPI.verifyTradeableProducts("username: trader", "product: BTCUSD", "product: GBPUSD");
    }
}