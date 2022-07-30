package exchange.lob.fix;

import exchange.lob.api.codecs.fix.ExecType;
import exchange.lob.api.codecs.fix.OrdRejReason;
import exchange.lob.api.codecs.fix.OrdStatus;
import exchange.lob.api.codecs.fix.builder.ExecutionReportEncoder;
import exchange.lob.api.codecs.fix.builder.OrderCancelRejectEncoder;
import exchange.lob.domain.OrderStatus;
import exchange.lob.domain.RejectionReason;
import exchange.lob.domain.Side;
import exchange.lob.fix.util.CancelRejectionReasons;
import exchange.lob.fix.util.RejectionReasons;
import exchange.lob.fix.util.Sides;
import org.agrona.concurrent.UnsafeBuffer;
import uk.co.real_logic.artio.fields.DecimalFloat;
import uk.co.real_logic.artio.session.Session;

import java.util.Map;

import static exchange.lob.fix.util.OrderStatuses.toFix;
import static exchange.lob.fix.util.Util.sendReliably;
import static exchange.lob.match.util.Sides.other;

public class ExecutionReporter
{
    private final int SIZE_OF_ASCII_LONG = String.valueOf(Long.MAX_VALUE).length();
    private final byte[] EXEC_ID_BUFFER = new byte[SIZE_OF_ASCII_LONG];
    private final UnsafeBuffer EXEC_ID_ENCODER = new UnsafeBuffer(EXEC_ID_BUFFER);
    private int execIdEncodedLength;

    private final ExecutionReportEncoder executionReport = new ExecutionReportEncoder();
    private final OrderCancelRejectEncoder orderCancelReject = new OrderCancelRejectEncoder();
    private final DecimalFloat priceCodec = new DecimalFloat();
    private final DecimalFloat ordQtyCodec = new DecimalFloat();

    private final Map<Long, Session> fixSessionByUserId;

    public ExecutionReporter(final Map<Long, Session> fixSessionByUserId)
    {
        this.fixSessionByUserId = fixSessionByUserId;
    }

    public void reportOrderAccepted(
        final byte[] timestampBytes,
        final long userId,
        final long executionId,
        final String clientOrderId,
        final OrderStatus orderStatus,
        final String productSymbol,
        final Side side,
        final long price,
        final short priceScale,
        final long amount,
        final short amountScale
    )
    {
        final Session session = fixSessionByUserId.get(userId);

        encodeAndSendExecutionReport(
            session,
            timestampBytes,
            executionId,
            clientOrderId,
            productSymbol,
            orderStatus,
            ExecType.NEW,
            side,
            price,
            priceScale,
            amount,
            amountScale
        );
    }

    public void reportOrderRejected(
        final byte[] timestampBytes,
        final long userId,
        final long executionId,
        final String clientOrderId,
        final String productSymbol,
        final Side side,
        final RejectionReason rejectionReason
    )
    {
        final Session session = fixSessionByUserId.get(userId);

        encodeAndSendRejectionExecutionReport(
            session,
            timestampBytes,
            executionId,
            clientOrderId,
            productSymbol,
            side,
            rejectionReason
        );
    }

    public void reportTrade(
        final byte[] timestampBytes,
        final long executionId,
        final long makerUserId,
        final long takerUserId,
        final String makerClientOrderId,
        final String takerClientOrderId,
        final OrderStatus makerOrderStatus,
        final OrderStatus takerOrderStatus,
        final String productSymbol,
        final Side takerSide,
        final long price,
        final short priceScale,
        final long amount,
        final short amountScale
    )
    {
        final Session makerSession = fixSessionByUserId.get(makerUserId);
        final Session takerSession = fixSessionByUserId.get(takerUserId);

        encodeAndSendExecutionReport(
            makerSession,
            timestampBytes,
            executionId,
            makerClientOrderId,
            productSymbol,
            makerOrderStatus,
            ExecType.TRADE,
            other(takerSide),
            price,
            priceScale,
            amount,
            amountScale
        );

        encodeAndSendExecutionReport(
            takerSession,
            timestampBytes,
            executionId,
            takerClientOrderId,
            productSymbol,
            takerOrderStatus,
            ExecType.TRADE,
            takerSide,
            price,
            priceScale,
            amount,
            amountScale
        );
    }

    public void publishOrderCancelled(
        final byte[] timestampBytes,
        final long userId,
        final long executionId,
        final String clientOrderId,
        final String productSymbol,
        final OrderStatus orderStatus,
        final Side side,
        final long price,
        final short priceScale,
        final long amount,
        final short amountScale
    )
    {
        final Session session = fixSessionByUserId.get(userId);

        encodeAndSendExecutionReport(
            session,
            timestampBytes,
            executionId,
            clientOrderId,
            productSymbol,
            orderStatus,
            ExecType.CANCELED,
            side,
            price,
            priceScale,
            amount,
            amountScale
        );
    }

    public void publishCancelRejected(final byte[] timestampBytes, final long userId, final String clientOrderId, final RejectionReason rejectionReason)
    {
        final Session session = fixSessionByUserId.get(userId);

        encodeAndSendCancelReject(session, timestampBytes, clientOrderId, rejectionReason);
    }

    private void encodeAndSendExecutionReport(
        final Session session,
        final byte[] timestamp,
        final long executionId,
        final String clientOrderId,
        final String productSymbol,
        final OrderStatus orderStatus,
        final ExecType execType,
        final Side side,
        final long price,
        final short priceScale,
        final long amount,
        final short amountScale
    )
    {
        newExecId(executionId);

        executionReport.reset();

        priceCodec.set(price, priceScale);
        ordQtyCodec.set(amount, amountScale);

        executionReport
            .transactTime(timestamp)
            .orderID(clientOrderId)
            .execID(EXEC_ID_BUFFER, execIdEncodedLength)
            .execType(execType)
            .ordStatus(toFix(orderStatus))
            .side(Sides.toFix(side))
            .price(priceCodec)
            .orderQtyData()
            .orderQty(ordQtyCodec);

        executionReport.instrument().symbol(productSymbol);

        sendReliably(session, executionReport);
    }

    private void encodeAndSendRejectionExecutionReport(
        final Session session,
        final byte[] timestamp,
        final long executionId,
        final String clientOrderId,
        final String productSymbol,
        final Side side,
        final RejectionReason rejectionReason
    )
    {
        newExecId(executionId);

        executionReport.reset();

        final OrdRejReason ordRejReason = RejectionReasons.toFix(rejectionReason);

        executionReport
            .transactTime(timestamp)
            .orderID(clientOrderId)
            .execID(EXEC_ID_BUFFER, execIdEncodedLength)
            .side(Sides.toFix(side))
            .execType(ExecType.REJECTED)
            .ordStatus(OrdStatus.REJECTED)
            .ordRejReason(ordRejReason);

        if (ordRejReason == OrdRejReason.BROKER_OPTION)
        {
            executionReport.text(rejectionReason.name());
        }

        executionReport.instrument().symbol(productSymbol);

        sendReliably(session, executionReport);
    }

    private void encodeAndSendCancelReject(
        final Session session,
        final byte[] timestampBytes,
        final String clientOrderId,
        final RejectionReason rejectionReason
    )
    {
        orderCancelReject.reset();

        orderCancelReject
            .transactTime(timestampBytes)
            .orderID(clientOrderId)
            .cxlRejReason(CancelRejectionReasons.toFix(rejectionReason));

        sendReliably(session, orderCancelReject);
    }

    private void newExecId(final long executionId)
    {
        execIdEncodedLength = EXEC_ID_ENCODER.putLongAscii(0, executionId);
    }
}
