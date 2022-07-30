package exchange.lob.fix.integration;

import exchange.lob.fix.FixClient;
import exchange.lob.fix.FixClientFactory;
import exchange.lob.fix.integration.fixture.IntegrationSocketFactory;
import exchange.lob.fix.integration.fixture.SignallingConnectionObserver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FixClientLifecycleTest
{
    @Test
    public void shouldBeAbleToStartListeningAgainAfterFirstStartingAndThenStopping() throws InterruptedException
    {
        Lock lock = new ReentrantLock();
        final Condition connectionClosedCondition = lock.newCondition();
        final Condition connectionEstablishedCondition = lock.newCondition();

        final FixClient fixClient = FixClientFactory.createFixClient(9990);
        fixClient.registerTransportObserver(new SignallingConnectionObserver(lock, connectionEstablishedCondition, connectionClosedCondition));


        fixClient.listen();

        try
        {
            lock.lock();
            final FixClient fixClient2 = FixClientFactory.createFixClient("localhost", 9990);
            fixClient2.connect();
            final boolean connectionEstablished = connectionEstablishedCondition.await(5, TimeUnit.SECONDS);
            assertTrue(connectionEstablished);
            fixClient2.killSocket();
            final boolean connectionClosed = connectionClosedCondition.await(5, TimeUnit.SECONDS);
            assertTrue(connectionClosed);
            fixClient.listen();
        }
        finally
        {
            lock.unlock();
        }
    }

    @Test
    public void shouldBeSafeToCloseAFixClientWhichHasNotLoggedOn()
    {
        // Passes by merit of not throwing an exception

        final FixClient fixClient = FixClientFactory.createFixClient(new IntegrationSocketFactory(null, null));
        fixClient.close();
    }

    @Test
    public void shouldReportAnUnconnectedFixClientAsNotConnected()
    {
        final FixClient fixClient = FixClientFactory.createFixClient(new IntegrationSocketFactory(null, null));
        assertFalse(fixClient.isConnected());
    }
}
