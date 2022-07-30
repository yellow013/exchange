package exchange.lob.match;

import java.util.Map;

import static org.assertj.core.api.Assertions.entry;

public class MatchingEngineTestUtils
{
    public static Order orderWithId(Order order, final long orderId)
    {
        return new Order(
            orderId,
            order.getClientOrderId(),
            order.getUserId(),
            order.getProductId(),
            order.getOrderStatus(),
            order.getOrderType(),
            order.getSide(),
            order.getPrice(),
            order.getAmount()
        );
    }

    public static Map.Entry<Long, Order> orderEntry(final Order order)
    {
        return entry(order.getOrderId(), order);
    }
}
