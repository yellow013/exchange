package exchange.lob.fix.incoming;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawFixMessageHandler implements MessageParserCallback
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RawFixMessageHandler.class);
    private final FixTagParser fixTagParser;

    public RawFixMessageHandler(final FixTagParser fixTagParser)
    {
        this.fixTagParser = fixTagParser;
    }

    @Override
    public void onMessage(final byte[] buffer, final int offset, final int length)
    {
        fixTagParser.parse(buffer, offset, length, true);
    }

    @Override
    public void onTruncatedMessage()
    {
        LOGGER.warn("Truncated Message received");
    }

    @Override
    public void onParseError(final String error)
    {
        LOGGER.error("Unable to parse data: " + error);
    }
}
