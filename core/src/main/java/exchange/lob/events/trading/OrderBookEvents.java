package exchange.lob.events.trading;

import exchange.lob.domain.OrderStatus;
import exchange.lob.domain.RejectionReason;
import exchange.lob.domain.Side;
import exchange.lob.product.Product;
import io.aeronic.Aeronic;

@Aeronic
public interface OrderBookEvents
{
    void onOrderPlaced(
        long correlationId,
        String product,
        String clientOrderId,
        long executionId,
        long userId,
        long orderId,
        OrderStatus orderStatus,
        Side side,
        long price,
        short priceScale,
        long amount,
        short amountScale
    );

    void onOrderRejected(
        long correlationId,
        String clientOrderId,
        long executionId,
        String productSymbol,
        long userId,
        Side side,
        RejectionReason rejectionReason
    );

    void onTrade(
        long correlationId,
        String makerClientOrderId,
        String takerClientOrderId,
        long executionId,
        long makerUserId,
        long takerUserId,
        OrderStatus makerOrderStatus,
        OrderStatus takerOrderStatus,
        Product product,
        Side takerSide,
        long price,
        short priceScale,
        long amount,
        short amountScale
    );

    void onExecution(
        long correlationId,
        String clientOrderId,
        long userId,
        OrderStatus orderStatus
    );

    void onOrderCancelled(
        long correlationId,
        String clientOrderId,
        long executionId,
        OrderStatus orderStatus,
        long userId,
        String product,
        Side side,
        long price,
        short priceScale,
        long amount,
        short amountScale
    );

    void onCancellationRejected(
        long correlationId,
        String clientOrderId,
        long userId,
        final String productSymbol,
        RejectionReason rejectionReason
    );
}
