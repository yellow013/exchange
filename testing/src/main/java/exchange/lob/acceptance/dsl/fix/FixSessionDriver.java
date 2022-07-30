package exchange.lob.acceptance.dsl.fix;

import exchange.lob.fix.FixClient;
import exchange.lob.fix.FixClientFactory;
import exchange.lob.fix.fields.EncryptMethod;
import exchange.lob.fix.fields.MsgType;
import exchange.lob.fix.fields.OrdType;
import exchange.lob.fix.fields.Side;
import exchange.lob.fix.outgoing.FixMessage;
import exchange.lob.fix.outgoing.FixMessageBuilder;
import exchange.lob.fix.transport.ConnectionObserver;
import org.agrona.collections.MutableInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static exchange.lob.Assertions.assertEventually;
import static exchange.lob.Assertions.assertReflectiveContains;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixSessionDriver
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FixSessionDsl.class);
    private static final String TARGET_COMP_ID = "exchange";

    private final FixClient client;
    private final AtomicBoolean loggedOn = new AtomicBoolean(false);
    private final AtomicBoolean loggedOut = new AtomicBoolean(false);
    private final CopyOnWriteArrayList<ExecutionReport> executionReports = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<FixOrderCancelReject> orderCancelRejects = new CopyOnWriteArrayList<>();
    private final String compId;
    private final MutableInteger msgSeqNum = new MutableInteger(1);

    public FixSessionDriver(final String compId, final String gatewayHostname, final int gatewayPort)
    {
        this.compId = compId;
        this.client = FixClientFactory.createFixClient(gatewayHostname, gatewayPort);
        connect();
    }

    private void connect()
    {
        client.subscribeToAllMessages(new AcceptanceFixMessageHandler(compId, loggedOn, loggedOut, executionReports, orderCancelRejects));
        client.registerTransportObserver(new TestConnectionObserver());
        client.connect();

        await().atMost(Duration.ofSeconds(2)).until(client::isConnected);
    }

    public void login(final String username, final String password, final boolean authenticationFailure)
    {
        final FixMessage message = new FixMessageBuilder()
            .messageType(MsgType.LOGIN)
            .msgSeqNum(msgSeqNum.getAndIncrement())
            .senderCompID(compId)
            .targetCompID(TARGET_COMP_ID)
            .sendingTimeNow()
            .encryptMethod(EncryptMethod.NONE)
            .heartBtInt(10)
            .username(username)
            .password(password)
            .build();

        client.send(message);

        LOGGER.info("{} sent FIX message: {}", compId, message.toHumanString());

        if (authenticationFailure)
        {
            await().untilTrue(loggedOut);
        }
        else
        {
            await().untilTrue(loggedOn);
        }
    }

    public void logout()
    {
        final FixMessage message = new FixMessageBuilder()
            .messageType(MsgType.LOGOUT)
            .msgSeqNum(msgSeqNum.getAndIncrement())
            .senderCompID(compId)
            .targetCompID(TARGET_COMP_ID)
            .sendingTimeNow()
            .build();

        client.send(message);

        LOGGER.info("{} sent FIX message: {}", compId, message.toHumanString());

        await().untilTrue(loggedOut);
    }

    public void placeOrder(
        final String clientOrderId,
        final String product,
        final OrdType ordType,
        final Side side,
        final Optional<String> maybePrice,
        final Optional<String> maybeOrderQty
    )
    {
        final FixMessageBuilder messageBuilder = new FixMessageBuilder()
            .messageType(MsgType.NEW_ORDER_SINGLE)
            .msgSeqNum(msgSeqNum.getAndIncrement())
            .senderCompID(compId)
            .targetCompID(TARGET_COMP_ID)
            .sendingTimeNow()
            .clOrdID(clientOrderId)
            .symbol(product)
            .orderType(ordType)
            .side(side);

        maybePrice.ifPresent(messageBuilder::price);
        maybeOrderQty.ifPresent(messageBuilder::orderQty);

        final FixMessage message = messageBuilder.build();

        client.send(message);

        LOGGER.info("{} sent FIX message: {}", compId, message.toHumanString());

        assertEventually(() -> assertTrue(executionReports.stream().anyMatch(er -> er.clientOrderId().equals(clientOrderId))), Duration.ofHours(1));
    }

    public void verifyExecutionReport(final ExecutionReport expectedExecutionReport)
    {
        await().ignoreExceptions().until(() -> {
            assertReflectiveContains(expectedExecutionReport, executionReports, ExecutionReport::match);
            return true;
        });
    }

    public void verifyCancelReject(final FixOrderCancelReject expectedOrderCancelReject)
    {
        await().ignoreExceptions().until(() -> {
            assertReflectiveContains(expectedOrderCancelReject, orderCancelRejects, FixOrderCancelReject::match);
            return true;
        });
    }

    public void noMoreExecutionReports()
    {
        assertThat(executionReports).allMatch(ExecutionReport::matched);
    }

    public void noMoreCancelRejects()
    {
        assertThat(orderCancelRejects).allMatch(FixOrderCancelReject::matched);
    }

    public void cancelOrder(final String clientOrderId, final String product, final Optional<BigDecimal> maybeOrderQty)
    {
        final FixMessageBuilder messageBuilder = new FixMessageBuilder()
            .messageType(MsgType.ORDER_CANCEL_REQUEST)
            .msgSeqNum(msgSeqNum.getAndIncrement())
            .senderCompID(compId)
            .targetCompID(TARGET_COMP_ID)
            .sendingTimeNow()
            .clOrdID(clientOrderId)
            .symbol(product);

        maybeOrderQty.ifPresent(messageBuilder::orderQty);

        final FixMessage message = messageBuilder.build();

        client.send(message);

        LOGGER.info("{} sent FIX message: {}", compId, message.toHumanString());

        await().until(() -> cancelAcknowledged(clientOrderId));
    }

    private boolean cancelAcknowledged(final String clientOrderId)
    {
        return executionReports.stream().anyMatch(er -> er.clientOrderId().equals(clientOrderId))
            || orderCancelRejects.stream().anyMatch(cr -> cr.clientOrderId().equals(clientOrderId));
    }

    public void close()
    {
        if (client.isConnected())
        {
            client.killSocket();
        }
    }

    private class TestConnectionObserver implements ConnectionObserver
    {
        @Override
        public void connectionEstablished()
        {
            LOGGER.info("FIX connection established for comp ID: {}", compId);
        }

        @Override
        public void connectionClosed()
        {
            LOGGER.info("FIX connection closed for comp ID: {}", compId);
        }
    }
}
