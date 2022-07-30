package exchange.lob.node.util;

import org.agrona.IoUtil;

import java.io.File;

public class FileUtil
{
    public static String tmpDirForName(String name)
    {
        return IoUtil.tmpDirName() + File.separator + name;
    }

    public static String shmDirForName(String name)
    {
        return "/dev/shm" + File.separator + name;
    }
}
