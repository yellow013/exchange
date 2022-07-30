package exchange.lob.fix.concurrent;

import java.util.concurrent.Semaphore;

public class ThreadBlocker implements Blocker
{
    private final Semaphore semaphore = new Semaphore(0);
    private volatile boolean paused;

    @Override
    public void mayWait()
    {
        while (paused)
        {
            semaphore.acquireUninterruptibly();
        }
    }

    public void pause()
    {
        paused = true;

    }

    public void resume()
    {
        paused = false;
        semaphore.release();
    }

}
