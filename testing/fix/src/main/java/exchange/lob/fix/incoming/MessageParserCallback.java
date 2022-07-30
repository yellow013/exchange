package exchange.lob.fix.incoming;

public interface MessageParserCallback
{
    void onMessage(final byte[] buffer, int offset, int length);
    void onTruncatedMessage();
    void onParseError(final String error);
}

