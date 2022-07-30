package exchange.lob.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exchange.lob.admin.dto.*;
import exchange.lob.events.admin.AdminClient;
import exchange.lob.node.client.response.Balance;
import lob.exchange.config.admin.AdminConfig;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static spark.Spark.*;

public class AdminApi
{

    public static final String OK = "OK";
    private final AdminConfig adminConfig;
    private final AdminClient adminClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminApi(final AdminConfig adminConfig, final AdminClient adminClient)
    {
        this.adminConfig = adminConfig;
        this.adminClient = adminClient;
    }

    public void start()
    {
        registerRoutes();
    }

    private static void enableCORS()
    {
        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
        options("/*", (request, response) -> {
            final String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null)
            {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            final String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null)
            {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return OK;
        });
    }

    private void registerRoutes()
    {
        port(adminConfig.getPort());

        enableCORS();

        get("/healthcheck", (rq, re) -> OK);

        post("/user", this::addUser);
        get("/users", this::getUsers);

        post("/asset", this::addAsset);
        get("/assets", this::getAssets);

        post("/product", this::addProduct);
        get("/products", this::getProducts);

        put("/:userId/:asset/balance", this::updateBalance);
        get("/:userId/balances", this::getUserBalances);

        exception(Exception.class, this::handleError);
        awaitInitialization();
    }

    public void close()
    {
        stop();
    }

    private <T extends Exception> void handleError(T exception, Request request, Response response)
    {
        try
        {
            response.status(400);
            response.body(objectMapper.writeValueAsString(Map.of("error", exception.getMessage())));
        }
        catch (final JsonProcessingException e)
        {
            response.status(500);
        }
    }

    private Object addUser(final Request request, final Response response) throws Exception
    {
        final AddUserRequest addUserRequest = objectMapper.readValue(request.body(), AddUserRequest.class);

        final exchange.lob.node.client.response.AddUserResponse exchangeResponse = adminClient
            .addUser(addUserRequest.username, addUserRequest.password)
            .get(1, TimeUnit.SECONDS);

        return switch (exchangeResponse.code())
            {
                case SUCCESS -> objectMapper.writeValueAsString(new AddUserResponse(exchangeResponse.userId()));
                case INVALID_USERNAME -> {
                    final ErrorResponse errorResponse = new ErrorResponse("invalid username provided");
                    response.status(400);
                    yield objectMapper.writeValueAsString(errorResponse);
                }
                default -> throw new IllegalStateException("Unexpected value: " + exchangeResponse.code());
            };
    }

    private Object addAsset(final Request request, final Response response) throws Exception
    {
        final AddAssetRequest addAssetRequest = objectMapper.readValue(request.body(), AddAssetRequest.class);
        final exchange.lob.node.client.response.AddAssetResponse exchangeResponse = adminClient
            .addAsset(addAssetRequest.symbol, addAssetRequest.scale)
            .join();

        return switch (exchangeResponse.code())
            {
                case SUCCESS -> {
                    final AddAssetResponse addAssetResponse = new AddAssetResponse(exchangeResponse.assetId());
                    yield objectMapper.writeValueAsString(addAssetResponse);
                }
                case INVALID_ASSET -> {
                    final ErrorResponse errorResponse = new ErrorResponse("invalid asset symbol provided");
                    response.status(400);
                    yield objectMapper.writeValueAsString(errorResponse);
                }
                default -> throw new IllegalStateException("Unexpected value: " + exchangeResponse.code());
            };
    }

    private Object getUsers(final Request request, final Response response) throws Exception
    {
        return objectMapper.writeValueAsString(new GetUsersResponse(adminClient.fetchUsers().join().users().stream()
            .map(GetUsersResponse.User::toResponse)
            .collect(toList())));
    }

    private Object getAssets(final Request request, final Response response) throws Exception
    {
        return objectMapper.writeValueAsString(new GetAssetsResponse(adminClient.fetchAssets().join().assets().stream()
            .map(GetAssetsResponse.Asset::toResponse)
            .collect(toList())));
    }

    private Object addProduct(final Request request, final Response response) throws Exception
    {
        final AddProductRequest addProductRequest = objectMapper.readValue(request.body(), AddProductRequest.class);
        final exchange.lob.node.client.response.AddProductResponse exchangeResponse = adminClient.addProduct(
            addProductRequest.baseAssetId,
            addProductRequest.counterAssetId,
            addProductRequest.makerFee,
            addProductRequest.takerFee
        ).join();

        return switch (exchangeResponse.code())
            {
                case SUCCESS -> {
                    final AddProductResponse addProductResponse = new AddProductResponse(exchangeResponse.productId());
                    yield objectMapper.writeValueAsString(addProductResponse);
                }
                case INVALID_PRODUCT -> {
                    response.status(400);
                    final ErrorResponse errorResponse = new ErrorResponse("attempted to add invalid product");
                    yield objectMapper.writeValueAsString(errorResponse);
                }
                default -> throw new IllegalStateException("Unexpected value: " + exchangeResponse.code());
            };
    }

    private Object getProducts(final Request request, final Response response) throws Exception
    {
        final exchange.lob.node.client.response.GetProductsResponse exchangeResponse = adminClient.fetchProducts().join();
        final GetProductsResponse getProductsResponse = new GetProductsResponse(exchangeResponse.products().stream()
            .map(GetProductsResponse.Product::toResponse)
            .collect(toList()));

        return objectMapper.writeValueAsString(getProductsResponse);
    }

    private Object updateBalance(final Request request, final Response response) throws Exception
    {
        final UpdateBalanceRequest updateBalanceRequest = objectMapper.readValue(request.body(), UpdateBalanceRequest.class);
        final long userId = Long.parseLong(request.params("userId"));
        final String asset = request.params("asset");
        final double amount = updateBalanceRequest.amount;
        adminClient.updateBalance(userId, asset, amount).join();
        return OK;
    }

    private Object getUserBalances(final Request request, final Response response) throws Exception
    {
        final long userId = Long.parseLong(request.params("userId"));
        final List<Balance> balances = adminClient.fetchUserBalances(userId).get().balances();
        return objectMapper.writeValueAsString(new GetUserBalancesResponse(balances.stream()
            .collect(toMap(Balance::getAssetSymbol, Balance::getBalance))));
    }
}
