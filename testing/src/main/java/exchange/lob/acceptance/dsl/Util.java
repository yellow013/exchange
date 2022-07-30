package exchange.lob.acceptance.dsl;

import java.util.UUID;

public class Util
{

    public static String randomizeAndTruncateUsername(final String value)
    {
        if (value.isEmpty())
        {
            return value;
        }
        return (value + "-" + UUID.randomUUID().toString().replace("-", "")).substring(0, 20);
    }
}
