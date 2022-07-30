package exchange.lob.fix.transport;

public class TransportClosedException extends RuntimeException
{
    public TransportClosedException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
