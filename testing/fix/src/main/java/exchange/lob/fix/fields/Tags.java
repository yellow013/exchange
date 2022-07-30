package exchange.lob.fix.fields;

public enum Tags
{
    Account(1),
    BeginSeqNo(7),
    BusinessRejectReason(380),
    ClOrdID(11),
    EncryptMethod(98),
    EndSeqNo(16),
    HeartBtInt(108),
    MsgSeqNum(34),
    MsgType(35),
    OrderQty(38),
    OrdType(40),
    OrigSendingTime(122),
    Password(554),
    PossDupFlag(43),
    Price(44),
    RefMsgType(372),
    RefSeqNum(45),
    ResetSeqNumFlag(141),
    SenderCompID(49),
    SecurityID(48),
    SecurityIDSource(22),
    SendingTime(52),
    SessionRejectReason(373),
    Side(54),
    Symbol(55),
    TargetCompID(56),
    TestReqID(112),
    TransactTime(60),
    RawDataLength(95),
    RawData(96),
    Username(553);

    private final int tag;

    Tags(final int tag)
    {
        this.tag = tag;
    }

    public int getTag()
    {
        return tag;
    }

    public static boolean knownTag(final int possibleTag)
    {
       for (Tags tag : Tags.values())
       {
           if (tag.getTag() == possibleTag)
           {
               return true;
           }
       }
       return false;
    }
}
