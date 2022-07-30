package exchange.lob.events.admin;

import exchange.lob.domain.ExchangeResponseCode;
import exchange.lob.match.MatchingEngine;
import exchange.lob.node.client.response.*;
import exchange.lob.product.Asset;
import exchange.lob.product.ProductService;
import exchange.lob.risk.RiskEngine;
import exchange.lob.user.User;
import exchange.lob.user.UserService;
import org.agrona.collections.Long2LongHashMap;

import java.util.List;

import static exchange.lob.math.Quantiser.toMajorUnits;
import static exchange.lob.math.Quantiser.toMinorUnits;

public class AdminRequestProcessor implements AdminRequests
{
    private final AdminResponse adminResponse;
    private UserService userService;
    private RiskEngine riskEngine;
    private ProductService productService;
    private MatchingEngine matchingEngine;

    public AdminRequestProcessor(final AdminResponse adminResponse)
    {
        this.adminResponse = adminResponse;
    }

    @Override
    public void addUser(final long correlationId, final String username, final String password)
    {
        final AddUserResponse response = userService.addUser(username, password, riskEngine::addUser);
        adminResponse.onAddUserResponse(correlationId, response);
    }

    @Override
    public void verifyUser(final long correlationId, final String username, final String password)
    {
        final VerifyUserResponse response = userService.verifyUser(username, password);
        adminResponse.onVerifyUserResponse(correlationId, response);
    }

    @Override
    public void addAsset(final long correlationId, final String symbol, final byte scale)
    {
        final AddAssetResponse response = productService.addAsset(symbol, scale, riskEngine::addAsset);
        adminResponse.onAddAssetResponse(correlationId, response);
    }

    @Override
    public void addProduct(final long correlationId, final long baseAssetId, final long counterAssetId, final long makerFee, final long takerFee)
    {
        final AddProductResponse response = productService.addProduct(
            baseAssetId,
            counterAssetId,
            makerFee,
            takerFee
        );

        matchingEngine.onAddProduct(response.productId());
        adminResponse.onAddProduct(correlationId, response);
    }

    @Override
    public void fetchUsers(final long correlationId)
    {
        final GetUsersResponse response = userService.getUsers();
        adminResponse.onGetUsersResponse(correlationId, response);
    }

    @Override
    public void fetchAssets(final long correlationId)
    {
        final GetAssetsResponse response = productService.getAssets();
        adminResponse.onGetAssetsResponse(correlationId, response);
    }

    @Override
    public void fetchProducts(final long correlationId)
    {
        final GetProductsResponse response = productService.getProducts();
        adminResponse.onGetProductsResponse(correlationId, response);
    }

    @Override
    public void updateBalance(final long correlationId, final long userId, final String assetSymbol, final double amount)
    {
        final User user = userService.getUser(userId);
        if (user == null)
        {
            final UpdateBalanceResponse response = new UpdateBalanceResponse(ExchangeResponseCode.INVALID_USERNAME);
            adminResponse.onUpdateBalanceResponse(correlationId, response);
            return;
        }

        final Asset asset = productService.getAsset(assetSymbol);
        if (asset == null)
        {
            final UpdateBalanceResponse response = new UpdateBalanceResponse(ExchangeResponseCode.INVALID_ASSET);
            adminResponse.onUpdateBalanceResponse(correlationId, response);
            return;
        }

        final UpdateBalanceResponse response = riskEngine.updateBalance(userId, asset.getAssetId(), toMinorUnits(amount, asset.getScale()));
        adminResponse.onUpdateBalanceResponse(correlationId, response);
    }

    @Override
    public void fetchUserBalances(final long correlationId, final long userId)
    {
        final Long2LongHashMap userBalances = riskEngine.getBalances(userId);
        final List<Balance> balances = userBalances.keySet().stream()
            .map(productService::getAsset)
            .map(a -> new Balance(a.getSymbol(), toMajorUnits(userBalances.get(a.getAssetId()), a.getScale())))
            .toList();

        final GetBalancesResponse response = new GetBalancesResponse(ExchangeResponseCode.SUCCESS, balances);
        adminResponse.onFetchUserBalancesResponse(correlationId, response);
    }

    public AdminRequestProcessor bindUserService(final UserService userService)
    {
        this.userService = userService;
        return this;
    }

    public AdminRequestProcessor bindRiskEngine(final RiskEngine riskEngine)
    {
        this.riskEngine = riskEngine;
        return this;
    }

    public AdminRequestProcessor bindProductService(final ProductService productService)
    {
        this.productService = productService;
        return this;
    }

    public AdminRequestProcessor bindMatchingEngine(final MatchingEngine matchingEngine)
    {
        this.matchingEngine = matchingEngine;
        return this;
    }
}
