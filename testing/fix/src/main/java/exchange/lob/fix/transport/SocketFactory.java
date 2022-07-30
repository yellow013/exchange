package exchange.lob.fix.transport;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public interface SocketFactory
{
    DelegatingServerSocketChannel bind(InetSocketAddress socketAddress);

    void createSocketOnIncomingConnection(DelegatingServerSocketChannel serverSocketChannel, SocketEstablishedCallback socketEstablishedCallback);

    void createSocketOnOutgoingConnection(InetSocketAddress socketAddress, SocketEstablishedCallback socketEstablishedCallback);

    public interface SocketEstablishedCallback
    {
        void onSocketEstablished(SocketChannel socketChannel);
    }
}
