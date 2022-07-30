package exchange.lob.fix.transport;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface Transport extends TransportOperations
{
    ReadableByteChannel getReadableByteChannel();

    WritableByteChannel getWritableByteChannel();

}
