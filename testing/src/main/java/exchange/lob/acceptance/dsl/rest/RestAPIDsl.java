package exchange.lob.acceptance.dsl.rest;

import com.lmax.simpledsl.DslParams;
import com.lmax.simpledsl.OptionalParam;
import com.lmax.simpledsl.RepeatingParamGroup;
import com.lmax.simpledsl.RequiredParam;
import exchange.lob.acceptance.TestStorage;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static exchange.lob.Assertions.assertEventually;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RestAPIDsl
{
    private final Map<String, String> authorisedUserPasswords = new HashMap<>();

    private final RestAPIDriver restAPIDriver = new RestAPIDriver("http://localhost:8081");
    private final TestStorage testStorage;

    public RestAPIDsl(final TestStorage testStorage)
    {
        this.testStorage = testStorage;
    }

    public void authUser(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("username"),
            new RequiredParam("password"),
            new OptionalParam("expectedStatusCode").setDefault("200"),
            new OptionalParam("expectedBody").setDefault("OK")
        );

        final String username = params.value("username");
        final String actualUsername = testStorage.getSystemUsername(username);
        final String password = params.value("password");
        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");
        final String expectedBody = params.value("expectedBody");

        final HttpResponse<String> response = restAPIDriver.sendAuthRequest(actualUsername, password);

        assertEquals(expectedStatusCode, response.statusCode());
        assertEquals(expectedBody, response.body());

        if (expectedStatusCode == 200)
        {
            authorisedUserPasswords.put(actualUsername, password);
        }
    }

    public void healthcheck(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("expectedStatusCode"),
            new RequiredParam("expectedBody")
        );

        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");
        final String expectedBody = params.value("expectedBody");

        final HttpResponse<String> response = restAPIDriver.sendHealthcheckRequest();

        assertEquals(expectedStatusCode, response.statusCode());
        assertEquals(expectedBody, response.body());
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

        final String username = params.value("username");
        final String actualUsername = testStorage.getSystemUsername(username);
        final String password = authorisedUserPasswords.get(actualUsername);

        if (password == null)
        {
            fail(actualUsername + " is not authorised. Call authUser first.");
        }

        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");

        final Map<String, Double> expectedBalances = Arrays.stream(params.valuesAsGroup("asset"))
            .collect(Collectors.toMap(
                group -> testStorage.getSystemAsset(group.value("asset")),
                group -> group.valueAsDouble("amount")
            ));

        final Map<String, Double> actualBalances = restAPIDriver.sendGetBalancesRequest(actualUsername, password, expectedStatusCode);

        // by default, users have 0.0 balance in every new asset,
        // so we need to compare them on this ad-hoc basis
        assertEventually(() -> assertThat(expectedBalances).allSatisfy((symbol, expectedBalance) -> {
            final double actualBalance = actualBalances.get(symbol);
            assertEquals(expectedBalance, actualBalance);
        }));
    }

    public void verifyTradeableProducts(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("username"),
            new RepeatingParamGroup(new RequiredParam("product")),
            new OptionalParam("expectedStatusCode").setDefault("200")
        );

        final String username = params.value("username");
        final String actualUsername = testStorage.getSystemUsername(username);
        final String password = authorisedUserPasswords.get(actualUsername);

        if (password == null)
        {
            fail(actualUsername + " is not authorised. Call authUser first.");
        }

        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");

        final List<String> expectedProducts = Arrays.stream(params.valuesAsGroup("product"))
            .map(group -> testStorage.getSystemProduct(group.value("product")))
            .collect(toList());

        final List<String> actualProducts = restAPIDriver.sendGetProductsRequest(actualUsername, password, expectedStatusCode);

        assertThat(actualProducts).containsAll(expectedProducts);
    }

    public void placeOrder(final String... args)
    {
        final DslParams params = new DslParams(
            args,
            new RequiredParam("username"),
            new RequiredParam("product"),
            new RequiredParam("side"),
            new RequiredParam("price"),
            new RequiredParam("amount"),
            new OptionalParam("expectedOrderStatus").setDefault("FILLED"),
            new OptionalParam("expectedStatusCode").setDefault("200")
        );

        final String username = params.value("username");
        final String actualUsername = testStorage.getSystemUsername(username);
        final String password = authorisedUserPasswords.get(actualUsername);

        if (password == null)
        {
            fail(actualUsername + " is not authorised. Call authUser first.");
        }

        final String product = testStorage.getSystemProduct(params.value("product"));
        final String side = params.value("side");
        final String price = params.value("price");
        final String amount = params.value("amount");
        final int expectedStatusCode = params.valueAsInt("expectedStatusCode");

        final HttpResponse<String> response = restAPIDriver.placeOrder(actualUsername, password, product, side, price, amount);

        assertEquals(expectedStatusCode, response.statusCode());
        assertEquals(params.value("expectedOrderStatus"), response.body());
    }
}
