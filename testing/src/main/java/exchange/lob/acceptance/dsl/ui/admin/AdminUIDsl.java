package exchange.lob.acceptance.dsl.ui.admin;

import com.lmax.simpledsl.DslParams;
import com.lmax.simpledsl.RepeatingParamGroup;
import com.lmax.simpledsl.RequiredParam;
import exchange.lob.acceptance.TestStorage;

import java.util.Arrays;

import static com.codeborne.selenide.Selenide.open;
import static exchange.lob.acceptance.TestStorage.generateSystemSymbolFor;

public class AdminUIDsl
{
    private final AdminUIDriver driver = new AdminUIDriver();
    private final TestStorage testStorage;

    public AdminUIDsl(final TestStorage testStorage)
    {
        this.testStorage = testStorage;
    }

    public void addUser(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("username"),
            new RequiredParam("password")
        );

        open("/");

        final String username = testStorage.storeUser(params.value("username"));
        final String password = params.value("password");

        driver.addUser(username, password);
    }

    public void verifyUsers(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RepeatingParamGroup(
                new RequiredParam("username")
            )
        );

        Arrays.stream(params.valuesAsGroup("username")).forEach(group -> {
            final String username = testStorage.getSystemUsername(group.value("username"));
            driver.verifyUserPresent(username);
        });
    }

    public void addAsset(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("symbol"),
            new RequiredParam("scale")
        );

        open("/");

        final String symbol = params.value("symbol");
        final String systemSymbol = generateSystemSymbolFor(symbol);
        final byte scale = (byte)params.valueAsInt("scale");
        testStorage.storeAsset(symbol, systemSymbol, scale);
        driver.addAsset(systemSymbol, scale);
    }

    public void verifyAssets(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RepeatingParamGroup(
                new RequiredParam("symbol"),
                new RequiredParam("scale")
            )
        );

        Arrays.stream(params.valuesAsGroup("symbol")).forEach(group -> {
            final String symbol = testStorage.getSystemAsset(group.value("symbol"));
            final String scale = group.value("scale");
            driver.verifyAssetPresent(symbol, scale);
        });
    }

    public void addProduct(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("baseAsset"),
            new RequiredParam("counterAsset"),
            new RequiredParam("makerFee"),
            new RequiredParam("takerFee")
        );

        final String baseAsset = params.value("baseAsset");
        final String counterAsset = params.value("counterAsset");
        final String makerFee = params.value("makerFee");
        final String takerFee = params.value("takerFee");

        final String actualBaseAsset = testStorage.getSystemAsset(baseAsset);
        final String actualCounterAsset = testStorage.getSystemAsset(counterAsset);

        driver.addProduct(actualBaseAsset, actualCounterAsset, makerFee, takerFee);

        testStorage.storeProduct(baseAsset + counterAsset, actualBaseAsset + actualCounterAsset);
    }

    public void verifyProducts(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RepeatingParamGroup(
                new RequiredParam("symbol"),
                new RequiredParam("makerFee"),
                new RequiredParam("takerFee")
            )
        );

        Arrays.stream(params.valuesAsGroup("symbol")).forEach(group -> {
            final String symbol = testStorage.getSystemProduct(group.value("symbol"));
            final String makerFee = group.value("makerFee");
            final String takerFee = group.value("takerFee");
            driver.verifyProductPresent(symbol, makerFee, takerFee);
        });
    }

    public void updateBalance(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("username"),
            new RequiredParam("asset"),
            new RequiredParam("balance")
        );

        final String username = testStorage.getSystemUsername(params.value("username"));
        final String asset = testStorage.getSystemAsset(params.value("asset"));
        final String balance = params.value("balance");

        driver.updateBalance(username, asset, balance);
    }

    public void verifyUserBalance(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("asset"),
            new RequiredParam("balance")
        );

        final String asset = testStorage.getSystemAsset(params.value("asset"));
        final String balance = params.value("balance");

        driver.verifyBalance(asset, balance);
    }
}
