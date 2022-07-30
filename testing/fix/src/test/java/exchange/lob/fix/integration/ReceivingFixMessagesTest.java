package exchange.lob.fix.integration;

import exchange.lob.fix.FixClient;
import exchange.lob.fix.FixClientFactory;
import exchange.lob.fix.fields.MsgType;
import exchange.lob.fix.incoming.FixMessage;
import exchange.lob.fix.incoming.FixMessageHandler;
import exchange.lob.fix.integration.fixture.IntegrationSocketFactory;
import exchange.lob.fix.outgoing.FixMessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ReceivingFixMessagesTest
{

    private static final String EXPECTED_MSG_1 = "8=FIX.4.2\u00019=65\u000135=A\u000149=SERVER\u000156=CLIENT\u000134=177\u000152=20090107-18:15:16\u000198=0\u0001108=30\u000110=062\u0001";
    private static final String EXPECTED_MSG_2 = "8=FIX.4.2\u00019=65\u000135=A\u000149=SERVER\u000156=CLIENT\u000134=177\u000134=178\u000152=20090107-18:15:16\u000198=0\u0001108=30" +
                                                 "\u000110=062\u0001";
    private CountDownLatch countDownLatch;
    private ReadableByteChannel readableByteChannel;
    private WritableByteChannel writableByteChannel;
    private ByteArrayOutputStream byteArrayOutputStream;

    @BeforeEach
    public void setUp()
    {
        byteArrayOutputStream = new ByteArrayOutputStream();
        writableByteChannel = Channels.newChannel(byteArrayOutputStream);
        countDownLatch = new CountDownLatch(1);
    }

    @Test
    public void shouldGetFixMessages() throws Exception
    {
        readableByteChannel = Channels.newChannel(new ByteArrayInputStream(EXPECTED_MSG_1.getBytes()));
        final FixClient fixClient = buildFixClient();
        fixClient.subscribeToAllMessages(new AssertingFixMessageHandler(EXPECTED_MSG_1));
        fixClient.connect();
        final boolean await = countDownLatch.await(5, TimeUnit.SECONDS);
        assertTrue(await);
    }

    @Test
    public void shouldGetFixMessageWithDuplicateKey() throws Exception
    {
        readableByteChannel = Channels.newChannel(new ByteArrayInputStream(EXPECTED_MSG_2.getBytes()));
        final FixClient fixClient = buildFixClient();
        fixClient.subscribeToAllMessages(new AssertingFixMessageHandler(EXPECTED_MSG_2));
        fixClient.connect();
        final boolean await = countDownLatch.await(5, TimeUnit.SECONDS);
        assertTrue(await);
    }

    @Test
    public void shouldSendFixMessage()
    {
        readableByteChannel = Channels.newChannel(new ByteArrayInputStream(new byte[0]));
        final FixClient fixClient = buildFixClient();
        fixClient.connect();
        final FixMessageBuilder fixMessageBuilder = new FixMessageBuilder();
        fixMessageBuilder.messageType(MsgType.MARKET_DATA_SNAPSHOT).refSeqNum(3).refSeqNum(7);

        fixClient.send(fixMessageBuilder.build());
        assertEquals(byteArrayOutputStream.toString(), "8=FIX.4.4\u00019=15\u000135=W\u000145=3\u000145=7\u000110=179\u0001");
    }

    private final class AssertingFixMessageHandler implements FixMessageHandler
    {
        private final String expectedMessage;

        private AssertingFixMessageHandler(final String expectedMessage)
        {
            this.expectedMessage = expectedMessage;
        }

        @Override
        public void onFixMessage(final FixMessage fixMessage)
        {
            if (expectedMessage.equals(fixMessage.toFixString()))
            {
                countDownLatch.countDown();
            }
            else
            {
                throw new RuntimeException("Expected: '" + expectedMessage + "'  message does not match actual: " + fixMessage.toFixString());
            }

        }
    }

    private FixClient buildFixClient()
    {
        return FixClientFactory.createFixClient(new IntegrationSocketFactory(readableByteChannel, writableByteChannel));
    }
}
