package exchange.lob.fix;

import exchange.lob.fix.concurrent.NamedThreadFactory;
import exchange.lob.fix.concurrent.ThreadBlocker;
import exchange.lob.fix.incoming.*;
import exchange.lob.fix.outgoing.OutboundMessageHandler;
import exchange.lob.fix.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FixClientFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FixClientFactory.class);

    static final Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = new Thread.UncaughtExceptionHandler()
    {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable)
        {
            LOGGER.error("Uncaught Exception thrown in thread: " + thread.getName(), throwable);
        }
    };

    private FixClientFactory()
    {
    }

    /**
     * Max Fix Message size
     */
    private static final int MAX_MESSAGE_SIZE = 2000;

    /**
     * Create an initiating fix client that will connect to the host on connect()
     *
     * @param host hostname of server to connect to.
     * @param port tcp port number int between 0 and 65535
     */
    public static FixClient createFixClient(final String host, final int port)
    {
        return createFixClient(new InetSocketAddress(host, port), new SystemConfig(false));
    }

    /**
     * Create an listening fix client that will listen for inbound tcp connections on port
     *
     * @param port tcp port number int between 0 and 65535
     */
    public static FixClient createFixClient(final int port)
    {
        return createFixClient(new InetSocketAddress(port), new SystemConfig(false));
    }

    /**
     * Create a fix client
     *
     * @param host         to bind to if listening for a connection or host to connect to when attempting to connect.
     * @param port         tcp port number int between 0 and 65535
     * @param systemConfig additional NanoFix configuration
     */
    public static FixClient createFixClient(final String host, final int port, final SystemConfig systemConfig)
    {
        return createFixClient(new InetSocketAddress(host, port), systemConfig);
    }

    public static FixClient createFixClient(final SocketFactory socketFactory)
    {
        final PublishingConnectionObserver publishingTransportObserver = new PublishingConnectionObserver();
        final TcpTransport transport = new TcpTransport(publishingTransportObserver, null, socketFactory, new TransportConfigImpl(false));
        publishingTransportObserver.addObserver(transport);
        return buildFixClient(transport, publishingTransportObserver, MAX_MESSAGE_SIZE);
    }

    /**
     * Build an initiating or listening {@link FixClient} based on the values contained in {@link FixClientConfiguration}
     *
     * @param fixClientConfiguration Contains configuration that determines the type of {@link FixClient}
     */
    public static FixClient createFixClient(final FixClientConfiguration fixClientConfiguration)
    {
        final InetSocketAddress socketAddress = fixClientConfiguration.getSocketAddress();
        final SocketFactory socketFactory = fixClientConfiguration.getSocketFactory();
        final SystemConfig systemConfig = fixClientConfiguration.getSystemConfig();
        final int maxMessageSize = fixClientConfiguration.getMaxMessageSize();

        final PublishingConnectionObserver publishingTransportObserver = new PublishingConnectionObserver();
        final TcpTransport transport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, systemConfig);
        publishingTransportObserver.addObserver(transport);
        return buildFixClient(transport, publishingTransportObserver, maxMessageSize);
    }

    private static FixClient createFixClient(final InetSocketAddress socketAddress, final SystemConfig systemConfig)
    {
        final PublishingConnectionObserver publishingTransportObserver = new PublishingConnectionObserver();

        final ExecutorService executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("InboundConnection", true, UNCAUGHT_EXCEPTION_HANDLER));
        final AsyncTcpSocketFactory asyncTcpSocketFactory = new AsyncTcpSocketFactory(executorService);
        final TcpTransport transport = new TcpTransport(publishingTransportObserver, socketAddress, asyncTcpSocketFactory, systemConfig);
        publishingTransportObserver.addObserver(transport);
        return buildFixClient(transport, publishingTransportObserver, MAX_MESSAGE_SIZE);
    }

    private static FixClient buildFixClient(final Transport transport, final PublishingConnectionObserver publishingTransportObserver, final int maxMessageSize)
    {
        final FixStreamMessageParser fixStreamMessageParser = new FixStreamMessageParser(maxMessageSize);
        final ThreadBlocker messageConsumingThreadBlocker = new ThreadBlocker();
        final FixMessagePublisher fixMessagePublisher = new FixMessagePublisher();
        fixStreamMessageParser.initialise(new RawFixMessageHandler(new FixTagParser(new FixMessageStreamFactory(fixMessagePublisher))));
        final OutboundMessageHandler outboundMessageSender = new OutboundMessageHandler(publishingTransportObserver);

        final ByteChannelReader inputStreamReader = new ByteChannelReader(fixStreamMessageParser, messageConsumingThreadBlocker, publishingTransportObserver);
        final ExecutorService channelReaderExecutorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("channelReader", true, UNCAUGHT_EXCEPTION_HANDLER));
        final ChannelInitializer channelInitializer = new ChannelInitializer(transport, inputStreamReader, outboundMessageSender, channelReaderExecutorService);
        publishingTransportObserver.addObserver(channelInitializer);

        return new FixClient(fixMessagePublisher, channelInitializer, transport, new FixSession(outboundMessageSender), messageConsumingThreadBlocker);
    }
}
