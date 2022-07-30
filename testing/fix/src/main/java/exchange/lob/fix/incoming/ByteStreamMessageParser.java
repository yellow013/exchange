package exchange.lob.fix.incoming;

import java.nio.ByteBuffer;

public interface ByteStreamMessageParser
{
    void parse(final ByteBuffer segment);

    void initialise(MessageParserCallback messageParserCallback);
}
