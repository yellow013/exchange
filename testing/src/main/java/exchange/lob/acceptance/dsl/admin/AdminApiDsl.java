package exchange.lob.acceptance.dsl.admin;

import com.lmax.simpledsl.*;
import exchange.lob.acceptance.TestStorage;
import exchange.lob.admin.dto.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static exchange.lob.Assertions.assertEventually;
import static exchange.lob.Assertions.assertReflectiveContainsAll;
import static exchange.lob.acceptance.TestStorage.generateSystemSymbolFor;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class AdminApiDsl
{
    public static final String LOCALHOST = "localhost";
    public static final int PORT = 8080;
    private final TestStorage testStorage;
    private final AdminApiDriver adminApiDriver;

    public AdminApiDsl(final TestStorage testStorage)
    {
        this.testStorage = testStorage;
        this.adminApiDriver = new AdminApiDriver(LOCALHOST, PORT, testStorage);
    }

    public void addAsset(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("symbol"),
            new RequiredParam("scale"),
            new OptionalParam("expectedStatusCode").setDefault("200"),
            new OptionalParam("expectedErrorMessage")
        );

        final String symbol = params.value("symbol");
        final boolean isLiteralSymbol = symbol.startsWith("<") && symbol.endsWith(">");
        final String systemAssetSymbol = isLiteralSymbol ? symbol.substring(1, symbol.length() - 1) : generateSystemSymbolFor(symbol);
        final byte scale = Byte.parseByte(params.value("scale"));
        testStorage.storeAsset(symbol, systemAssetSymbol, scale);
        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");

        adminApiDriver.sendAddAssetRequest(
            systemAssetSymbol,
            scale,
            expectedStatusCode,
            params.valueAsOptional("expectedErrorMessage")
        );
    }

    public void verifyAssets(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RepeatingParamGroup(
                new RequiredParam("symbol"),
                new RequiredParam("scale")
            ),
            new OptionalParam("ignoreId").setDefault("false"),
            new OptionalParam("expectedStatusCode").setDefault("200")
        );

        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");
        final boolean ignoreId = params.valueAsBoolean("ignoreId");
        final GetAssetsResponse response = adminApiDriver.sendGetAssetsRequest(expectedStatusCode);
        final List<GetAssetsResponse.Asset> expectedAssets = parseAssets(params, ignoreId);

        assertReflectiveContainsAll(expectedAssets, response.assets, ignoreId ? new String[]{ "assetId" } : new String[0]);
    }

    private List<GetAssetsResponse.Asset> parseAssets(final DslParams params, final boolean ignoreId)
    {
        return Arrays.stream(params.valuesAsGroup("symbol"))
            .map(group -> {
                final String systemAssetSymbol = testStorage.getSystemAsset(group.value("symbol"));
                return new GetAssetsResponse.Asset(
                    ignoreId ? Long.MIN_VALUE : testStorage.getAssetId(systemAssetSymbol),
                    systemAssetSymbol,
                    Byte.parseByte(group.value("scale"))
                );
            })
            .collect(toList());
    }

    public void addProduct(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("baseAsset"),
            new RequiredParam("counterAsset"),
            new RequiredParam("makerFee"),
            new RequiredParam("takerFee"),
            new OptionalParam("expectedStatusCode").setDefault("200"),
            new OptionalParam("expectedErrorMessage")
        );

        final String baseAsset = params.value("baseAsset");
        final String counterAsset = params.value("counterAsset");
        final long makerFee = params.valueAsLong("makerFee");
        final long takerFee = params.valueAsLong("takerFee");
        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");

        final String actualBaseAsset = maybeGetLiteralAsset(baseAsset);
        final String actualCounterAsset = maybeGetLiteralAsset(counterAsset);

        final long actualBaseAssetId = testStorage.getAssetId(actualBaseAsset);
        final long actualCounterAssetId = testStorage.getAssetId(actualCounterAsset);

        final String systemProductSymbol = actualBaseAsset + actualCounterAsset;

        testStorage.storeProduct(parseAsset(baseAsset) + parseAsset(counterAsset), systemProductSymbol);

        adminApiDriver.sendAddProductRequest(
            systemProductSymbol,
            actualBaseAssetId,
            actualCounterAssetId,
            makerFee,
            takerFee,
            expectedStatusCode,
            params.valueAsOptional("expectedErrorMessage")
        );
    }

    private String parseAsset(final String asset)
    {
        return isLiteral(asset) ? asset.substring(1, asset.length() - 1) : asset;
    }

    public void updateBalance(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("username"),
            new RequiredParam("asset"),
            new RequiredParam("amount"),
            new OptionalParam("expectedStatusCode").setDefault("200")
        );

        final long userId = testStorage.getUserIdByAlias(params.value("username"));
        final String asset = maybeGetLiteralAsset(params.value("asset"));
        final double amount = params.valueAsDouble("amount");
        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");

        adminApiDriver.sendUpdateBalanceRequest(userId, asset, amount, expectedStatusCode);
    }

    private List<GetProductsResponse.Product> parseProducts(final RepeatingGroup[] products)
    {
        return Arrays.stream(products)
            .map(repeatingGroup -> {
                final String product = testStorage.getSystemProduct(repeatingGroup.value("product"));
                return new GetProductsResponse.Product(
                    testStorage.getProductId(product),
                    product,
                    Byte.parseByte(repeatingGroup.value("baseScale")),
                    Byte.parseByte(repeatingGroup.value("counterScale")),
                    repeatingGroup.valueAsLong("makerFee"),
                    repeatingGroup.valueAsLong("takerFee")
                );
            })
            .collect(toList());
    }

    public void addUser(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("username"),
            new RequiredParam("password"),
            new OptionalParam("expectedStatusCode").setDefault("200"),
            new OptionalParam("expectedErrorMessage")
        );

        final String systemUsername = testStorage.storeUser(params.value("username"));
        final String password = params.value("password");
        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");

        adminApiDriver.sendAddUserRequest(systemUsername, password, expectedStatusCode, params.valueAsOptional("expectedErrorMessage"));
    }

    public void verifyProducts(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RepeatingParamGroup(
                new RequiredParam("product"),
                new RequiredParam("baseScale"),
                new RequiredParam("counterScale"),
                new RequiredParam("makerFee"),
                new RequiredParam("takerFee")
            ),
            new OptionalParam("expectedStatusCode").setDefault("200")
        );

        final RepeatingGroup[] products = params.valuesAsGroup("product");
        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");
        final List<GetProductsResponse.Product> expectedProducts = parseProducts(products);

        final GetProductsResponse response = adminApiDriver.sendGetProductsRequest(expectedStatusCode);

        assertReflectiveContainsAll(expectedProducts, response.products);
    }

    public void verifyUsers(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RepeatingParamGroup(
                new RequiredParam("username")
            ),
            new OptionalParam("expectedStatusCode").setDefault("200")
        );

        final RepeatingGroup[] users = params.valuesAsGroup("username");
        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");
        final List<GetUsersResponse.User> expectedUsers = parseUsers(users);

        final GetUsersResponse response = adminApiDriver.sendGetUsersRequest(expectedStatusCode);

        assertReflectiveContainsAll(expectedUsers, response.users);
    }

    public void verifyBalances(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("username"),
            new RepeatingParamGroup(
                new RequiredParam("asset"),
                new RequiredParam("amount")
            ),
            new OptionalParam("expectedStatusCode").setDefault("200")
        );

        final long userId = testStorage.getUserIdByAlias(params.value("username"));
        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");

        final Map<String, Double> expectedUserBalances = Arrays.stream(params.valuesAsGroup("asset"))
            .collect(toMap(
                group -> {
                    final String assetSymbol = group.value("asset");
                    return maybeGetLiteralAsset(assetSymbol);
                },
                group -> group.valueAsDouble("amount")
            ));

        final Set<Map.Entry<String, Double>> actualUserBalances = adminApiDriver
            .sendGetUserBalancesRequest(userId, expectedStatusCode)
            .balances
            .entrySet();

        // FIXME: assertReflectiveContainsAll is an artifact of retaining asset symbols between tests - this does not reflect the actual usage of the system
        assertEventually(() -> assertReflectiveContainsAll(
            expectedUserBalances.entrySet(),
            actualUserBalances
        ));
    }

    private String maybeGetLiteralAsset(final String assetSymbol)
    {
        return isLiteral(assetSymbol) ?
            assetSymbol.substring(1, assetSymbol.length() - 1) :
            testStorage.getSystemAsset(assetSymbol);
    }

    private boolean isLiteral(final String value)
    {
        return value.startsWith("<") && value.endsWith(">");
    }

    private List<GetUsersResponse.User> parseUsers(final RepeatingGroup[] users)
    {
        return Arrays.stream(users)
            .map(repeatingGroup -> {
                final String username = testStorage.getSystemUsername(repeatingGroup.value("username"));
                return new GetUsersResponse.User(testStorage.getUserId(username), username);
            })
            .collect(toList());
    }
}
