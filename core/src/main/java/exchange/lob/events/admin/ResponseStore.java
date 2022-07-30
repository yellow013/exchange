package exchange.lob.events.admin;

import exchange.lob.node.client.response.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResponseStore
{
    final ConcurrentMap<Long, CompletableFuture<AddUserResponse>> addUserResponses = new ConcurrentHashMap<>();
    final ConcurrentMap<Long, CompletableFuture<VerifyUserResponse>> verifyUserResponses = new ConcurrentHashMap<>();
    final ConcurrentMap<Long, CompletableFuture<GetUsersResponse>> getUsersResponses = new ConcurrentHashMap<>();
    final ConcurrentMap<Long, CompletableFuture<AddAssetResponse>> addAssetResponses = new ConcurrentHashMap<>();
    final ConcurrentMap<Long, CompletableFuture<GetAssetsResponse>> getAssetsResponses = new ConcurrentHashMap<>();
    final ConcurrentMap<Long, CompletableFuture<AddProductResponse>> addProductResponses = new ConcurrentHashMap<>();
    final ConcurrentMap<Long, CompletableFuture<GetProductsResponse>> getProductResponses = new ConcurrentHashMap<>();
    public ConcurrentMap<Long, CompletableFuture<UpdateBalanceResponse>> updateBalanceResponses = new ConcurrentHashMap<>();
    public ConcurrentMap<Long, CompletableFuture<GetBalancesResponse>> getBalancesResponses = new ConcurrentHashMap<>();
}
