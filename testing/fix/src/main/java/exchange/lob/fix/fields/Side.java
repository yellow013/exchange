package exchange.lob.fix.fields;

public enum Side
{
    Buy(1),
    Sell(2);

    private final int code;

    Side(final int code)
    {
        this.code = code;
    }

    public static Side fromFixValue(final String value)
    {
        return switch (value)
            {
                case "1" -> Buy;
                case "2" -> Sell;
                default -> throw new IllegalArgumentException(value);
            };
    }

    public int getCode()
    {
        return code;
    }


}
