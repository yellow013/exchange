package exchange.lob.fix.fields;

public enum BusinessRejectionReason
{
    Other(0),
    UnknownId(1),
    UnknownSecurity(2),
    UnsupportedMessageType(3),
    ApplicationNotAvailable(4),
    ConditionallyRequiredFieldMissing(5);

    private final int code;

    BusinessRejectionReason(final int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
