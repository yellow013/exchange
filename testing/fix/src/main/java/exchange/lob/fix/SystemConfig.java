package exchange.lob.fix;

import exchange.lob.fix.transport.TransportConfig;

public class SystemConfig implements TransportConfig
{
    private final boolean stayListening;

    /**
     * @param stayListening when set to true, Nanofix will continue listening for additional inbound connections
     *                      after an existing connection is dropped.
     */
    public SystemConfig(final boolean stayListening)
    {
        this.stayListening = stayListening;
    }

    @Override
    public boolean shouldStayListening()
    {
        return stayListening;
    }
}
