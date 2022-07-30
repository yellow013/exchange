package exchange.lob.fix.transport;

public class TransportConfigImpl implements TransportConfig
{
    final boolean stayListening;

    public TransportConfigImpl(final boolean stayListening)
    {
        this.stayListening = stayListening;
    }

    @Override
    public boolean shouldStayListening()
    {
        return stayListening;
    }
}
