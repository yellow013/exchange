package exchange.lob.fix.fields;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public enum OrdRejReason
{
    BROKER_OPTION("0"),
    UNKNOWN_SYMBOL("1"),
    DUPLICATE_ORDER("6");

    private final String value;

    private static final Map<String, OrdRejReason> values = Arrays.stream(values()).collect(toMap(v -> v.value, Function.identity()));

    OrdRejReason(final String value)
    {
        this.value = value;
    }

    public static OrdRejReason fromFixValue(final String value)
    {
        return values.get(value);
    }
}
