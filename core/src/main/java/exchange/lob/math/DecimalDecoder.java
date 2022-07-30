package exchange.lob.math;

import org.agrona.DirectBuffer;
import org.agrona.sbe.CompositeDecoderFlyweight;

public class DecimalDecoder implements CompositeDecoderFlyweight
{
    public static final int SCHEMA_ID = 1;
    public static final int SCHEMA_VERSION = 0;
    public static final int ENCODED_LENGTH = 9;
    public static final java.nio.ByteOrder BYTE_ORDER = java.nio.ByteOrder.LITTLE_ENDIAN;

    private int offset;
    private DirectBuffer buffer;

    public DecimalDecoder wrap(final DirectBuffer buffer, final int offset)
    {
        if (buffer != this.buffer)
        {
            this.buffer = buffer;
        }
        this.offset = offset;

        return this;
    }

    public DirectBuffer buffer()
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

    public static int valueSinceVersion()
    {
        return 0;
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

    public long value()
    {
        return buffer.getLong(offset + 0, java.nio.ByteOrder.LITTLE_ENDIAN);
    }


    public static int scaleEncodingOffset()
    {
        return 8;
    }

    public static int scaleEncodingLength()
    {
        return 1;
    }

    public static int scaleSinceVersion()
    {
        return 0;
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

    public short scale()
    {
        return ((short)(buffer.getByte(offset + 8) & 0xFF));
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

        builder.append('(');
        builder.append("value=");
        builder.append(value());
        builder.append('|');
        builder.append("scale=");
        builder.append(scale());
        builder.append(')');

        return builder;
    }
}
