package exchange.lob.events.admin;

import exchange.lob.node.client.response.*;
import io.aeronic.Aeronic;

@Aeronic
public interface AdminResponse
{
    void onAddUserResponse(long correlationId, AddUserResponse response);
    void onVerifyUserResponse(long correlationId, VerifyUserResponse response);
    void onAddAssetResponse(long correlationId, AddAssetResponse response);
    void onAddProduct(long correlationId, AddProductResponse response);
    void onGetUsersResponse(long correlationId, GetUsersResponse response);
    void onGetAssetsResponse(long correlationId, GetAssetsResponse response);
    void onGetProductsResponse(long correlationId, GetProductsResponse response);
    void onUpdateBalanceResponse(long correlationId, UpdateBalanceResponse response);
    void onFetchUserBalancesResponse(long correlationId, GetBalancesResponse response);
}
