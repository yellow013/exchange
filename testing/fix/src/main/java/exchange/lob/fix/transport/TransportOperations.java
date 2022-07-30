package exchange.lob.fix.transport;

public interface TransportOperations
{
    void connect();

    void listen();

    void stopListening();

    void killSocket();

    void close();

    boolean isConnected();

    void registerTransportObserver(final ConnectionObserver connectionObserver);

    void unregisterTransportObserver(final ConnectionObserver connectionObserver);
}
