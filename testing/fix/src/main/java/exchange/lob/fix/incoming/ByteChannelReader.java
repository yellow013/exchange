package exchange.lob.fix.incoming;

import exchange.lob.fix.concurrent.Blocker;
import exchange.lob.fix.transport.ConnectionObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;

public class ByteChannelReader
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteChannelReader.class);
    private static final int BUFFER_SIZE = 1024;
    private final ByteStreamMessageParser byteStreamMessageParser;
    private final Blocker blocker;
    private final ConnectionObserver connectionObserver;
    private final ByteBuffer buffer;

    public ByteChannelReader(final ByteStreamMessageParser byteStreamMessageParser, final Blocker blocker, final ConnectionObserver connectionObserver)
    {
        this.byteStreamMessageParser = byteStreamMessageParser;
        this.blocker = blocker;
        this.connectionObserver = connectionObserver;
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public void blockingStart(final ReadableByteChannel readableByteChannel)
    {
        try
        {
            while ((readableByteChannel.read(buffer)) != -1)
            {
                blocker.mayWait();
                buffer.flip();
                byteStreamMessageParser.parse(buffer);
                buffer.clear();
            }

        }
        catch (final ClosedChannelException e)
        {
            //Yes closed.
        }
        catch (final IOException e)
        {
            LOGGER.error("An error occurred trying to read from the socket", e);
        }
        finally
        {
            if (readableByteChannel != null)
            {
                try
                {
                    if (readableByteChannel.isOpen())
                    {
                        readableByteChannel.close();
                    }
                }
                catch (IOException e)
                {
                    //I don't care
                }
                connectionObserver.connectionClosed();
            }
        }
    }
}
