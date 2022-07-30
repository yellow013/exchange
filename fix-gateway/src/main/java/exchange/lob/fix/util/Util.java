package exchange.lob.fix.util;

import org.agrona.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.artio.builder.Encoder;
import uk.co.real_logic.artio.engine.EngineConfiguration;
import uk.co.real_logic.artio.library.FixLibrary;
import uk.co.real_logic.artio.library.LibraryConfiguration;
import uk.co.real_logic.artio.session.Session;

import java.io.File;

public class Util
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    public static void sendReliably(final Session session, final Encoder encoder)
    {
        long sendResult;
        do
        {
            sendResult = session.trySend(encoder);
            LOGGER.info("Send result: {}", sendResult);
        }
        while (sendResult < 0);
    }

    public static FixLibrary blockingConnect(final LibraryConfiguration configuration)
    {
        final FixLibrary library = FixLibrary.connect(configuration);

        while (!library.isConnected())
        {
            library.poll(1);
            Thread.yield();
        }

        return library;
    }

    public static void cleanupOldLogFileDir(final EngineConfiguration engineConfiguration)
    {
        IoUtil.delete(new File(engineConfiguration.logFileDir()), true);
    }
}
