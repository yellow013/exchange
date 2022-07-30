package exchange.lob.ui.trading;

import com.codeborne.selenide.Configuration;
import exchange.lob.acceptance.AcceptanceTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class TradingUITest extends AcceptanceTestCase
{

    @BeforeAll
    static void beforeAll()
    {
        Configuration.browser = "firefox";
        Configuration.headless = true;
        Configuration.baseUrl = "http://localhost:3000";
        Configuration.pageLoadTimeout = TimeUnit.MINUTES.toMillis(2);
    }

    @Override
    @AfterEach
    public void tearDown()
    {
        tradingUI.resetBrowser();

        super.tearDown();
    }

    @Test
    public void shouldAllowUserToLogin()
    {
        admin.addUser("username: trader", "password: strongPassword");

        tradingUI.login("username: trader", "password: strongPassword");
    }

    @Test
    public void shouldNotAllowUserToLoginWithWrongPassword()
    {
        admin.addUser("username: trader", "password: strongPassword");

        tradingUI.login("username: trader", "password: wrongPassword", "expectedErrorMessage: Invalid Credentials!");
    }

    @Test
    public void shouldDisplayUserBalances()
    {
        admin.addUser("username: trader", "password: strongPassword1");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");

        admin.updateBalance("username: trader", "asset: BTC", "amount: 1.5");
        admin.updateBalance("username: trader", "asset: USD", "amount: 1000");

        tradingUI.login("username: trader", "password: strongPassword1");
        tradingUI.verifyBalances("asset: BTC", "amount: 1.5", "asset: USD", "amount: 1000");
    }

    @Test
    public void shouldDisplayZeroBalancesEvenIfNoDepositWasMade()
    {
        admin.addUser("username: trader", "password: strongPassword1");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");

        tradingUI.login("username: trader", "password: strongPassword1");
        tradingUI.verifyBalances("asset: BTC", "amount: 0", "asset: USD", "amount: 0");
    }

    @Test
    public void shouldKeepUserLoggedInAfterPageReload()
    {
        admin.addUser("username: trader", "password: strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");

        admin.updateBalance("username: trader", "asset: BTC", "amount: 1.5");
        admin.updateBalance("username: trader", "asset: USD", "amount: 1000");

        tradingUI.login("username: trader", "password: strongPassword");
        tradingUI.verifyBalances("asset: BTC", "amount: 1.5", "asset: USD", "amount: 1000");

        tradingUI.reloadCurrentPage();

        tradingUI.verifyBalances("asset: BTC", "amount: 1.5", "asset: USD", "amount: 1000");
    }

    @Test
    public void shouldDisplayTradeableProducts()
    {
        admin.addUser("username: trader", "password: strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addAsset("symbol: GBP", "scale: 2");

        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: -10", "takerFee: 20");
        admin.addProduct("baseAsset: GBP", "counterAsset: USD", "makerFee: -10", "takerFee: 20");

        tradingUI.login("username: trader", "password: strongPassword");
        tradingUI.verifyTradeableProducts("product: BTCUSD", "product: GBPUSD");
    }

    @Test
    public void shouldBeAbleToBuy()
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

        tradingUI.login("username: taker", "password: strongPassword");

        tradingUI.selectProductToTrade("product: BTCUSD");
        tradingUI.placeOrder("side: BID", "price: 100", "amount: 1");

        tradingUI.verifyBalances("asset: BTC", "amount: 1", "asset: USD", "amount: 0");
    }

    @Test
    public void shouldBeAbleToSell()
    {
        admin.addUser("username: maker", "password: strongPassword");
        admin.addUser("username: taker", "password: strongPassword");

        fix.login("maker", "strongPassword");

        admin.addAsset("symbol: BTC", "scale: 8");
        admin.addAsset("symbol: USD", "scale: 2");
        admin.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: 0", "takerFee: 0");

        admin.updateBalance("username: maker", "asset: USD", "amount: 100");
        admin.updateBalance("username: taker", "asset: BTC", "amount: 1");

        fix("maker").placeOrder(
            "clientOrderId: limitAsk",
            "product: BTCUSD",
            "ordType: Limit",
            "side: Buy",
            "price: 100",
            "orderQty: 1"
        );

        tradingUI.login("username: taker", "password: strongPassword");

        tradingUI.selectProductToTrade("product: BTCUSD");
        tradingUI.placeOrder("side: ASK", "price: 100", "amount: 1");

        tradingUI.verifyBalances("asset: BTC", "amount: 0", "asset: USD", "amount: 100");
    }
}
