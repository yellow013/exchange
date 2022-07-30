package exchange.lob.fix.fields;

public enum SessionRejectReason
{
    InvalidTagNumber(0),
    RequiredTagMissing(1),
    TagNotDefinedForThisMessageType(2),
    UndefinedTag(3),
    TagSpecifiedWithoutAValue(4),
    ValueIsIncorrectOutOfRangeForThisTag(5),
    IncorrectDataFormatForValue(6),
    DecryptionProblem(7),
    SignatureProblem(8),
    CompIdProblem(9),
    SendingTimeAccuracyProblem(10),
    InvalidMsgType(11);

    private final int code;

    private SessionRejectReason(final int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
