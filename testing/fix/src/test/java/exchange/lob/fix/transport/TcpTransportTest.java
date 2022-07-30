package exchange.lob.fix.transport;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;


public class TcpTransportTest
{

    private Mockery mockery;
    private PublishingConnectionObserver publishingTransportObserver;
    private SocketFactory socketFactory;
    private DelegatingServerSocketChannel serverSocketChannel;
    private InetSocketAddress socketAddress;

    @BeforeEach
    public void setUp()
    {
        mockery = new Mockery();
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
        publishingTransportObserver = mockery.mock(PublishingConnectionObserver.class);
        serverSocketChannel = mockery.mock(DelegatingServerSocketChannel.class);
        socketFactory = mockery.mock(SocketFactory.class);
        socketAddress = new InetSocketAddress("host", 222);
    }

    @Test
    public void shouldBindToSocketOnListen()
    {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(false));

        mockery.checking(new Expectations()
        {
            {
                one(socketFactory).bind(socketAddress);
                will(returnValue(serverSocketChannel));
                one(socketFactory).createSocketOnIncomingConnection(with(serverSocketChannel), with(any(SocketFactory.SocketEstablishedCallback.class)));
            }
        });
        tcpTransport.listen();
    }

    @Test
    public void shouldRemoveBindOnWhenTheConnectionClosesByDefault() throws Exception
    {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(false));

        mockery.checking(new Expectations()
        {
            {

                allowing(socketFactory).bind(socketAddress);
                will(returnValue(serverSocketChannel));
                allowing(socketFactory).createSocketOnIncomingConnection(with(serverSocketChannel), with(any(SocketFactory.SocketEstablishedCallback.class)));

                one(serverSocketChannel).close();
            }
        });

        tcpTransport.listen();
        tcpTransport.connectionClosed();
    }

    @Test
    public void shouldStayBoundOnWhenTheConnectionClosesIfConfiguredToMaintainBind() throws Exception
    {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(true));

        mockery.checking(new Expectations()
        {
            {

                one(socketFactory).bind(socketAddress);
                will(returnValue(serverSocketChannel));
                one(socketFactory).createSocketOnIncomingConnection(with(serverSocketChannel), with(any(SocketFactory.SocketEstablishedCallback.class)));

                never(serverSocketChannel).close();
                one(socketFactory).createSocketOnIncomingConnection(with(serverSocketChannel), with(any(SocketFactory.SocketEstablishedCallback.class)));

            }
        });

        tcpTransport.listen();
        tcpTransport.connectionClosed();
    }

    @Test
    public void shouldCreateAnOutboundSocketWhenConnecting()
    {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(false));

        mockery.checking(new Expectations()
        {
            {
                one(socketFactory).createSocketOnOutgoingConnection(with(socketAddress), with(any(SocketFactory.SocketEstablishedCallback.class)));
            }
        });

        tcpTransport.connect();
    }

    @Test
    public void shouldNotAcceptTryAcceptNewConnectionsWhenInitiatingOutboundConnections()
    {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(true));

        mockery.checking(new Expectations()
        {
            {
                one(socketFactory).createSocketOnOutgoingConnection(with(socketAddress), with(any(SocketFactory.SocketEstablishedCallback.class)));
            }
        });

        tcpTransport.connect();
        tcpTransport.connectionClosed();
    }

}
