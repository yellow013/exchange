package exchange.lob.fix;

import exchange.lob.fix.incoming.ByteChannelReader;
import exchange.lob.fix.outgoing.OutboundMessageHandler;
import exchange.lob.fix.transport.Transport;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.DeterministicExecutor;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ChannelInitializerTest
{

    private Transport transport;
    private Mockery mockery;
    private ByteChannelReader byteChannelReader;
    private OutboundMessageHandler outboundMessageHandler;
    private DeterministicExecutor deterministicExecutor;
    private WritableByteChannel writableByteChannel;
    private ReadableByteChannel readableByteChannel;

    @BeforeEach
    public void setUp() throws Exception
    {
        mockery = new Mockery();
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
        transport = mockery.mock(Transport.class);
        byteChannelReader = mockery.mock(ByteChannelReader.class);
        outboundMessageHandler = mockery.mock(OutboundMessageHandler.class);
        writableByteChannel = mockery.mock(WritableByteChannel.class);
        readableByteChannel = mockery.mock(ReadableByteChannel.class);

        mockery.checking(new Expectations()
        {
            {
                ignoring(writableByteChannel);
            }
        });
    }

    @Test
    public void shouldStartByteChannelReaderOnConnectionEstablished() throws Exception
    {
        deterministicExecutor = new DeterministicExecutor();
        final ChannelInitializer channelInitializer = new ChannelInitializer(transport, byteChannelReader, outboundMessageHandler, deterministicExecutor);

        mockery.checking(new Expectations()
        {
            {
                one(transport).getWritableByteChannel();
                will(returnValue(writableByteChannel));

                one(outboundMessageHandler).initialiseOutboundChannel(writableByteChannel);
            }
        });
        channelInitializer.connectionEstablished();

        mockery.checking(new Expectations()
        {
            {
                one(transport).getReadableByteChannel();
                will(returnValue(readableByteChannel));

                one(byteChannelReader).blockingStart(readableByteChannel);
            }
        });
        deterministicExecutor.runPendingCommands();

    }

}
