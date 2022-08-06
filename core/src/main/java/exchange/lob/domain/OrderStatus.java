package exchange.lob.domain;

import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public enum OrderStatus implements Encodable
{
    NEW((short)1),

    FILLED((short)2),

    PARTIALLY_FILLED((short)3),

    REJECTED((short)4),

    CANCELLED((short)5),

    NULL_VAL((short)255);

    private static final Map<Short, OrderStatus> VALUES = Arrays.stream(values()).collect(toMap(OrderStatus::value, Function.identity()));

    private final short value;

    OrderStatus(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static OrderStatus get(final short value)
    {
        final OrderStatus orderStatus = VALUES.get(value);

        if (orderStatus == null)
        {
            throw new IllegalArgumentException("Unknown value: " + value);
        }

        return orderStatus;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(value);
    }

    @DecodedBy
    public static OrderStatus decode(final BufferDecoder bufferDecoder)
    {
        final short value = bufferDecoder.decodeShort();
        return OrderStatus.get(value);
    }
}
