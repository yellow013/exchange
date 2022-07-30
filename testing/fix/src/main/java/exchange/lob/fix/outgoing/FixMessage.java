package exchange.lob.fix.outgoing;


public class FixMessage
{

    private static final String SOH = "\u0001";
    private static final String PIPE = "|";
    private final String messageWithChecksum;

    FixMessage(final String messageWithChecksum)
    {
        this.messageWithChecksum = messageWithChecksum;
    }

    public String toFixString()
    {
        return messageWithChecksum;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("FixMessage");
        sb.append("{messageWithChecksum='").append(messageWithChecksum).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String toHumanString()
    {
        return messageWithChecksum.replace(SOH, PIPE);
    }
}
