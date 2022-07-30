package exchange.lob.events.trading;

import exchange.lob.domain.OrderType;
import exchange.lob.domain.Side;
import io.aeronic.Aeronic;

@Aeronic
public interface TradingRequests
{
    void placeOrder(
        final String username,
        final String productSymbol,
        final String clientOrderId,
        final OrderType orderType,
        final Side side,
        final double price,
        final double amount
    );

    void cancelOrder(
        final String username,
        final String productSymbol,
        final String clientOrderId,
        final double amount
    );
}
