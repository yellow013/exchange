package exchange.lob.acceptance.dsl.ui.admin;

import com.codeborne.selenide.SelenideElement;

import java.util.function.Function;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdminUIDriver
{

    private static final String DATA_TEST_ID = "data-test-id";
    private final SelenideElement usernameInput = $(byAttribute(DATA_TEST_ID, "username-input"));
    private final SelenideElement passwordInput = $(byAttribute(DATA_TEST_ID, "password-input"));
    private final SelenideElement addUserButton = $(byAttribute(DATA_TEST_ID, "add-user-button"));
    private final SelenideElement usersTable = $(byAttribute(DATA_TEST_ID, "users-table"));

    private final SelenideElement assetSymbolInput = $(byAttribute(DATA_TEST_ID, "symbol-input"));
    private final SelenideElement scaleInput = $(byAttribute(DATA_TEST_ID, "scale-input"));
    private final SelenideElement addAssetButton = $(byAttribute(DATA_TEST_ID, "add-asset-button"));
    private final SelenideElement assetsTable = $(byAttribute(DATA_TEST_ID, "assets-table"));

    private final SelenideElement baseAssetSelect = $(byAttribute(DATA_TEST_ID, "base-asset-select"));
    private final SelenideElement counterAssetSelect = $(byAttribute(DATA_TEST_ID, "counter-asset-select"));
    private final SelenideElement makerFeeInput = $(byAttribute(DATA_TEST_ID, "maker-fee-input"));
    private final SelenideElement takerFeeInput = $(byAttribute(DATA_TEST_ID, "taker-fee-input"));
    private final SelenideElement addProductButton = $(byAttribute(DATA_TEST_ID, "add-product-button"));
    private final Function<String, SelenideElement> productRowLocator = productSymbol ->
        $(byAttribute(DATA_TEST_ID, "product-row-" + productSymbol));

    private final Function<String, SelenideElement> userPageButtonLocator = username ->
        $(byAttribute(DATA_TEST_ID, "user-page-" + username));

    private final Function<String, SelenideElement> updateBalanceInputLocator = asset ->
        $(byAttribute(DATA_TEST_ID, "update-balance-input-" + asset));

    private final Function<String, SelenideElement> updateBalanceButtonLocator = asset ->
        $(byAttribute(DATA_TEST_ID, "update-balance-button-" + asset));

    private final Function<String, SelenideElement> balanceLocator = asset ->
        $(byAttribute(DATA_TEST_ID, "balance-" + asset));

    public void addUser(final String username, final String password)
    {
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        addUserButton.pressEnter();
    }

    public void verifyUserPresent(final String username)
    {
        usersTable.should(text(username));
    }

    public void addAsset(final String symbol, final short scale)
    {
        assetSymbolInput.sendKeys(symbol);
        scaleInput.sendKeys(Short.toString(scale));
        addAssetButton.pressEnter();
    }

    public void verifyAssetPresent(final String symbol, final String scale)
    {
        assetsTable.should(text(symbol));
        assetsTable.should(text(scale)); // TODO: do better - see products.
    }

    public void addProduct(final String baseAsset, final String counterAsset, final String makerFee, final String takerFee)
    {
        baseAssetSelect.selectOptionContainingText(baseAsset);
        counterAssetSelect.selectOptionContainingText(counterAsset);
        makerFeeInput.sendKeys(makerFee);
        takerFeeInput.sendKeys(takerFee);
        addProductButton.pressEnter();
    }

    public void verifyProductPresent(final String symbol, final String makerFee, final String takerFee)
    {
        productRowLocator.apply(symbol)
            .should(text(symbol))
            .should(text(makerFee))
            .should(text(takerFee));
    }

    public void updateBalance(final String username, final String asset, final String balance)
    {
        final SelenideElement userPageButton = userPageButtonLocator.apply(username);
        userPageButton.click();

        final SelenideElement updateBalanceInput = updateBalanceInputLocator.apply(asset);
        updateBalanceInput.sendKeys(balance);

        final SelenideElement updateBalanceButton = updateBalanceButtonLocator.apply(asset);
        updateBalanceButton.click();
    }

    public void verifyBalance(final String asset, final String balance)
    {
        final SelenideElement balanceBox = balanceLocator.apply(asset);
        assertEquals(balance, balanceBox.getOwnText());
    }
}
