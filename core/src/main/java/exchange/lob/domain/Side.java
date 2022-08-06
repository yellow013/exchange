package exchange.lob.domain;

import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public enum Side implements Encodable
{

    BID((short)1),
    ASK((short)2),
    NULL_VAL((short)255);

    private static final Map<Short, Side> VALUES = Arrays.stream(values()).collect(toMap(Side::value, Function.identity()));

    private final short value;

    Side(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static Side get(final short value)
    {
        final Side side = VALUES.get(value);

        if (side == null)
        {
            throw new IllegalArgumentException("Unknown value: " + value);
        }

        return side;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(value);
    }

    @DecodedBy
    public static Side decode(final BufferDecoder bufferDecoder)
    {
        final short value = bufferDecoder.decodeShort();
        return Side.get(value);
    }
}
