package exchange.lob.match.execution;

import exchange.lob.domain.RejectionReason;
import exchange.lob.domain.Side;
import exchange.lob.product.Product;
import org.agrona.collections.Long2LongHashMap;
import org.agrona.collections.Long2ObjectHashMap;

import java.util.Objects;

public class RejectionExecutionReport implements ExecutionReport
{

    private final long executionId;
    private final long userId;
    private final long releaseAssetId;
    private final long reservedBalance;
    private final RejectionReason rejectionReason;
    private final Product product;

    public RejectionExecutionReport(
        final long executionId,
        final long userId,
        final long releaseAssetId,
        final long reservedBalance,
        final RejectionReason rejectionReason,
        final Product product
    )
    {
        this.executionId = executionId;
        this.userId = userId;
        this.releaseAssetId = releaseAssetId;
        this.reservedBalance = reservedBalance;
        this.rejectionReason = rejectionReason;
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
        return rejectionReason;
    }

    @Override
    public Side getSide()
    {
        return Side.NULL_VAL;
    }

    @Override
    public long getPrice()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getAmount()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void settle(final Long2ObjectHashMap<Long2LongHashMap> balances)
    {
        final Long2LongHashMap userBalances = balances.get(userId);
        final long assetBalance = userBalances.getOrDefault(releaseAssetId, 0L);
        userBalances.put(releaseAssetId, assetBalance + reservedBalance);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final RejectionExecutionReport that = (RejectionExecutionReport)o;
        return executionId == that.executionId && userId == that.userId && releaseAssetId == that.releaseAssetId &&
            reservedBalance == that.reservedBalance && rejectionReason == that.rejectionReason && Objects.equals(product, that.product);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(executionId, userId, releaseAssetId, reservedBalance, rejectionReason, product);
    }

    @Override
    public String toString()
    {
        return "RejectionExecutionReport{" +
            "executionId=" + executionId +
            ", userId=" + userId +
            ", releaseAssetId=" + releaseAssetId +
            ", reservedBalance=" + reservedBalance +
            ", rejectionReason=" + rejectionReason +
            ", product=" + product +
            '}';
    }
}
