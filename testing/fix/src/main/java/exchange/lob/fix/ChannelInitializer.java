package exchange.lob.fix;

import exchange.lob.fix.incoming.ByteChannelReader;
import exchange.lob.fix.outgoing.OutboundMessageHandler;
import exchange.lob.fix.transport.ConnectionObserver;
import exchange.lob.fix.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;


class ChannelInitializer implements ConnectionObserver
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelInitializer.class);
    private final Transport transport;
    private final ByteChannelReader inputStreamReader;
    private final OutboundMessageHandler outboundMessageSender;
    private final Executor channelReaderExecutorService;

    private volatile CountDownLatch countDownLatch = new CountDownLatch(1);


    public ChannelInitializer(final Transport transport, final ByteChannelReader inputStreamReader, final OutboundMessageHandler outboundMessageSender, Executor channelReaderExecutorService)
    {
        this.channelReaderExecutorService = channelReaderExecutorService;
        this.transport = transport;
        this.inputStreamReader = inputStreamReader;
        this.outboundMessageSender = outboundMessageSender;
    }

    @Override
    public void connectionEstablished()
    {
        outboundMessageSender.initialiseOutboundChannel(transport.getWritableByteChannel());
        channelReaderExecutorService.execute(new Runnable()
        {
            @Override
            public void run()
            {
                final ReadableByteChannel readableByteChannel = transport.getReadableByteChannel();
                try
                {
                    inputStreamReader.blockingStart(readableByteChannel);
                }
                catch (RuntimeException e)
                {
                    LOGGER.error("Exception thrown while reading from stream, channel: " + readableByteChannel, e);
                }
            }
        });
        countDownLatch.countDown();
    }

    public void awaitConnection() throws InterruptedException
    {
        countDownLatch.await();
    }

    @Override
    public void connectionClosed()
    {
        countDownLatch = new CountDownLatch(1);
    }
}
