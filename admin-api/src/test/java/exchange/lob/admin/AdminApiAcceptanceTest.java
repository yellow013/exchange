package exchange.lob.admin;

import exchange.lob.acceptance.AcceptanceTestCase;
import org.junit.jupiter.api.Test;

public class AdminApiAcceptanceTest extends AcceptanceTestCase
{

    @Test
    public void shouldAddUser()
    {
        admin.addUser("username: trader", "password: strongPassword", "expectedStatusCode: 200");
    }

    @Test
    public void shouldGetAnErrorWhenAddingUserWithExistingUsername()
    {
        admin.addUser("username: trader", "password: strongPassword", "expectedStatusCode: 200");
        admin.addUser("username: trader", "password: strongPassword", "expectedStatusCode: 400", "expectedErrorMessage: invalid username provided");
    }

    @Test
    public void shouldGetAnErrorWhenAddingUserWithBlankUsername()
    {
        admin.addUser("username: trader", "password: strongPassword", "expectedStatusCode: 200");
        admin.addUser("username: ", "password: strongPassword", "expectedStatusCode: 400", "expectedErrorMessage: invalid username provided");
    }

    @Test
    public void shouldRetrieveUsers()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addUser("username: maker", "password: strongPassword");
        admin.addUser("username: taker", "password: strongPassword");
        admin.addUser("username: taker1", "password: strongPassword");
        admin.addUser("username: taker2", "password: strongPassword");
        admin.addUser("username: taker3", "password: strongPassword");

        admin.verifyUsers(
            "username: trader",
            "username: maker",
            "username: taker",
            "username: taker1",
            "username: taker2",
            "username: taker3",
            "expectedStatusCode: 200"
        );
    }

    @Test
    public void shouldAddAsset()
    {
        admin.addAsset("symbol: BTC", "scale: 8", "expectedStatusCode: 200");
    }

    @Test
    public void shouldNotAllowAddingAssetsOfSameSymbols()
    {
        admin.addAsset("symbol: <BTC>", "scale: 8", "expectedStatusCode: 200");
        admin.addAsset("symbol: <BTC>", "scale: 8", "expectedStatusCode: 400", "expectedErrorMessage: invalid asset symbol provided");
    }

    @Test
    public void shouldRetrieveAssets()
    {
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: ETH", "scale: 8");
        admin.verifyAssets(
            "symbol: BTC", "scale: 8",
            "symbol: ETH", "scale: 8"
        );
    }

    @Test
    public void shouldAddProduct()
    {
        admin.addAsset("symbol: BTC", "scale: 8", "expectedStatusCode: 200");
        admin.addAsset("symbol: USD", "scale: 2", "expectedStatusCode: 200");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10", "expectedStatusCode: 200");
    }

    @Test
    public void shouldRetrieveProducts()
    {
        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addAsset("symbol: GBP", "scale: 2");

        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 10");
        admin.addProduct("baseAsset: GBP", "counterAsset: USD", "makerFee: -20", "takerFee: 20");

        admin.verifyProducts(
            "product: BTCUSD", "baseScale: 8", "counterScale: 2", "makerFee: -10", "takerFee: 10",
            "product: GBPUSD", "baseScale: 2", "counterScale: 2", "makerFee: -20", "takerFee: 20"
        );
    }

    @Test
    public void shouldNotAddInvalidProduct()
    {
        admin.addAsset("symbol: BTC", "scale: 8");

        admin.addProduct(
            "baseAsset: BTC",
            "counterAsset: BTC",
            "makerFee: -10",
            "takerFee: 10",
            "expectedStatusCode: 400",
            "expectedErrorMessage: attempted to add invalid product"
        );

        admin.verifyProducts();
    }

    @Test
    public void shouldDeposit()
    {
        admin.addUser("username: trader", "password: strongPassword", "expectedStatusCode: 200");
        admin.addAsset("symbol: BTC", "scale: 8", "expectedStatusCode: 200");
        admin.updateBalance("username: trader", "asset: BTC", "amount: 1", "expectedStatusCode: 200");
        admin.verifyBalances("username: trader", "asset: BTC", "amount: 1", "expectedStatusCode: 200");
    }

    @Test
    public void shouldWithdraw()
    {
        admin.addUser("username: trader", "password: strongPassword");
        admin.addAsset("symbol: BTC", "scale: 8");

        admin.updateBalance("username: trader", "asset: BTC", "amount: 1");
        admin.updateBalance("username: trader", "asset: BTC", "amount: -1");

        admin.verifyBalances("username: trader", "asset: BTC", "amount: 0");
    }

    @Test
    public void shouldRetrieveUserBalances()
    {
        admin.addUser("username: trader", "password: strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");

        admin.updateBalance("username: trader", "asset: BTC", "amount: 1");
        admin.updateBalance("username: trader", "asset: USD", "amount: 1000");

        admin.verifyBalances(
            "username: trader",
            "asset: BTC", "amount: 1",
            "asset: USD", "amount: 1000"
        );
    }
}