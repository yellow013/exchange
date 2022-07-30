package exchange.lob.fix.outgoing;

import exchange.lob.fix.fields.EncryptMethod;
import exchange.lob.fix.fields.MsgType;
import exchange.lob.fix.transport.ConnectionObserver;
import exchange.lob.fix.transport.TransportClosedException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OutboundMessageHandlerTest
{
    private Mockery mockery;
    private WritableByteChannel writableByteChannel;

    private OutboundMessageHandler handler;
    private ConnectionObserver connectionObserver;

    @BeforeEach
    public void setUp()
    {
        mockery = new Mockery();
        writableByteChannel = mockery.mock(WritableByteChannel.class);
        connectionObserver = mockery.mock(ConnectionObserver.class);
        handler = new OutboundMessageHandler(connectionObserver);
        handler.initialiseOutboundChannel(writableByteChannel);
    }

    @Test
    public void shouldPlaceMultipleMessagesInSameBuffer() throws Exception
    {
        final FixMessage loginMessage = new FixMessageBuilder().messageType(MsgType.LOGIN).msgSeqNum(1).senderCompID("username").targetCompID("LMXBL")
            .sendingTimeNow().username("username").password("password").heartBtInt(100000)
            .encryptMethod(EncryptMethod.NONE)
            .build();

        final FixMessage logoutMessage = new FixMessageBuilder().messageType(MsgType.LOGOUT).msgSeqNum(2).senderCompID("username").targetCompID("LMXBL")
            .sendingTimeNow()
            .build();

        final List<FixMessage> expected = newArrayList(loginMessage, logoutMessage);

        mockery.checking(new Expectations()
        {
            {
                one(writableByteChannel).write(with(new ByteBufferMatcher(expected)));
            }
        });

        handler.send(expected);
    }

    @Test
    public void shouldNotifyTransportObserverIfAClosedChannelExceptionIsThrownWhileWriting()
    {
        assertThrows(TransportClosedException.class, () -> {
            mockery.checking(new Expectations()
            {
                {
                    //when
                    one(writableByteChannel).write(with(any(ByteBuffer.class)));
                    will(throwException(new ClosedChannelException()));

                    //then
                    one(connectionObserver).connectionClosed();
                }
            });

            handler.send(new FixMessageBuilder().build());
        });
    }

    @Test
    public void shouldNotifyTransportObserverIfAnClosedChannelExceptionIsThrownWhileWritingACollection()
    {
        assertThrows(TransportClosedException.class, () -> {
            mockery.checking(new Expectations()
            {
                {
                    //when
                    one(writableByteChannel).write(with(any(ByteBuffer.class)));
                    will(throwException(new ClosedChannelException()));

                    //then
                    one(connectionObserver).connectionClosed();
                }
            });

            handler.send(List.of(new FixMessageBuilder().build()));
        });
    }

    @Test
    public void shouldNotifyTransportObserverWhenAnIOExceptionIsThrownAndCloseTheTransportWhileWritingACollection()
    {
        assertThrows(TransportClosedException.class, () -> {
            mockery.checking(new Expectations()
            {
                {
                    //when
                    one(writableByteChannel).write(with(any(ByteBuffer.class)));
                    will(throwException(new IOException("Broken pipe")));

                    //then
                    one(connectionObserver).connectionClosed();
                    one(writableByteChannel).close();
                }
            });

            handler.send(List.of(new FixMessageBuilder().build()));
        });
    }

    @Test
    public void shouldNotifyTransportObserverWhenAnIOExceptionIsThrownAndCloseTheTransportWhileWriting()
    {
        assertThrows(TransportClosedException.class, () -> {
            mockery.checking(new Expectations()
            {
                {
                    //when
                    one(writableByteChannel).write(with(any(ByteBuffer.class)));
                    will(throwException(new IOException("Broken pipe")));

                    //then
                    one(connectionObserver).connectionClosed();
                    one(writableByteChannel).close();
                }
            });

            handler.send(new FixMessageBuilder().build());
        });
    }
}
