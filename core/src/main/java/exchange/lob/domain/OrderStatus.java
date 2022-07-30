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
    NEW('1'),

    FILLED('2'),

    PARTIALLY_FILLED('3'),

    REJECTED('4'),

    CANCELLED('5'),

    NULL_VAL('6');

    private static final Map<Character, OrderStatus> VALUES = Arrays.stream(values()).collect(toMap(OrderStatus::value, Function.identity()));

    private final char value;

    OrderStatus(final char value)
    {
        this.value = value;
    }

    public static OrderStatus fromSbe(final exchange.lob.api.codecs.internal.OrderStatus makerOrderStatus)
    {
        return switch (makerOrderStatus)
            {
                case NEW -> NEW;
                case FILLED -> FILLED;
                case REJECTED -> REJECTED;
                case CANCELLED -> CANCELLED;
                case PARTIALLY_FILLED -> PARTIALLY_FILLED;
                case NULL_VAL -> NULL_VAL;
            };
    }

    public char value()
    {
        return value;
    }

    public static OrderStatus get(final char value)
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
        final char value = bufferDecoder.decodeChar();
        return OrderStatus.get(value);
    }
}
