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

    LMT((short)1),
    MKT((short)2),
    NULL_VAL((short)255);

    private static final Map<Short, OrderType> VALUES = Arrays.stream(values()).collect(toMap(OrderType::value, Function.identity()));

    private final short value;

    OrderType(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static OrderType get(final short value)
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
        final short value = bufferDecoder.decodeShort();
        return OrderType.get(value);
    }
}
