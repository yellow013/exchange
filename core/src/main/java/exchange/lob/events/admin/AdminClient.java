package exchange.lob.events.admin;


import exchange.lob.node.client.response.*;

import java.util.concurrent.CompletableFuture;
import java.util.function.LongSupplier;

public class AdminClient implements AdminResponse
{
    private final AdminRequests adminRequests;
    private final LongSupplier correlationIdSupplier = System::nanoTime;
    private final ResponseStore responseStore = new ResponseStore();

    public AdminClient(final AdminRequests adminRequests)
    {
        this.adminRequests = adminRequests;
    }

    public CompletableFuture<AddUserResponse> addUser(final String username, final String password)
    {
        final long correlationId = correlationIdSupplier.getAsLong();
        final CompletableFuture<AddUserResponse> future = new CompletableFuture<>();
        responseStore.addUserResponses.put(correlationId, future);
        adminRequests.addUser(correlationId, username, password);
        return future;
    }

    public CompletableFuture<VerifyUserResponse> verifyUser(final String username, final String password)
    {
        final long correlationId = correlationIdSupplier.getAsLong();
        final CompletableFuture<VerifyUserResponse> future = new CompletableFuture<>();
        responseStore.verifyUserResponses.put(correlationId, future);
        adminRequests.verifyUser(correlationId, username, password);
        return future;
    }

    public CompletableFuture<AddAssetResponse> addAsset(final String symbol, final byte scale)
    {
        final long correlationId = correlationIdSupplier.getAsLong();
        final CompletableFuture<AddAssetResponse> future = new CompletableFuture<>();
        responseStore.addAssetResponses.put(correlationId, future);
        adminRequests.addAsset(correlationId, symbol, scale);
        return future;
    }

    public CompletableFuture<AddProductResponse> addProduct(final long baseAssetId, final long counterAssetId, final long makerFee, final long takerFee)
    {
        final long correlationId = correlationIdSupplier.getAsLong();
        final CompletableFuture<AddProductResponse> future = new CompletableFuture<>();
        responseStore.addProductResponses.put(correlationId, future);
        adminRequests.addProduct(correlationId, baseAssetId, counterAssetId, makerFee, takerFee);
        return future;
    }

    public CompletableFuture<GetProductsResponse> fetchProducts()
    {
        final long correlationId = correlationIdSupplier.getAsLong();
        final CompletableFuture<GetProductsResponse> future = new CompletableFuture<>();
        responseStore.getProductResponses.put(correlationId, future);
        adminRequests.fetchProducts(correlationId);
        return future;
    }

    public CompletableFuture<GetUsersResponse> fetchUsers()
    {
        final long correlationId = correlationIdSupplier.getAsLong();
        final CompletableFuture<GetUsersResponse> future = new CompletableFuture<>();
        responseStore.getUsersResponses.put(correlationId, future);
        adminRequests.fetchUsers(correlationId);
        return future;
    }

    public CompletableFuture<GetAssetsResponse> fetchAssets()
    {
        final long correlationId = correlationIdSupplier.getAsLong();
        final CompletableFuture<GetAssetsResponse> future = new CompletableFuture<>();
        responseStore.getAssetsResponses.put(correlationId, future);
        adminRequests.fetchAssets(correlationId);
        return future;
    }

    public CompletableFuture<UpdateBalanceResponse> updateBalance(final long userId, final String assetSymbol, final double amount)
    {
        final long correlationId = correlationIdSupplier.getAsLong();
        final CompletableFuture<UpdateBalanceResponse> future = new CompletableFuture<>();
        responseStore.updateBalanceResponses.put(correlationId, future);
        adminRequests.updateBalance(correlationId, userId, assetSymbol, amount);
        return future;
    }

    public CompletableFuture<GetBalancesResponse> fetchUserBalances(final long userId)
    {
        final long correlationId = correlationIdSupplier.getAsLong();
        final CompletableFuture<GetBalancesResponse> future = new CompletableFuture<>();
        responseStore.getBalancesResponses.put(correlationId, future);
        adminRequests.fetchUserBalances(correlationId, userId);
        return future;
    }

    @Override
    public void onAddUserResponse(final long correlationId, final AddUserResponse response)
    {
        final CompletableFuture<AddUserResponse> future = responseStore.addUserResponses.remove(correlationId);
        if (future != null)
        {
            future.complete(response);
        }
    }

    @Override
    public void onVerifyUserResponse(final long correlationId, final VerifyUserResponse response)
    {
        final CompletableFuture<VerifyUserResponse> future = responseStore.verifyUserResponses.remove(correlationId);
        if (future != null)
        {
            future.complete(response);
        }
    }

    @Override
    public void onAddAssetResponse(final long correlationId, final AddAssetResponse response)
    {
        final CompletableFuture<AddAssetResponse> future = responseStore.addAssetResponses.remove(correlationId);
        if (future != null)
        {
            future.complete(response);
        }
    }

    @Override
    public void onGetUsersResponse(final long correlationId, final GetUsersResponse response)
    {
        final CompletableFuture<GetUsersResponse> future = responseStore.getUsersResponses.remove(correlationId);
        if (future != null)
        {
            future.complete(response);
        }
    }

    @Override
    public void onGetAssetsResponse(final long correlationId, final GetAssetsResponse response)
    {
        final CompletableFuture<GetAssetsResponse> future = responseStore.getAssetsResponses.remove(correlationId);
        if (future != null)
        {
            future.complete(response);
        }
    }

    @Override
    public void onGetProductsResponse(final long correlationId, final GetProductsResponse response)
    {
        final CompletableFuture<GetProductsResponse> future = responseStore.getProductResponses.remove(correlationId);
        if (future != null)
        {
            future.complete(response);
        }
    }

    @Override
    public void onAddProduct(final long correlationId, final AddProductResponse response)
    {
        final CompletableFuture<AddProductResponse> future = responseStore.addProductResponses.remove(correlationId);
        if (future != null)
        {
            future.complete(response);
        }
    }

    @Override
    public void onUpdateBalanceResponse(final long correlationId, final UpdateBalanceResponse response)
    {
        final CompletableFuture<UpdateBalanceResponse> future = responseStore.updateBalanceResponses.remove(correlationId);
        if (future != null)
        {
            future.complete(response);
        }
    }

    @Override
    public void onFetchUserBalancesResponse(final long correlationId, final GetBalancesResponse response)
    {
        final CompletableFuture<GetBalancesResponse> future = responseStore.getBalancesResponses.remove(correlationId);
        if (future != null)
        {
            future.complete(response);
        }
    }
}
