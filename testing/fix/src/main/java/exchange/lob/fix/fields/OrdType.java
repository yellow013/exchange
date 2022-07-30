package exchange.lob.fix.fields;

public enum OrdType
{
    Market(1),
    Limit(2);

    private final int code;

    private OrdType(final int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
