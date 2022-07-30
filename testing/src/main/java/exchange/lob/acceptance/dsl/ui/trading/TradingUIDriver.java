package exchange.lob.acceptance.dsl.ui.trading;

import com.codeborne.selenide.SelenideElement;

import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selenide.*;

public class TradingUIDriver
{

    private static final String DATA_TEST_ID = "data-test-id";
    private final SelenideElement usernameInput = $(byAttribute(DATA_TEST_ID, "username-input"));
    private final SelenideElement passwordInput = $(byAttribute(DATA_TEST_ID, "password-input"));
    private final SelenideElement loginButton = $(byAttribute(DATA_TEST_ID, "login-button"));
    private final SelenideElement loginError = $(byAttribute(DATA_TEST_ID, "login-error"));
    private final SelenideElement welcomeUser = $(byAttribute(DATA_TEST_ID, "welcome-user"));
    private final SelenideElement balances = $(byAttribute(DATA_TEST_ID, "balances"));
    private final SelenideElement products = $(byAttribute(DATA_TEST_ID, "products"));
    private final SelenideElement placeOrderButton = $(byAttribute(DATA_TEST_ID, "place"));
    private final SelenideElement priceInput = $(byAttribute(DATA_TEST_ID, "price"));
    private final SelenideElement amountInput = $(byAttribute(DATA_TEST_ID, "amount"));

    public void resetBrowser()
    {
        // FIXME maybe: this is an artifact of using local storage to store basic auth creds
        open("/");
        clearBrowserLocalStorage();
    }

    public void reloadCurrentPage()
    {
        refresh();
    }

    public void login(final String username, final String password)
    {
        open("/");
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        loginButton.pressEnter();
    }

    public void expectLoginError(final String expectedError)
    {
        loginError.should(exactText(expectedError));
    }

    public void expectLoginSuccessFor(final String username)
    {
        welcomeUser.should(exactText("Welcome, " + username));
    }

    public void verifyBalances(final Map<String, String> expectedBalances)
    {
        expectedBalances.forEach((asset, amount) -> balances.should(text(asset + ": " + amount)));
    }

    public void navigateToAccountPage()
    {
        open("/account");
    }

    public void verifyProducts(final List<String> expectedProducts)
    {
        expectedProducts.forEach(product -> products.should(text(product)));
    }

    public void selectProductToTrade(final String product)
    {
        final SelenideElement productElement = $(byAttribute(DATA_TEST_ID, "product-" + product));
        productElement.pressEnter();
    }

    public void placeOrder(final String side, final String price, final String amount)
    {
        switch (side)
        {
            case "BID" -> $(byAttribute(DATA_TEST_ID, "radio-bid")).click();
            case "ASK" -> $(byAttribute(DATA_TEST_ID, "radio-ask")).click();
            default -> throw new IllegalStateException("Unexpected value: " + side);
        }

        priceInput.sendKeys(price);
        amountInput.sendKeys(amount);
        placeOrderButton.pressEnter();
    }
}
