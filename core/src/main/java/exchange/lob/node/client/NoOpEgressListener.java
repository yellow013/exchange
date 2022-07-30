package exchange.lob.node.client;

import io.aeron.cluster.client.EgressListener;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;

public class NoOpEgressListener implements EgressListener
{
    public static final EgressListener INSTANCE = new NoOpEgressListener();

    @Override
    public void onMessage(
        final long clusterSessionId,
        final long timestamp,
        final DirectBuffer buffer,
        final int offset,
        final int length,
        final Header header
    )
    {

    }
}
