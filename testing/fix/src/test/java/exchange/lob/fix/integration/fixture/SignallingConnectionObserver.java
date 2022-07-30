package exchange.lob.fix.integration.fixture;

import exchange.lob.fix.transport.ConnectionObserver;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class SignallingConnectionObserver implements ConnectionObserver
{
    private final Lock lock;
    private final Condition connectionEstablishedCondition;
    private final Condition connectionClosedCondition;

    public SignallingConnectionObserver(final Lock lock, final Condition connectionEstablishedCondition, final Condition connectionClosedCondition)
    {
        this.lock = lock;
        this.connectionEstablishedCondition = connectionEstablishedCondition;
        this.connectionClosedCondition = connectionClosedCondition;
    }

    @Override
    public void connectionEstablished()
    {
        lock.lock();
        connectionEstablishedCondition.signalAll();
        lock.unlock();

    }

    @Override
    public void connectionClosed()
    {
        lock.lock();
        connectionClosedCondition.signalAll();
        lock.unlock();
    }
}
