package exchange.lob.acceptance.dsl.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exchange.lob.acceptance.TestStorage;
import exchange.lob.admin.dto.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdminApiDriver
{
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;
    private final TestStorage testStorage;

    public AdminApiDriver(final String hostname, final int port, final TestStorage testStorage)
    {
        this.baseUrl = "http://" + hostname + ":" + port;
        this.httpClient = HttpClient.newHttpClient();
        this.testStorage = testStorage;
    }

    private HttpResponse<String> sendRequest(final String method, final String path, final Object body)
    {
        try
        {
            final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void sendAddAssetRequest(
        final String systemAssetSymbol,
        final byte scale,
        final int expectedStatusCode,
        final Optional<String> expectedErrorMessage
    )
    {
        final AddAssetRequest request = new AddAssetRequest(systemAssetSymbol, scale);
        final HttpResponse<String> httpResponse = sendRequest("POST", "/asset", request);
        assertEquals(expectedStatusCode, httpResponse.statusCode());

        if (expectedErrorMessage.isPresent())
        {
            final ErrorResponse errorResponse = readErrorValue(httpResponse);
            assertEquals(expectedErrorMessage.get(), errorResponse.error);
            return;
        }

        final long assetId = readValue(httpResponse, AddAssetResponse.class).assetId;
        testStorage.storeAssetId(systemAssetSymbol, assetId);
    }

    public void sendAddProductRequest(
        final String systemProductSymbol,
        final long baseAssetId,
        final long counterAssetId,
        final long makerFee,
        final long takerFee,
        final int expectedStatusCode,
        final Optional<String> expectedErrorMessage
    )
    {
        final AddProductRequest request = new AddProductRequest(baseAssetId, counterAssetId, makerFee, takerFee);
        final HttpResponse<String> httpResponse = sendRequest("POST", "/product", request);

        if (expectedErrorMessage.isPresent())
        {
            final ErrorResponse errorResponse = readErrorValue(httpResponse);
            assertEquals(expectedErrorMessage.get(), errorResponse.error);
            return;
        }

        assertEquals(expectedStatusCode, httpResponse.statusCode());
        final long productId = readValue(httpResponse, AddProductResponse.class).productId;
        testStorage.storeProductId(systemProductSymbol, productId);
    }

    public void sendUpdateBalanceRequest(final long userId, final String asset, final double amount, final int expectedStatusCode)
    {
        final UpdateBalanceRequest request = new UpdateBalanceRequest(amount);
        final String path = "/" + userId + "/" + asset + "/balance";
        final HttpResponse<String> httpResponse = sendRequest("PUT", path, request);
        assertEquals(expectedStatusCode, httpResponse.statusCode());
    }

    public void sendAddUserRequest(
        final String username,
        final String password,
        final int expectedStatusCode,
        final Optional<String> expectedErrorMessage
    )
    {
        final AddUserRequest request = new AddUserRequest(username, password);
        final HttpResponse<String> httpResponse = sendRequest("POST", "/user", request);
        assertEquals(expectedStatusCode, httpResponse.statusCode());

        if (expectedErrorMessage.isPresent())
        {
            final ErrorResponse errorResponse = readErrorValue(httpResponse);
            assertEquals(expectedErrorMessage.get(), errorResponse.error);
            return;
        }

        final long userId = readValue(httpResponse, AddUserResponse.class).userId;
        testStorage.storeUserId(username, userId);
    }

    private ErrorResponse readErrorValue(final HttpResponse<String> httpResponse)
    {
        try
        {
            return new ObjectMapper().readValue(httpResponse.body(), ErrorResponse.class);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public GetProductsResponse sendGetProductsRequest(final int expectedStatusCode)
    {
        final HttpResponse<String> httpResponse = sendRequest("GET", "/products", null);
        assertEquals(expectedStatusCode, httpResponse.statusCode());
        return readValue(httpResponse, GetProductsResponse.class);
    }

    public GetUsersResponse sendGetUsersRequest(final int expectedStatusCode)
    {
        final HttpResponse<String> httpResponse = sendRequest("GET", "/users", null);
        assertEquals(expectedStatusCode, httpResponse.statusCode());
        return readValue(httpResponse, GetUsersResponse.class);
    }

    public GetAssetsResponse sendGetAssetsRequest(final int expectedStatusCode)
    {
        final HttpResponse<String> httpResponse = sendRequest("GET", "/assets", null);
        assertEquals(expectedStatusCode, httpResponse.statusCode());
        return readValue(httpResponse, GetAssetsResponse.class);
    }

    public GetUserBalancesResponse sendGetUserBalancesRequest(final long userId, final int expectedStatusCode)
    {
        final HttpResponse<String> httpResponse = sendRequest("GET", "/" + userId + "/balances", null);
        assertEquals(expectedStatusCode, httpResponse.statusCode());
        return readValue(httpResponse, GetUserBalancesResponse.class);
    }

    private <T> T readValue(final HttpResponse<String> httpResponse, final Class<T> responseType)
    {
        try
        {
            return objectMapper.readValue(httpResponse.body(), responseType);
        }
        catch (final JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
