package exchange.lob.match.execution;

import exchange.lob.domain.OrderType;
import exchange.lob.domain.RejectionReason;
import exchange.lob.domain.Side;
import exchange.lob.match.Order;
import exchange.lob.product.Product;
import org.agrona.collections.Long2LongHashMap;
import org.agrona.collections.Long2ObjectHashMap;

import static exchange.lob.domain.OrderType.MKT;
import static exchange.lob.domain.Side.BID;
import static exchange.lob.user.UserService.EXCHANGE_USER_ID;

public class TradeExecutionReport implements ExecutionReport
{

    private final long executionId;
    private final OrderType takerOrderType;
    private final Order makerOrder;
    private final Side takerSide;
    private final long amount;
    private final long takerUserId;
    private final Product product;
    private final long reservedBalance;

    public TradeExecutionReport(
        long executionId,
        Order makerOrder,
        OrderType takerOrderType,
        Side takerSide,
        long amount,
        long takerUserId,
        Product product,
        long reservedBalance
    )
    {
        this.executionId = executionId;
        this.makerOrder = makerOrder;
        this.takerSide = takerSide;
        this.takerOrderType = takerOrderType;
        this.amount = amount;
        this.takerUserId = takerUserId;
        this.product = product;
        this.reservedBalance = reservedBalance;
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
        return takerSide;
    }

    @Override
    public long getPrice()
    {
        return makerOrder.getPrice();
    }

    @Override
    public long getAmount()
    {
        return amount;
    }

    @Override
    public void settle(final Long2ObjectHashMap<Long2LongHashMap> balances)
    {
        final long price = makerOrder.getPrice();
        final long makerUserId = makerOrder.getUserId();

        final long makerReceipts = product.make(takerSide, price, amount);
        final long takerReceipts = product.take(takerSide, price, amount);

        final long makerFees = product.calculateMakerFees(takerReceipts);
        final long takerFees = product.calculateTakerFees(takerReceipts);

        final long makerAssetId = product.getMakerAssetId(takerSide);
        final long takerAssetId = product.getTakerAssetId(takerSide);

        ExecutionSettler.release(balances, makerUserId, makerAssetId, makerReceipts);
        ExecutionSettler.release(balances, takerUserId, takerAssetId, takerReceipts - takerFees);
        ExecutionSettler.release(balances, makerUserId, takerAssetId, -makerFees);
        ExecutionSettler.release(balances, EXCHANGE_USER_ID, takerAssetId, takerFees + makerFees);

        if (takerSide == BID && takerOrderType == MKT)
        {
            // release the pessimistic MKT BID reservation
            ExecutionSettler.release(balances, takerUserId, makerAssetId, reservedBalance - makerReceipts);
        }
    }

    @Override
    public String toString()
    {
        return "TradeExecutionReport{" +
            "executionId=" + executionId +
            ", takerOrderType=" + takerOrderType +
            ", makerOrder=" + makerOrder +
            ", takerSide=" + takerSide +
            ", amount=" + amount +
            ", takerUserId=" + takerUserId +
            ", product=" + product +
            ", reservedBalance=" + reservedBalance +
            '}';
    }
}
