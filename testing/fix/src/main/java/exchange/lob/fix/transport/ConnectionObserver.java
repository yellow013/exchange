package exchange.lob.fix.transport;

public interface ConnectionObserver
{
    void connectionEstablished();

    void connectionClosed();
}
