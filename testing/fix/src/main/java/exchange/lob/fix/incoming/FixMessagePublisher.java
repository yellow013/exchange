package exchange.lob.fix.incoming;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FixMessagePublisher implements FixMessageHandler
{
    List<FixMessageHandler> handlers = new CopyOnWriteArrayList<FixMessageHandler>();

    public void subscribeToAllMessages(final FixMessageHandler fixMessageHandler)
    {
        handlers.add(fixMessageHandler);
    }

    @Override
    public void onFixMessage(final FixMessage fixMessage)
    {
        for (FixMessageHandler fixMessageHandler : handlers)
        {
            fixMessageHandler.onFixMessage(fixMessage);
        }
    }
}
