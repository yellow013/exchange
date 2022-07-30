package exchange.lob.fix.transport;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class PublishingConnectionObserver implements ConnectionObserver
{

    private final Set<ConnectionObserver> observers = new CopyOnWriteArraySet<ConnectionObserver>();

    public void addObserver(final ConnectionObserver connectionObserver)
    {
        observers.add(connectionObserver);
    }

    public void removeObserver(final ConnectionObserver connectionObserver)
    {
        observers.remove(connectionObserver);
    }

    @Override
    public void connectionEstablished()
    {
        for (ConnectionObserver observer : observers)
        {
            observer.connectionEstablished();
        }
    }

    @Override
    public void connectionClosed()
    {
        for (ConnectionObserver observer : observers)
        {
            observer.connectionClosed();
        }
    }
}
