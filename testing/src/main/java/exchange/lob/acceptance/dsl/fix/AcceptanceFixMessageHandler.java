package exchange.lob.acceptance.dsl.fix;

import exchange.lob.fix.fields.*;
import exchange.lob.fix.incoming.FixMessage;
import exchange.lob.fix.incoming.FixMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static exchange.lob.fix.fields.ExecType.Rejected;

class AcceptanceFixMessageHandler implements FixMessageHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptanceFixMessageHandler.class);

    private static final String LOGON_MSG_TYPE = "A";
    private static final String LOGOUT_MSG_TYPE = "5";
    private static final String EXECUTION_REPORT_MSG_TYPE = "8";
    private static final String ORDER_CANCEL_REJECT_MSG_TYPE = "9";
    private static final String HEARTBEAT_MSG_TYPE = "0";
    private static final String TEST_REQUEST_MSG_TYPE = "1";

    private static final int MSG_TYPE_TAG_ID = 35;

    private final String compId;
    private final AtomicBoolean loggedOn;
    private final AtomicBoolean loggedOut;
    private final CopyOnWriteArrayList<ExecutionReport> executionReports;
    private final CopyOnWriteArrayList<FixOrderCancelReject> orderCancelRejects;

    public AcceptanceFixMessageHandler(
        final String compId,
        final AtomicBoolean loggedOn,
        final AtomicBoolean loggedOut,
        final CopyOnWriteArrayList<ExecutionReport> executionReports,
        final CopyOnWriteArrayList<FixOrderCancelReject> orderCancelRejects
    )
    {
        this.compId = compId;
        this.loggedOn = loggedOn;
        this.loggedOut = loggedOut;
        this.executionReports = executionReports;
        this.orderCancelRejects = orderCancelRejects;
    }

    @Override
    public void onFixMessage(final FixMessage fixMessage)
    {
        LOGGER.info("{} received FIX message: {}", compId, fixMessage.toHumanString());
        final String messageType = fixMessage.getFirstValue(MSG_TYPE_TAG_ID);
        switch (messageType)
        {
            case HEARTBEAT_MSG_TYPE -> LOGGER.info("{} received heartbeat", compId);
            case TEST_REQUEST_MSG_TYPE -> LOGGER.info("{} received test request", compId);
            case LOGON_MSG_TYPE -> loggedOn.set(true);
            case LOGOUT_MSG_TYPE -> loggedOut.set(true);
            case EXECUTION_REPORT_MSG_TYPE -> handleExecutionReport(fixMessage);
            case ORDER_CANCEL_REJECT_MSG_TYPE -> handleOrderCancelReject(fixMessage);
            default -> throw new IllegalStateException("Unexpected value: " + messageType);
        }
    }

    private void handleExecutionReport(final FixMessage fixMessage)
    {
        final String product = fixMessage.getFirstValue(55);
        final String clientOrderId = fixMessage.getFirstValue(37);
        final ExecType execType = ExecType.fromFixValue(fixMessage.getFirstValue(150));
        final Side side = Side.fromFixValue(fixMessage.getFirstValue(54));
        final OrdStatus ordStatus = OrdStatus.fromFixValue(fixMessage.getFirstValue(39));

        if (execType == Rejected)
        {
            final OrdRejReason ordRejReason = OrdRejReason.fromFixValue(fixMessage.getFirstValue(103));
            final Optional<String> text = fixMessage.getValue(58);

            executionReports.add(new ExecutionReport(
                product,
                clientOrderId,
                ordStatus,
                execType,
                side,
                Optional.empty(),
                Optional.empty(),
                Optional.of(ordRejReason),
                text
            ));
        }
        else
        {
            final String price = fixMessage.getFirstValue(44);
            final String orderQty = fixMessage.getFirstValue(38);

            executionReports.add(new ExecutionReport(
                product,
                clientOrderId,
                ordStatus,
                execType,
                side,
                Optional.of(price),
                Optional.of(orderQty),
                Optional.empty(),
                Optional.empty()
            ));
        }
    }

    private void handleOrderCancelReject(final FixMessage fixMessage)
    {
        final String clientOrderId = fixMessage.getFirstValue(37);
        final CxlRejReason cxlRejReason = CxlRejReason.fromFixValue(fixMessage.getFirstValue(102));
        orderCancelRejects.add(new FixOrderCancelReject(clientOrderId, cxlRejReason));
    }
}
