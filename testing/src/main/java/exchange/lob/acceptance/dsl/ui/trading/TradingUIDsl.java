package exchange.lob.acceptance.dsl.ui.trading;

import com.lmax.simpledsl.DslParams;
import com.lmax.simpledsl.OptionalParam;
import com.lmax.simpledsl.RepeatingParamGroup;
import com.lmax.simpledsl.RequiredParam;
import exchange.lob.acceptance.TestStorage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TradingUIDsl
{

    private final TradingUIDriver tradingUIDriver = new TradingUIDriver();
    private final TestStorage testStorage;


    public TradingUIDsl(final TestStorage testStorage)
    {
        this.testStorage = testStorage;
    }

    public void reloadCurrentPage()
    {
        tradingUIDriver.reloadCurrentPage();
    }

    public void resetBrowser()
    {
        tradingUIDriver.resetBrowser();
    }

    public void login(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("username"),
            new RequiredParam("password"),
            new OptionalParam("expectedErrorMessage")
        );

        final String username = testStorage.getSystemUsername(params.value("username"));
        final String password = params.value("password");
        final Optional<String> expectedErrorMessage = params.valueAsOptional("expectedErrorMessage");

        tradingUIDriver.login(username, password);

        if (expectedErrorMessage.isPresent())
        {
            tradingUIDriver.expectLoginError(expectedErrorMessage.get());
        }
        else
        {
            tradingUIDriver.expectLoginSuccessFor(username);
        }
    }

    public void verifyBalances(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RepeatingParamGroup(
                new RequiredParam("asset"),
                new RequiredParam("amount")
            )
        );

        tradingUIDriver.navigateToAccountPage();

        final Map<String, String> expectedBalances = Arrays.stream(params.valuesAsGroup("asset"))
            .collect(Collectors.toMap(
                group -> testStorage.getSystemAsset(group.value("asset")),
                group -> group.value("amount")
            ));

        tradingUIDriver.verifyBalances(expectedBalances);
    }

    public void verifyTradeableProducts(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RepeatingParamGroup(new RequiredParam("product"))
        );

        final List<String> expectedProducts = Arrays.stream(params.valuesAsGroup("product"))
            .map(group -> testStorage.getSystemProduct(group.value("product")))
            .collect(Collectors.toList());

        tradingUIDriver.verifyProducts(expectedProducts);
    }

    public void selectProductToTrade(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("product")
        );

        final String product = params.value("product");

        tradingUIDriver.selectProductToTrade(testStorage.getSystemProduct(product));
    }

    public void placeOrder(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("side"),
            new RequiredParam("price"),
            new RequiredParam("amount")
        );

        final String side = params.value("side");
        final String price = params.value("price");
        final String amount = params.value("amount");

        tradingUIDriver.placeOrder(side, price, amount);
    }
}
