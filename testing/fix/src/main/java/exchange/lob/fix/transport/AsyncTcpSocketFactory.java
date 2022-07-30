package exchange.lob.fix.transport;

import exchange.lob.fix.exceptions.GeneralRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;


public class AsyncTcpSocketFactory implements SocketFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTcpSocketFactory.class);
    private static final int SINGLE_CONNECTION_BACKLOG = 1;
    private final ExecutorService executorService;

    public AsyncTcpSocketFactory(final ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    @Override
    public DelegatingServerSocketChannel bind(final InetSocketAddress socketAddress)
    {
        try
        {
            final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(socketAddress, SINGLE_CONNECTION_BACKLOG);
            return new DelegatingServerSocketChannel(serverSocketChannel);
        }
        catch (java.net.BindException e)
        {
            throw new BindException(String.format("Failed to bind to interface:%s, port:%d", socketAddress.getHostName(), socketAddress.getPort()), e);
        }
        catch (IOException e)
        {
            LOGGER.error("IOException occurred while waiting for a connection", e);
            throw new GeneralRuntimeException("Failed to open or bind server socket channel", e);
        }
    }

    @Override
    public void createSocketOnIncomingConnection(final DelegatingServerSocketChannel serverSocketChannel, final SocketEstablishedCallback socketEstablishedCallback)
    {
        executorService.execute(new Runnable()
        {
            @Override
            public void run()
            {
                final SocketChannel socketChannel;
                try
                {
                    try
                    {
                        socketChannel = serverSocketChannel.accept();
                        socketEstablishedCallback.onSocketEstablished(socketChannel);
                    }
                    catch (AsynchronousCloseException e)
                    {
                        LOGGER.warn("Server socket closed by another thread,  while waiting to accept a new inbound connection.");
                    }
                    catch (ClosedChannelException e)
                    {
                        LOGGER.warn("Server socket closed,  while waiting to accept a new inbound connection.", e);
                    }
                    catch (IOException e)
                    {
                        LOGGER.error("IOException occurred while waiting for a connection", e);
                        throw new GeneralRuntimeException("Failed to accept server socket channel", e);
                    }

                }
                catch (RuntimeException e)
                {
                    LOGGER.error("Exception thrown: ", e);
                }
            }
        });
    }

    @Override
    public void createSocketOnOutgoingConnection(final InetSocketAddress socketAddress, final SocketEstablishedCallback socketEstablishedCallback)
    {
        executorService.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final SocketChannel socketChannel;
                    try
                    {
                        socketChannel = SocketChannel.open(socketAddress);
                        socketEstablishedCallback.onSocketEstablished(socketChannel);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(String.format("Can't connect - host:%s, port:%d", socketAddress.getHostName(), socketAddress.getPort()), e);
                    }
                }
                catch (RuntimeException e)
                {
                    LOGGER.error("Caught Exception while executing", e);
                }
            }

        });
    }
}