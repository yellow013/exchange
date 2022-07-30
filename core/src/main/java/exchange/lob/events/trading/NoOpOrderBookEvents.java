package exchange.lob.events.trading;

import exchange.lob.domain.OrderStatus;
import exchange.lob.domain.RejectionReason;
import exchange.lob.domain.Side;
import exchange.lob.product.Product;

public class NoOpOrderBookEvents implements OrderBookEvents
{

    public static final OrderBookEvents INSTANCE = new NoOpOrderBookEvents();

    @Override
    public void onOrderPlaced(final long correlationId, final String product, final String clientOrderId, final long executionId, final long userId, final long orderId, final OrderStatus orderStatus, final Side side, final long price, final short priceScale, final long amount, final short amountScale)
    {

    }

    @Override
    public void onOrderRejected(final long correlationId, final String clientOrderId, final long executionId, final String productSymbol, final long userId, final Side side, final RejectionReason rejectionReason)
    {

    }

    @Override
    public void onTrade(final long correlationId, final String makerClientOrderId, final String takerClientOrderId, final long executionId, final long makerUserId, final long takerUserId, final OrderStatus makerOrderStatus, final OrderStatus takerOrderStatus, final Product product, final Side takerSide, final long price, final short priceScale, final long amount, final short amountScale)
    {

    }

    @Override
    public void onExecution(final long correlationId, final String clientOrderId, final long userId, final OrderStatus orderStatus)
    {

    }

    @Override
    public void onOrderCancelled(final long correlationId, final String clientOrderId, final long executionId, final OrderStatus orderStatus, final long userId, final String product, final Side side, final long price, final short priceScale, final long amount, final short amountScale)
    {

    }

    @Override
    public void onCancellationRejected(final long correlationId, final String clientOrderId, final long userId, final String productSymbol, final RejectionReason rejectionReason)
    {

    }
}
