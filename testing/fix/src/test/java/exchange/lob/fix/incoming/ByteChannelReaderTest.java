package exchange.lob.fix.incoming;

import exchange.lob.fix.concurrent.ThreadBlocker;
import exchange.lob.fix.transport.ConnectionObserver;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ByteChannelReaderTest
{


    private ByteChannelReader inputStreamReader;
    private final Mockery mockery = new Mockery();
    private ReadableByteChannel readableByteChannel;
    private ByteStreamMessageParser byteStreamMessageParser;
    private ConnectionObserver connectionObserver;

    @BeforeEach
    public void setUp()
    {
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
        readableByteChannel = mockery.mock(ReadableByteChannel.class);
        byteStreamMessageParser = mockery.mock(ByteStreamMessageParser.class);
        connectionObserver = mockery.mock(ConnectionObserver.class);
        inputStreamReader = new ByteChannelReader(byteStreamMessageParser, new ThreadBlocker(), connectionObserver);
    }

    @Test
    public void shouldPassBytesFromInputStreamToParser() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                ignoring(connectionObserver).connectionClosed();
                allowing(readableByteChannel).isOpen();
                will(returnValue(false));

                one(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(returnValue(1));
                one(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(returnValue(-1));

                one(byteStreamMessageParser).parse(with(any(ByteBuffer.class)));
            }
        });

        //when
        inputStreamReader.blockingStart(readableByteChannel);
    }

    @Test
    public void shouldCloseInputStreamIfExceptionIsThrownAndInputStreamIsStillOpen()
    {
        assertThrows(RuntimeException.class, () -> {
            mockery.checking(new Expectations()
            {
                {
                    ignoring(connectionObserver).connectionClosed();
                    allowing(byteStreamMessageParser).parse(with(any(ByteBuffer.class)));

                    //when
                    one(readableByteChannel).read(with(any(ByteBuffer.class)));
                    will(throwException(new RuntimeException("boom!")));


                    //then
                    one(readableByteChannel).isOpen();
                    will(returnValue(true));
                    one(readableByteChannel).close();

                }
            });

            //when
            inputStreamReader.blockingStart(readableByteChannel);
        });
    }

    @Test
    public void shouldNotifyTransportObserverWhenConnectionIsClosed() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                ignoring(readableByteChannel).isOpen();
                allowing(byteStreamMessageParser).parse(with(any(ByteBuffer.class)));

                //when
                allowing(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(throwException(new ClosedChannelException()));
                allowing(readableByteChannel).close();


                //then
                one(connectionObserver).connectionClosed();
            }
        });

        //when
        inputStreamReader.blockingStart(readableByteChannel);
    }

    @Test
    public void shouldNotifyTransportObserverWhenIOExceptionIsThrown() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                ignoring(readableByteChannel).isOpen();
                allowing(byteStreamMessageParser).parse(with(any(ByteBuffer.class)));

                //when
                allowing(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(throwException(new IOException()));
                allowing(readableByteChannel).close();


                //then
                one(connectionObserver).connectionClosed();
            }
        });

        //when
        inputStreamReader.blockingStart(readableByteChannel);
    }

    @Test
    public void shouldCloseChannelWhenEndOfStreamIsReached() throws Exception
    {
        expectEndOfInputStream();

        mockery.checking(new Expectations()
        {
            {
                one(readableByteChannel).isOpen();
                will(returnValue(true));

                one(readableByteChannel).close();
                one(connectionObserver).connectionClosed();
            }
        });

        //when
        inputStreamReader.blockingStart(readableByteChannel);
    }

    private void expectEndOfInputStream() throws IOException
    {
        mockery.checking(new Expectations()
        {
            {
                one(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(returnValue(-1));
            }
        });

    }


}
