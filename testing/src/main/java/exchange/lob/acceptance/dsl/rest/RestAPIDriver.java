package exchange.lob.acceptance.dsl.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import exchange.lob.acceptance.dsl.http.TestRequestBuilder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RestAPIDriver
{
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RestAPIDriver(final String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    private HttpResponse<String> sendRequest(final HttpRequest request)
    {
        try
        {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<String> sendAuthRequest(final String username, final String password)
    {
        final HttpRequest request = new TestRequestBuilder()
            .GET()
            .uri(baseUrl + "/auth")
            .basicAuth(username, password)
            .build();

        return sendRequest(request);
    }

    public HttpResponse<String> sendHealthcheckRequest()
    {
        final HttpRequest request = new TestRequestBuilder()
            .GET()
            .uri(baseUrl + "/healthcheck")
            .build();

        return sendRequest(request);
    }

    public Map<String, Double> sendGetBalancesRequest(final String username, final String password, final int expectedStatusCode)
    {
        final HttpRequest request = new TestRequestBuilder()
            .GET()
            .uri(baseUrl + "/balances")
            .basicAuth(username, password)
            .build();

        final HttpResponse<String> response = sendRequest(request);
        assertEquals(expectedStatusCode, response.statusCode());

        return readValue(response.body(), Map.class);
    }

    public List<String> sendGetProductsRequest(final String username, final String password, final int expectedStatusCode)
    {
        final HttpRequest request = new TestRequestBuilder()
            .GET()
            .uri(baseUrl + "/products")
            .basicAuth(username, password)
            .build();

        final HttpResponse<String> response = sendRequest(request);
        assertEquals(expectedStatusCode, response.statusCode());

        return (List<String>) readValue(response.body(), Map.class).get("products");
    }

    public HttpResponse<String> placeOrder(
        final String username,
        final String password,
        final String product,
        final String side,
        final String price,
        final String amount
    )
    {
        final String body = writeValue(Map.of(
            "product", product,
            "side", side,
            "price", price,
            "amount", amount
        ));

        final HttpRequest request = new TestRequestBuilder()
            .POST(body)
            .uri(baseUrl + "/placeOrder")
            .basicAuth(username, password)
            .build();

        return sendRequest(request);
    }

    private <T> T readValue(final String value, final Class<T> clazz)
    {
        try
        {
            return objectMapper.readValue(value, clazz);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private String writeValue(final Object object)
    {
        try
        {
            return objectMapper.writeValueAsString(object);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
