package exchange.lob.fix.concurrent;


import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class NamedThreadFactory implements ThreadFactory
{
    private final String name;
    private final boolean daemon;
    private final AtomicInteger count = new AtomicInteger(0);
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public NamedThreadFactory(final String name, boolean daemon, Thread.UncaughtExceptionHandler uncaughtExceptionHandler)
    {
        this.name = name;
        this.daemon = daemon;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    @Override
    public Thread newThread(final Runnable runnable)
    {
        if (runnable != null)
        {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(daemon);
            thread.setName(name + "-" + count.getAndIncrement());
            if (uncaughtExceptionHandler != null)
            {
                thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }
            return thread;
        }
        return null;
    }
}