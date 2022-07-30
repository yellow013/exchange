package exchange.lob.fix.incoming;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class FixMessageStreamFactory implements FixTagHandler
{
    private final Multimap<Integer, String> multimap = LinkedListMultimap.create();
    private final FixMessageHandler handler;

    public FixMessageStreamFactory(final FixMessageHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public void messageStart()
    {
        multimap.clear();
    }

    @Override
    public void onTag(final int tagIdentity, final byte[] message, final int tagValueOffset, final int tagValueLength)
    {
        multimap.put(tagIdentity, new String(message, tagValueOffset, tagValueLength));
    }

    @Override
    public boolean isFinished()
    {
        return false;
    }

    @Override
    public void messageEnd()
    {
        handler.onFixMessage(new FixMessage(multimap));
    }


}
