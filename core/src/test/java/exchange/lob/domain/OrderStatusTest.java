package exchange.lob.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderStatusTest
{
    @Test
    public void sbeMapping()
    {
        assertEquals(OrderStatus.values().length, exchange.lob.api.sbe.OrderStatus.values().length);

        Arrays.stream(OrderStatus.values()).forEach(domainValue -> {
            final var sbeValue = exchange.lob.api.sbe.OrderStatus.valueOf(domainValue.name());
            assertEquals(domainValue.value(), sbeValue.value());
        });
    }
}