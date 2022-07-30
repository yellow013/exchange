package exchange.lob.fix.fields;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public enum ExecType
{
    New("0"),
    Trade("F"),
    Cancelled("4"),
    Rejected("8");

    private final String value;

    private static final Map<String, ExecType> values = Arrays.stream(values()).collect(toMap(v -> v.value, Function.identity()));

    ExecType(final String value)
    {
        this.value = value;
    }

    public static ExecType fromFixValue(final String value)
    {
        return values.get(value);
    }
}
