package exchange.lob.risk;

import exchange.lob.api.codecs.internal.OrderType;
import exchange.lob.api.codecs.internal.Side;
import exchange.lob.product.Product;
import exchange.lob.user.UserService;

public class RiskEngineHelper
{
    public static final long ANY_CORRELATION_ID = 123L;
    private final RiskEngine riskEngine;
    private final UserService userService;
    private long userNonce = 0L;

    public RiskEngineHelper(final RiskEngine riskEngine, final UserService userService)
    {
        this.riskEngine = riskEngine;
        this.userService = userService;
    }

    public long addUser()
    {
        userService.addUser("TEST-" + ++userNonce, "password", riskEngine::addUser);
        return riskEngine.balances.keySet().stream().max(Long::compareTo).orElse(Long.MIN_VALUE);
    }

    public void updateBalance(long userId, long baseAssetId, long amount)
    {
        riskEngine.updateBalance(userId, baseAssetId, amount);
    }

    public long handleOrder(
        long userId,
        Product product,
        OrderType orderType,
        Side side,
        long price,
        long amount
    )
    {
        return riskEngine.reserveBalance(ANY_CORRELATION_ID, "clientOrderId", userId, product, orderType, side, price, amount, () -> 10L);
    }
}
