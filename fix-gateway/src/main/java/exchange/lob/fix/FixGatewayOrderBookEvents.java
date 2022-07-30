package exchange.lob.fix;

import exchange.lob.domain.OrderStatus;
import exchange.lob.domain.RejectionReason;
import exchange.lob.domain.Side;
import exchange.lob.events.trading.OrderBookEvents;
import exchange.lob.product.Product;
import org.agrona.concurrent.SystemEpochNanoClock;
import uk.co.real_logic.artio.session.Session;

import java.util.concurrent.ConcurrentHashMap;

public class FixGatewayOrderBookEvents implements OrderBookEvents
{
    private final ExecutionReporter executionReporter;
    private final SystemEpochNanoClock clock;

    public FixGatewayOrderBookEvents(final ConcurrentHashMap<Long, Session> fixSessionByUserId, final SystemEpochNanoClock clock)
    {
        this.executionReporter = new ExecutionReporter(fixSessionByUserId);
        this.clock = clock;
    }

    @Override
    public void onOrderPlaced(
        final long correlationId,
        final String product,
        final String clientOrderId,
        final long executionId,
        final long userId,
        final long orderId,
        final OrderStatus orderStatus,
        final Side side,
        final long price,
        final short priceScale,
        final long amount,
        final short amountScale
    )
    {
        final byte[] timestampBytes = getTimestampBytes();
        executionReporter.reportOrderAccepted(
            timestampBytes,
            userId,
            executionId,
            clientOrderId,
            orderStatus,
            product,
            side,
            price,
            priceScale,
            amount,
            amountScale
        );
    }

    @Override
    public void onOrderRejected(
        final long correlationId,
        final String clientOrderId,
        final long executionId,
        final String productSymbol,
        final long userId,
        final Side side,
        final RejectionReason rejectionReason
    )
    {
        final byte[] timestampBytes = getTimestampBytes();
        executionReporter.reportOrderRejected(
            timestampBytes,
            userId,
            executionId,
            clientOrderId,
            productSymbol,
            side,
            rejectionReason
        );
    }

    @Override
    public void onTrade(
        final long correlationId,
        final String makerClientOrderId,
        final String takerClientOrderId,
        final long executionId,
        final long makerUserId,
        final long takerUserId,
        final OrderStatus makerOrderStatus,
        final OrderStatus takerOrderStatus,
        final Product product,
        final Side takerSide,
        final long price,
        final short priceScale,
        final long amount,
        final short amountScale
    )
    {
        final byte[] timestampBytes = getTimestampBytes();

        executionReporter.reportTrade(
            timestampBytes,
            executionId,
            makerUserId,
            takerUserId,
            makerClientOrderId,
            takerClientOrderId,
            makerOrderStatus,
            takerOrderStatus,
            product.getSymbol(),
            takerSide,
            price,
            priceScale,
            amount,
            amountScale
        );
    }

    @Override
    public void onOrderCancelled(
        final long correlationId,
        final String clientOrderId,
        final long executionId,
        final OrderStatus orderStatus,
        final long userId,
        final String product,
        final Side side,
        final long price,
        final short priceScale,
        final long amount,
        final short amountScale
    )
    {
        final byte[] timestampBytes = getTimestampBytes();

        executionReporter.publishOrderCancelled(
            timestampBytes,
            userId,
            executionId,
            clientOrderId,
            product,
            orderStatus,
            side,
            price,
            priceScale,
            amount,
            amountScale
        );
    }

    @Override
    public void onCancellationRejected(
        final long correlationId,
        final String clientOrderId,
        final long userId,
        final String productSymbol,
        final RejectionReason rejectionReason
    )
    {
        final byte[] timestampBytes = getTimestampBytes();

        executionReporter.publishCancelRejected(timestampBytes, userId, clientOrderId, rejectionReason);
    }

    @Override
    public void onExecution(
        final long correlationId,
        final String clientOrderId,
        final long userId,
        final OrderStatus orderStatus
    )
    {

    }

    private byte[] getTimestampBytes()
    {
        return Long.toString(clock.nanoTime()).getBytes();
    }
}
