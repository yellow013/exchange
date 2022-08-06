package exchange.lob.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTypeTest
{
    @Test
    public void sbeMapping()
    {
        assertEquals(OrderType.values().length, exchange.lob.api.sbe.OrderType.values().length);

        Arrays.stream(OrderType.values()).forEach(domainValue -> {
            final var sbeValue = exchange.lob.api.sbe.OrderType.valueOf(domainValue.name());
            assertEquals(domainValue.value(), sbeValue.value());
        });
    }
}