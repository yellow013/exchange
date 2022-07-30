package exchange.lob.domain;

import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public enum RejectionReason implements Encodable
{
    NONE('1'),

    INSUFFICIENT_LIQUIDITY('2'),

    INSUFFICIENT_BALANCE('3'),

    INVALID_USER('4'),

    INVALID_PRODUCT('5'),

    UNKNOWN_ORDER('6'),

    DUPLICATE_ORDER('7'),

    NULL_VAL('8');

    private static final Map<Character, RejectionReason> VALUES = Arrays.stream(values()).collect(toMap(RejectionReason::value, Function.identity()));

    private final char value;

    RejectionReason(final char value)
    {
        this.value = value;
    }

    public char value()
    {
        return value;
    }

    public static RejectionReason get(final char value)
    {
        final RejectionReason rejectionReason = VALUES.get(value);

        if (rejectionReason == null)
        {
            throw new IllegalArgumentException("Unknown value: " + value);
        }

        return rejectionReason;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(value);
    }

    @DecodedBy
    public static RejectionReason decode(final BufferDecoder bufferDecoder)
    {
        final char value = bufferDecoder.decodeChar();
        return RejectionReason.get(value);
    }
}
