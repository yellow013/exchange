package exchange.lob.fix.incoming;

import exchange.lob.fix.FixUtil;
import exchange.lob.fix.byteoperations.ByteUtil;

public final class FixTagParser
{
    public static final byte SOH = 1;
    private static final byte ASCII_EQUALS = (byte)61;

    private final FixTagHandler fixTagHandler;
    private final byte[] lineSeparators;

    public FixTagParser(final FixTagHandler fixTagHandler)
    {
        this.fixTagHandler = fixTagHandler;
        lineSeparators = new byte[1];
        lineSeparators[0] = SOH;
    }

    public FixTagParser(final FixTagHandler fixTagHandler, final byte[] lineSeparators)
    {
        this.fixTagHandler = fixTagHandler;
        this.lineSeparators = lineSeparators;
    }

    public boolean parse(final byte[] message, final int offset, final int length, final boolean throwExceptionOnParseFailure)
    {
        fixTagHandler.messageStart();

        int tagStart = offset;
        int equalsIndex = -1;

        for (int i = 0; i < length; i++)
        {
            int index = i + offset;
            if (ASCII_EQUALS == message[index])
            {
                equalsIndex = index;
            }

            if (containsLineSeparator(message[index]))
            {
                if (-1 != equalsIndex)
                {
                    if (!ByteUtil.isInteger(message, tagStart, equalsIndex - tagStart))
                    {
                        if (throwExceptionOnParseFailure)
                        {
                            raiseException("Invalid tag id", message, offset, length, tagStart, equalsIndex);
                        }
                        return false;
                    }

                    int tagIdentity = ByteUtil.readIntFromAscii(message, tagStart, equalsIndex - tagStart);

                    final int tagValueOffset = equalsIndex + 1;
                    final int tagValueLength = index - tagValueOffset;

                    fixTagHandler.onTag(tagIdentity, message, tagValueOffset, tagValueLength);

                    if (fixTagHandler.isFinished())
                    {
                        break;
                    }
                }

                tagStart = index + 1;
                equalsIndex = -1;
            }
        }

        fixTagHandler.messageEnd();

        return true;
    }

    private boolean containsLineSeparator(final byte separator)
    {
        for (final byte lineSeparator : lineSeparators)
        {
            if (lineSeparator == separator)
            {
                return true;
            }
        }
        return false;
    }

    private static void raiseException(final String reason,
                                       final byte[] message,
                                       final int offset,
                                       final int length,
                                       final int tagStart,
                                       final int equalsIndex)
    {
        String msg = String.format("Parse Failed: %s at tagStart=%d equalsIndex=%d message=%s",
                                   reason,
                                   Integer.valueOf(tagStart),
                                   Integer.valueOf(equalsIndex),
                                   new String(message, offset, length, FixUtil.getCharset()).replace('\u0001', '|'));

        throw new FixParseException(msg);
    }
}

