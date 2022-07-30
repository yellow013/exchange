package exchange.lob.fix.concurrent;


import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ThreadBlockerTest
{

    private volatile boolean running = true;

    @Test
    public void threadShouldSwitchBetweenWaitingAndRunningStatesWhenRequested()
    {
        final ThreadBlocker threadBlocker = new ThreadBlocker();
        final Thread thread = new Thread(() -> {
            while (running)
            {
                threadBlocker.mayWait();
            }
        });
        assertEquals(thread.getState(), Thread.State.NEW);
        thread.start();
        waitForThreadState(thread, Thread.State.RUNNABLE, 100);
        threadBlocker.pause();
        waitForThreadState(thread, Thread.State.WAITING, 100);
        threadBlocker.resume();
        waitForThreadState(thread, Thread.State.RUNNABLE, 100);
        threadBlocker.pause();
        waitForThreadState(thread, Thread.State.WAITING, 100);
        threadBlocker.resume();
        waitForThreadState(thread, Thread.State.RUNNABLE, 100);
        running = false;
        waitForThreadState(thread, Thread.State.TERMINATED, 100);

    }

    private void waitForThreadState(final Thread thread, final Thread.State expectedState, final int timeoutMillis)
    {
        final int numberOfRetries = 10;
        final int waitBetweenRetries = timeoutMillis / 10;
        for (int i = 0; i < numberOfRetries; i++)
        {
            if (thread.getState() == expectedState)
            {
                return;
            }
            else
            {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(waitBetweenRetries));
            }
        }
        fail(String.format("Thread : %s did not match expected state %s actual state %s",
                                  thread.getName(), expectedState.name(), thread.getState().name()));
    }
}
