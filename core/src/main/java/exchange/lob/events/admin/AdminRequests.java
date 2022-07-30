package exchange.lob.events.admin;

import io.aeronic.Aeronic;

@Aeronic
public interface AdminRequests
{
    void addUser(long correlationId, String username, String password);
    void verifyUser(long correlationId, String username, String password);
    void addAsset(long correlationId, String symbol, byte scale);
    void addProduct(final long correlationId, final long baseAssetId, final long counterAssetId, final long makerFee, final long takerFee);
    void fetchUsers(long correlationId);
    void fetchAssets(long correlationId);
    void fetchProducts(long correlationId);
    void updateBalance(long correlationId, long userId, String assetSymbol, double amount);
    void fetchUserBalances(long correlationId, long userId);
}
