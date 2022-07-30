package exchange.lob.math;

import org.agrona.MutableDirectBuffer;
import org.agrona.sbe.CompositeEncoderFlyweight;

@SuppressWarnings("all")
public class DecimalEncoder implements CompositeEncoderFlyweight
{
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final int ENCODED_LENGTH = 9;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private int offset;
    private MutableDirectBuffer buffer;

    public DecimalEncoder wrap(final MutableDirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;

        return this;
    }

    public MutableDirectBuffer buffer()
    {
        return buffer;
    }

    public int offset()
    {
        return offset;
    }

    public int encodedLength()
    {
        return ENCODED_LENGTH;
    }

    public int sbeSchemaId()
    {
        return SCHEMA_ID;
    }

    public int sbeSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    public static int valueEncodingOffset()
    {
        return 0;
    }

    public static int valueEncodingLength()
    {
        return 8;
    }

    public static long valueNullValue()
    {
        return -9223372036854775808L;
    }

    public static long valueMinValue()
    {
        return -9223372036854775807L;
    }

    public static long valueMaxValue()
    {
        return 9223372036854775807L;
    }

    public DecimalEncoder value(final long value)
    {
        buffer.putLong(offset + 0, value, java.nio.ByteOrder.LITTLE_ENDIAN);
        return this;
    }


    public static int scaleEncodingOffset()
    {
        return 8;
    }

    public static int scaleEncodingLength()
    {
        return 1;
    }

    public static short scaleNullValue()
    {
        return (short)255;
    }

    public static short scaleMinValue()
    {
        return (short)0;
    }

    public static short scaleMaxValue()
    {
        return (short)254;
    }

    public DecimalEncoder scale(final short value)
    {
        buffer.putByte(offset + 8, (byte)value);
        return this;
    }


    public String toString()
    {
        if (null == buffer)
        {
            return "";
        }

        return appendTo(new StringBuilder()).toString();
    }

    public StringBuilder appendTo(final StringBuilder builder)
    {
        if (null == buffer)
        {
            return builder;
        }

        final DecimalDecoder decoder = new DecimalDecoder();
        decoder.wrap(buffer, offset);

        return decoder.appendTo(builder);
    }
}