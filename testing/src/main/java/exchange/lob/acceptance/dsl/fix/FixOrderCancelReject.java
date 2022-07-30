package exchange.lob.acceptance.dsl.fix;


import exchange.lob.fix.fields.CxlRejReason;

public class FixOrderCancelReject
{
    private final String clientOrderId;
    private final CxlRejReason cxlRejReason;
    private boolean matched;

    public FixOrderCancelReject(final String clientOrderId, final CxlRejReason cxlRejReason)
    {
        this.clientOrderId = clientOrderId;
        this.cxlRejReason = cxlRejReason;
    }

    public String clientOrderId()
    {
        return clientOrderId;
    }

    public boolean matched()
    {
        return matched;
    }

    public void match()
    {
        matched = true;
    }

    @Override
    public String toString()
    {
        return "FixOrderCancelReject{" +
            "clientOrderId='" + clientOrderId + '\'' +
            ", cxlRejReason=" + cxlRejReason +
            ", matched=" + matched +
            '}';
    }
}
