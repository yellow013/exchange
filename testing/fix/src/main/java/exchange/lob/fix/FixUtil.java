package exchange.lob.fix;

import exchange.lob.fix.incoming.FixTagParser;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class FixUtil
{
    public static final Charset ASCII_CHARSET;

    static
    {
            ASCII_CHARSET = StandardCharsets.US_ASCII;
    }

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("YYYYMMdd-HH:mm:ss.SSS").withZone(ZoneOffset.UTC);

    public static Charset getCharset()
    {
        return ASCII_CHARSET;
    }

    public static byte[] newMessage(String...tags)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = tags.length; i < n; i++)
        {
            sb.append(tags[i]);
            sb.append(FixTagParser.SOH);
        }
        return sb.toString().getBytes(FixUtil.getCharset());
    }
}

