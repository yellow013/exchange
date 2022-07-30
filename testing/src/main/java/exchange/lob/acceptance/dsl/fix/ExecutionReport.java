package exchange.lob.acceptance.dsl.fix;

import exchange.lob.fix.fields.ExecType;
import exchange.lob.fix.fields.OrdRejReason;
import exchange.lob.fix.fields.OrdStatus;
import exchange.lob.fix.fields.Side;

import java.util.Optional;

public final class ExecutionReport
{
    private final String product;
    private final String clientOrderId;
    private final OrdStatus ordStatus;
    private final ExecType execType;
    private final Side side;
    private final Optional<String> maybePrice;
    private final Optional<String> maybeOrderQty;
    private final Optional<OrdRejReason> maybeRejectionReason;
    private final Optional<String> text;
    private boolean matched = false;

    public ExecutionReport(
        final String product,
        final String clientOrderId,
        final OrdStatus ordStatus,
        final ExecType execType,
        final Side side,
        final Optional<String> maybePrice,
        final Optional<String> maybeOrderQty,
        final Optional<OrdRejReason> maybeRejectionReason,
        final Optional<String> text
    )
    {
        this.product = product;
        this.clientOrderId = clientOrderId;
        this.ordStatus = ordStatus;
        this.execType = execType;
        this.side = side;
        this.maybePrice = maybePrice;
        this.maybeOrderQty = maybeOrderQty;
        this.maybeRejectionReason = maybeRejectionReason;
        this.text = text;
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
        return "ExecutionReport{" +
            "product='" + product + '\'' +
            ", clientOrderId='" + clientOrderId + '\'' +
            ", ordStatus=" + ordStatus +
            ", execType=" + execType +
            ", side=" + side +
            ", maybePrice=" + maybePrice +
            ", maybeOrderQty=" + maybeOrderQty +
            ", maybeRejectionReason=" + maybeRejectionReason +
            ", text=" + text +
            ", matched=" + matched +
            '}';
    }
}
