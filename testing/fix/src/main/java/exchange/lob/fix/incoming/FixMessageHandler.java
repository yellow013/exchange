package exchange.lob.fix.incoming;

public interface FixMessageHandler
{
    void onFixMessage(FixMessage fixMessage);
}
