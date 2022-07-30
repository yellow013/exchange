package exchange.lob.fix.integration;

import exchange.lob.fix.FixClient;
import exchange.lob.fix.FixClientFactory;
import exchange.lob.fix.integration.fixture.IntegrationSocketFactory;
import exchange.lob.fix.integration.fixture.SignallingConnectionObserver;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConnectionNotificationsIntegrationTest
{
    private ReadableByteChannel readableByteChannel;
    private WritableByteChannel writableByteChannel;
    private ByteArrayOutputStream byteArrayOutputStream;

    @Test
    public void shouldNotifyOnConnectionEstablished() throws Exception
    {
        FixClient fixClient;
        final Lock lock = new ReentrantLock();

        final Condition connectionEstablishedCondition = lock.newCondition();
        final Condition connectionClosedCondition = lock.newCondition();

        byteArrayOutputStream = new ByteArrayOutputStream();
        writableByteChannel = Channels.newChannel(byteArrayOutputStream);
        readableByteChannel = Channels.newChannel(new ByteArrayInputStream(new byte[0]));
        fixClient = FixClientFactory.createFixClient(new IntegrationSocketFactory(readableByteChannel, writableByteChannel));
        fixClient.registerTransportObserver(new SignallingConnectionObserver(lock, connectionEstablishedCondition, connectionClosedCondition));

        try
        {
            //when
            lock.lock();
            fixClient.connect();
            final boolean connectionEstablished = connectionEstablishedCondition.await(5, TimeUnit.SECONDS);
            assertTrue(connectionEstablished);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Test
    public void shouldNotifyOnConnectionClosed() throws Exception
    {
        FixClient fixClient;
        final Lock lock = new ReentrantLock();

        final Condition connectionEstablishedCondition = lock.newCondition();
        final Condition connectionClosedCondition = lock.newCondition();

        byteArrayOutputStream = new ByteArrayOutputStream();
        writableByteChannel = Channels.newChannel(byteArrayOutputStream);
        readableByteChannel = Channels.newChannel(new ByteArrayInputStream(new byte[0]));
        fixClient = FixClientFactory.createFixClient(new IntegrationSocketFactory(readableByteChannel, writableByteChannel));
        fixClient.registerTransportObserver(new SignallingConnectionObserver(lock, connectionEstablishedCondition, connectionClosedCondition));

        try
        {
            //when
            lock.lock();
            fixClient.connect();
            final boolean connectionClosed = connectionClosedCondition.await(5, TimeUnit.SECONDS);
            assertTrue(connectionClosed);
        }
        finally
        {
            lock.unlock();
        }
    }

}
