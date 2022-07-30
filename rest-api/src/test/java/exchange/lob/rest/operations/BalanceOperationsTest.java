package exchange.lob.rest.operations;

import exchange.lob.acceptance.AcceptanceTestCase;
import org.junit.jupiter.api.Test;

public class BalanceOperationsTest extends AcceptanceTestCase
{

    @Test
    public void shouldRetrieveBalancesForUser()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.updateBalance("username: trader", "asset: BTC", "amount: 1");
        admin.updateBalance("username: trader", "asset: USD", "amount: 1000");

        restAPI.authUser("username: trader", "password: strongPassword");
        restAPI.verifyBalances(
            "username: trader",
            "asset: BTC", "amount: 1",
            "asset: USD", "amount: 1000"
        );
    }
}