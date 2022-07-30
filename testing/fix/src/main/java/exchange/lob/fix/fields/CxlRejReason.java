package exchange.lob.fix.fields;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public enum CxlRejReason
{
    UNKNOWN_ORDER("1");

    private final String value;

    private static final Map<String, CxlRejReason> values = Arrays.stream(values()).collect(toMap(v -> v.value, Function.identity()));

    CxlRejReason(final String value)
    {
        this.value = value;
    }

    public static CxlRejReason fromFixValue(final String value)
    {
        return values.get(value);
    }
}
