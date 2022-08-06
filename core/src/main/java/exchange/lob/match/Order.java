package exchange.lob.match;

import exchange.lob.domain.OrderStatus;
import exchange.lob.domain.OrderType;
import exchange.lob.domain.Side;

import java.util.Objects;

public class Order
{
    private long orderId;
    private final String clientOrderId;
    private final long userId;
    private final long productId;
    private OrderStatus orderStatus;
    private final OrderType orderType;
    private final Side side;
    // counter asset
    private final long price;
    // base asset
    private long amount;

    Order(
        final long orderId,
        final String clientOrderId,
        final long userId,
        final long productId,
        final OrderStatus orderStatus,
        final OrderType orderType,
        final Side side,
        final long price,
        final long amount
    )
    {
        this.orderId = orderId;
        this.clientOrderId = clientOrderId;
        this.userId = userId;
        this.productId = productId;
        this.orderStatus = orderStatus;
        this.orderType = orderType;
        this.side = side;
        this.price = price;
        this.amount = amount;
    }

    void depleteBy(long amount)
    {
        this.amount -= amount;
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
        final Order order = (Order)o;
        return orderId == order.orderId && userId == order.userId && productId == order.productId && price == order.price && amount == order.amount &&
            Objects.equals(clientOrderId, order.clientOrderId) && orderStatus == order.orderStatus && orderType == order.orderType && side == order.side;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(orderId, clientOrderId, userId, productId, orderStatus, orderType, side, price, amount);
    }

    public long getOrderId()
    {
        return this.orderId;
    }

    public String getClientOrderId()
    {
        return this.clientOrderId;
    }

    public long getUserId()
    {
        return this.userId;
    }

    public long getProductId()
    {
        return this.productId;
    }

    public OrderStatus getOrderStatus()
    {
        return this.orderStatus;
    }

    public OrderType getOrderType()
    {
        return this.orderType;
    }

    public Side getSide()
    {
        return this.side;
    }

    public long getPrice()
    {
        return this.price;
    }

    public long getAmount()
    {
        return this.amount;
    }

    public void setOrderId(final long orderId)
    {
        this.orderId = orderId;
    }

    public void setStatus(final OrderStatus orderStatus)
    {
        this.orderStatus = orderStatus;
    }

    @Override
    public String toString()
    {
        return "Order{" +
            "orderId=" + orderId +
            ", clientOrderId='" + clientOrderId + '\'' +
            ", userId=" + userId +
            ", productId=" + productId +
            ", orderStatus=" + orderStatus +
            ", orderType=" + orderType +
            ", side=" + side +
            ", price=" + price +
            ", amount=" + amount +
            '}';
    }
}
