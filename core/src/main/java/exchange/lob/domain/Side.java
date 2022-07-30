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

    BID('1'),
    ASK('2'),
    NULL_VAL('3');

    private static final Map<Character, Side> VALUES = Arrays.stream(values()).collect(toMap(Side::value, Function.identity()));

    private final char value;

    Side(final char value)
    {
        this.value = value;
    }

    public char value()
    {
        return value;
    }

    public static Side get(final char value)
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
        final char value = bufferDecoder.decodeChar();
        return Side.get(value);
    }

    public exchange.lob.api.codecs.internal.Side toSbe()
    {
        return switch (this)
            {
                case BID -> exchange.lob.api.codecs.internal.Side.BID;
                case ASK -> exchange.lob.api.codecs.internal.Side.ASK;
                case NULL_VAL -> exchange.lob.api.codecs.internal.Side.NULL_VAL;
            };
    }

    public static Side fromSbe(final exchange.lob.api.codecs.internal.Side side)
    {
        return switch (side)
            {
                case BID -> BID;
                case ASK -> ASK;
                case NULL_VAL -> NULL_VAL;
            };
    };
}
