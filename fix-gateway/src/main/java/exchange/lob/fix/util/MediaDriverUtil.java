package exchange.lob.fix.util;

import io.aeron.archive.ArchivingMediaDriver;
import org.agrona.IoUtil;
import uk.co.real_logic.artio.CloseChecker;

import java.io.File;

public class MediaDriverUtil
{
    public static void cleanupMediaDriver(final ArchivingMediaDriver mediaDriver)
    {
        if (mediaDriver != null)
        {
            final String aeronDirectoryName = closeMediaDriver(mediaDriver);

            final File directory = new File(aeronDirectoryName);
            if (directory.exists())
            {
                CloseChecker.validate(aeronDirectoryName);
                IoUtil.delete(directory, false);
            }
        }
    }

    public static String closeMediaDriver(final ArchivingMediaDriver archivingMediaDriver)
    {
        final String aeronDirectoryName = archivingMediaDriver.mediaDriver().aeronDirectoryName();
        CloseChecker.onClose(aeronDirectoryName, archivingMediaDriver);
        archivingMediaDriver.close();
        return aeronDirectoryName;
    }
}
