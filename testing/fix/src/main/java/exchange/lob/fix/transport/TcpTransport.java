package exchange.lob.fix.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;


public class TcpTransport implements Transport, ConnectionObserver
{

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpTransport.class);

    private final PublishingConnectionObserver publishingTransportObserver;
    private final TransportConfig transportConfig;

    private volatile SocketChannel socketChannel;
    private volatile DelegatingServerSocketChannel serverSocketChannel;

    private InetSocketAddress socketAddress;
    private SocketFactory asyncTcpSocketFactory;

    public TcpTransport(final PublishingConnectionObserver publishingTransportObserver, final InetSocketAddress socketAddress,
                        final SocketFactory asyncTcpSocketFactory, final TransportConfig transportConfig)
    {
        this.publishingTransportObserver = publishingTransportObserver;
        this.socketAddress = socketAddress;
        this.asyncTcpSocketFactory = asyncTcpSocketFactory;

        this.transportConfig = transportConfig;
    }

    @Override
    public void connect()
    {
        asyncTcpSocketFactory.createSocketOnOutgoingConnection(socketAddress, new SocketFactory.SocketEstablishedCallback()
        {
            @Override
            public void onSocketEstablished(final SocketChannel socketChannel)
            {
                TcpTransport.this.socketChannel = socketChannel;
                publishingTransportObserver.connectionEstablished();
            }
        });

    }

    @Override
    public void listen()
    {
        serverSocketChannel = asyncTcpSocketFactory.bind(socketAddress);
        acceptNewConnection();
    }


    @Override
    public void stopListening()
    {
        try
        {
            if (serverSocketChannel != null)
            {
                serverSocketChannel.close();
                serverSocketChannel = null;
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to close listener socket", e);
        }
    }

    @Override
    public ReadableByteChannel getReadableByteChannel()
    {
        return socketChannel;

    }

    @Override
    public WritableByteChannel getWritableByteChannel()
    {
        return socketChannel;
    }

    @Override
    public void killSocket()
    {
        try
        {
            socketChannel.socket().setSoLinger(true, 0);
            socketChannel.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to kill socket", e);

        }
    }

    @Override
    public void close()
    {
        try
        {
            if (socketChannel != null)
            {
                socketChannel.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to close socket", e);
        }
    }

    @Override
    public boolean isConnected()
    {
        return socketChannel != null && socketChannel.isConnected();
    }

    @Override
    public void registerTransportObserver(final ConnectionObserver connectionObserver)
    {
        publishingTransportObserver.addObserver(connectionObserver);
    }

    @Override
    public void unregisterTransportObserver(final ConnectionObserver connectionObserver)
    {
        publishingTransportObserver.removeObserver(connectionObserver);
    }

    @Override
    public void connectionEstablished()
    {
    }

    @Override
    public void connectionClosed()
    {
        if (serverSocketChannel != null)
        {
            if (!transportConfig.shouldStayListening())
            {
                try
                {
                    serverSocketChannel.close();
                }
                catch (IOException e)
                {
                    LOGGER.error("Failed to stop listening on port", e);
                }
            }
            else
            {
                acceptNewConnection();
            }
        }
    }

    private void acceptNewConnection()
    {
        asyncTcpSocketFactory.createSocketOnIncomingConnection(serverSocketChannel, new SocketFactory.SocketEstablishedCallback()
        {
            @Override
            public void onSocketEstablished(final SocketChannel socketChannel)
            {
                TcpTransport.this.socketChannel = socketChannel;
                publishingTransportObserver.connectionEstablished();
            }
        });
    }
}
