package exchange.lob.domain;

import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public enum OrderType implements Encodable
{

    LMT('1'),
    MKT('2'),
    NULL_VAL('3');

    private static final Map<Character, OrderType> VALUES = Arrays.stream(values()).collect(toMap(OrderType::value, Function.identity()));

    private final char value;

    OrderType(final char value)
    {
        this.value = value;
    }

    public char value()
    {
        return value;
    }

    public static OrderType get(final char value)
    {
        final OrderType orderType = VALUES.get(value);

        if (orderType == null)
        {
            throw new IllegalArgumentException("Unknown value: " + value);
        }

        return orderType;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(value);
    }

    @DecodedBy
    public static OrderType decode(final BufferDecoder bufferDecoder)
    {
        final char value = bufferDecoder.decodeChar();
        return OrderType.get(value);
    }

    public exchange.lob.api.codecs.internal.OrderType toSbe()
    {
        return switch (this)
            {
                case LMT -> exchange.lob.api.codecs.internal.OrderType.LMT;
                case MKT -> exchange.lob.api.codecs.internal.OrderType.MKT;
                case NULL_VAL -> exchange.lob.api.codecs.internal.OrderType.NULL_VAL;
            };
    }
}
