package exchange.lob.fix.fields;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public enum OrdStatus
{
    New("0"),
    PartiallyFilled("1"),
    Filled("2"),
    Cancelled("4"),
    Rejected("8");

    private final String value;

    private static final Map<String, OrdStatus> values = Arrays.stream(values()).collect(toMap(v -> v.value, Function.identity()));

    OrdStatus(final String value)
    {
        this.value = value;
    }

    public static OrdStatus fromFixValue(final String value)
    {
        return values.get(value);
    }
}
