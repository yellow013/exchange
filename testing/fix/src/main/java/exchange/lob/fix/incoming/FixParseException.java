package exchange.lob.fix.incoming;

@SuppressWarnings("serial")
public final class FixParseException
    extends RuntimeException
{
    public FixParseException(final String msg)
    {
        super(msg);
    }

    public FixParseException(final String msg, final Throwable rootCause)
    {
        super(msg, rootCause);
    }

    /**
     * Don't waste CPU time when under pressure to receive packets.  The CPU is likely ot be the cause of packet loss.
     * @return this instance
     */
    @Override
    public Throwable fillInStackTrace()
    {
        return this;
    }
}