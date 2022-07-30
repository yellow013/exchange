package exchange.lob.fix.fields;

public enum MsgType
{
    BUSINESS_MESSAGE_REJECT("j"),
    EXECUTION_REPORT("8"),
    LOGIN("A"),
    LOGOUT("5"),
    MARKET_DATA_SNAPSHOT("W"),
    NEW_ORDER_SINGLE("D"),
    ORDER_CANCEL_REQUEST("F"),
    REJECT("3"),
    RESEND_REQUEST("2"),
    TEST_REQUEST("1");

    private final String code;

    MsgType(final String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }

    public static boolean knownMsgType(final String possibleMessageType)
    {
       for (MsgType msgType : MsgType.values())
       {
           if (msgType.getCode().equals(possibleMessageType))
           {
               return true;
           }
       }
       return false;
    }

}
