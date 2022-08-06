package exchange.lob.match.execution;

import exchange.lob.domain.RejectionReason;
import exchange.lob.domain.Side;
import exchange.lob.product.Product;
import org.agrona.collections.Long2LongHashMap;
import org.agrona.collections.Long2ObjectHashMap;

public class CancellationExecutionReport implements ExecutionReport
{

    private final long executionId;
    private final long userId;
    private final long assetId;
    private final Side side;
    private final long price;
    private final long amount;
    private final Product product;

    public CancellationExecutionReport(
        final long executionId,
        final long userId,
        final long assetId,
        final Side side,
        final long price,
        final long amount,
        final Product product
    )
    {
        this.executionId = executionId;
        this.userId = userId;
        this.assetId = assetId;
        this.side = side;
        this.price = price;
        this.amount = amount;
        this.product = product;
    }

    @Override
    public Product getProduct()
    {
        return product;
    }

    @Override
    public long getExecutionId()
    {
        return executionId;
    }

    @Override
    public RejectionReason getRejectionReason()
    {
        return RejectionReason.NONE;
    }

    @Override
    public Side getSide()
    {
        return side;
    }

    @Override
    public long getPrice()
    {
        return price;
    }

    @Override
    public long getAmount()
    {
        return amount;
    }

    @Override
    public void settle(final Long2ObjectHashMap<Long2LongHashMap> balances)
    {
        ExecutionSettler.release(balances, userId, assetId, amount);
    }
}
