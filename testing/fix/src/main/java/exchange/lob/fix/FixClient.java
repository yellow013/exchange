package exchange.lob.fix;

import exchange.lob.fix.concurrent.ThreadBlocker;
import exchange.lob.fix.incoming.FixMessageHandler;
import exchange.lob.fix.incoming.FixMessagePublisher;
import exchange.lob.fix.outgoing.FixMessage;
import exchange.lob.fix.transport.ConnectionObserver;
import exchange.lob.fix.transport.TransportOperations;

import java.util.Collection;

/**
 * The main class used to interact with the fix client.
 */
public class FixClient
{
    private final FixMessagePublisher fixMessagePublisher;
    private final ChannelInitializer channelInitializer;
    private final TransportOperations transportOps;
    private final FixSession fixSession;
    private final ThreadBlocker messageConsumingThreadBlocker;

    FixClient(final FixMessagePublisher fixMessagePublisher, final ChannelInitializer channelInitializer, final TransportOperations transportOps,
                     final FixSession fixSession, final ThreadBlocker messageConsumingThreadBlocker)
    {
        this.fixMessagePublisher = fixMessagePublisher;
        this.channelInitializer = channelInitializer;
        this.transportOps = transportOps;
        this.fixSession = fixSession;
        this.messageConsumingThreadBlocker = messageConsumingThreadBlocker;
    }

    /**
     * Sends a collection of FIX messages.
     *
     * @param messages a collection of messages.
     */
    public void send(final Collection<FixMessage> messages)
    {
        fixSession.send(messages);
    }

    /**
     * Sends a single FIX message.
     *
     * @param message a FIX messages.
     */
    public void send(final FixMessage message)
    {
        fixSession.send(message);
    }

    /**
     * Sends an arbitrary string.
     *
     * @param message a FIX messages.
     */
    public void send(final String message)
    {
        fixSession.send(message);
    }

    /**
     * Sends a array of bytes string.
     *
     * @param bytes a FIX messages.
     */
    public void send(final byte[] bytes)
    {
        fixSession.send(bytes);
    }

    /**
     * Initiates a TCP connection with the remote host specified on construction..
     */
    public void connect()
    {
        transportOps.connect();
        try
        {
            channelInitializer.awaitConnection();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Interrupted while waiting for channel initialization");
        }
    }

    /**
     * Waits for an inbound TCP connection on the interface and port specified on construction.
     */
    public void listen()
    {
        transportOps.listen();
    }

    /**
     * Stops listening on the TCP socket.
     */
    public void stopListening()
    {
        transportOps.stopListening();
    }

    /**
     * Terminates the TCP connection with a TCP ( RST, ACK).
     */
    public void killSocket()
    {
        transportOps.killSocket();
    }

    /**
     * Attempts to gracefully close the TCP socket.
     */
    public void close()
    {
        transportOps.close();
    }

    /**
     * Checks if the transport is connected.
     */
    public boolean isConnected()
    {
        return transportOps.isConnected();
    }

    /**
     * Blocks until a connection is established.
     */
    public void awaitConnection() throws InterruptedException
    {
        channelInitializer.awaitConnection();
    }

    /**
     * Registers an observer that will be notified on connect and disconnect events.
     *
     * @param connectionObserver receives connection events.
     */
    public void registerTransportObserver(ConnectionObserver connectionObserver)
    {
        transportOps.registerTransportObserver(connectionObserver);
    }

    /**
     * Removes a registered observer that will be notified on connect and disconnect events.
     *
     * @param connectionObserver receives connection events.
     */
    public void unregisterTransportObserver(ConnectionObserver connectionObserver)
    {
        transportOps.unregisterTransportObserver(connectionObserver);
    }

    /**
     * Subscribe to all inbound messages.
     */
    public void subscribeToAllMessages(final FixMessageHandler fixMessageHandler)
    {
        fixMessagePublisher.subscribeToAllMessages(fixMessageHandler);
    }

    /**
     * Stop reading messages from the transport's byte channel
     */
    public void pauseMessageConsumer()
    {
        messageConsumingThreadBlocker.pause();
    }

    /**
     * Resume reading messages from the transport's byte channel
     */
    public void resumeMessageConsumer()
    {
        messageConsumingThreadBlocker.resume();
    }


}
