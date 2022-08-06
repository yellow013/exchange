package exchange.lob.events.trading;

import exchange.lob.domain.OrderType;
import exchange.lob.domain.RejectionReason;
import exchange.lob.domain.Side;
import exchange.lob.match.MatchingEngine;
import exchange.lob.product.Product;
import exchange.lob.product.ProductService;
import exchange.lob.risk.RiskEngine;
import exchange.lob.user.User;
import exchange.lob.user.UserService;

import static exchange.lob.math.Quantiser.toMinorUnits;

public class TradingRequestProcessor implements TradingRequests
{

    private final OrderBookEvents orderBookEvents;

    private MatchingEngine matchingEngine;
    private UserService userService;
    private ProductService productService;
    private RiskEngine riskEngine;

    public TradingRequestProcessor(final OrderBookEvents orderBookEvents)
    {
        this.orderBookEvents = orderBookEvents;
    }

    @Override
    public void placeOrder(
        final String username,
        final String productSymbol,
        final String clientOrderId,
        final OrderType orderType,
        final Side side,
        final double price,
        final double amount
    )
    {
        final long correlationId = System.nanoTime();

        final User user = userService.getUser(username);

        if (user == null)
        {
            final long executionId = matchingEngine.newExecutionId();
            orderBookEvents.onOrderRejected(
                correlationId,
                clientOrderId,
                executionId,
                productSymbol,
                Long.MIN_VALUE,
                side,
                RejectionReason.INVALID_USER
            );

            return;
        }

        final Product product = productService.getProduct(productSymbol);

        if (product == null)
        {
            orderBookEvents.onOrderRejected(
                correlationId,
                clientOrderId,
                matchingEngine.newExecutionId(),
                productSymbol,
                user.getUserId(),
                side,
                RejectionReason.INVALID_PRODUCT
            );

            return;
        }

        final long unscaledPrice = toMinorUnits(price, product.getCounterAsset().getScale());
        final long unscaledAmount = toMinorUnits(amount, product.getBaseAsset().getScale());

        final long reservedBalance = riskEngine.reserveBalance(
            correlationId,
            clientOrderId,
            user.getUserId(),
            product,
            orderType,
            side,
            unscaledPrice,
            unscaledAmount,
            matchingEngine::newExecutionId
        );

        if (reservedBalance != Long.MIN_VALUE)
        {
            matchingEngine.handleOrderPlacement(
                correlationId,
                clientOrderId,
                user.getUserId(),
                product,
                orderType,
                side,
                unscaledPrice,
                unscaledAmount,
                reservedBalance,
                riskEngine::settleExecutionReports
            );
        }
    }

    @Override
    public void cancelOrder(final String username, final String productSymbol, final String clientOrderId, final double amount)
    {
        final long correlationId = System.nanoTime();

        final Product product = productService.getProduct(productSymbol);
        final long unscaledAmount = toMinorUnits(amount, product.getBaseAsset().getScale());

        matchingEngine.handleOrderCancellation(
            correlationId,
            clientOrderId,
            product,
            userService.getUser(username).getUserId(),
            unscaledAmount,
            riskEngine::settleExecutionReports
        );
    }

    public TradingRequestProcessor bindUserService(final UserService userService)
    {
        this.userService = userService;
        return this;
    }

    public TradingRequestProcessor bindMatchingEngine(final MatchingEngine matchingEngine)
    {
        this.matchingEngine = matchingEngine;
        return this;
    }

    public TradingRequestProcessor bindProductService(final ProductService productService)
    {
        this.productService = productService;
        return this;
    }

    public TradingRequestProcessor bindRiskEngine(final RiskEngine riskEngine)
    {
        this.riskEngine = riskEngine;
        return this;
    }
}
