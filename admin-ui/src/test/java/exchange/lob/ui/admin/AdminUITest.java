package exchange.lob.ui.admin;

import com.codeborne.selenide.Configuration;
import exchange.lob.acceptance.AcceptanceTestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class AdminUITest extends AcceptanceTestCase
{

    @BeforeAll
    static void beforeAll()
    {
        Configuration.browser = "firefox";
        Configuration.headless = true;
        Configuration.baseUrl = "http://localhost:3001";
        Configuration.pageLoadTimeout = TimeUnit.MINUTES.toMillis(2);
    }

    @Test
    public void shouldBeAbleToAddUser()
    {
        adminUI.addUser("username: trader", "password: strongPassword");
        adminUI.verifyUsers("username: trader");
    }

    @Test
    public void shouldBeAbleToAddAsset()
    {
        adminUI.addAsset("symbol: BTC", "scale: 8");
        adminUI.verifyAssets("symbol: BTC", "scale: 8");
    }

    @Test
    public void shouldBeAbleToAddProduct()
    {
        adminUI.addAsset("symbol: BTC", "scale: 8");
        adminUI.addAsset("symbol: USD", "scale: 2");

        adminUI.addProduct("baseAsset: BTC", "counterAsset: USD", "makerFee: 20", "takerFee: 40");
        adminUI.verifyProducts("symbol: BTCUSD", "makerFee: 20", "takerFee: 40");
    }

    @Test
    public void shouldBeAbleToUpdateUserBalance()
    {
        adminUI.addUser("username: trader", "password: strongPassword");
        adminUI.addAsset("symbol: BTC", "scale: 8");
        adminUI.updateBalance("username: trader", "asset: BTC", "balance: 1000");
        adminUI.verifyUserBalance("asset: BTC", "balance: 1000");
    }
}
