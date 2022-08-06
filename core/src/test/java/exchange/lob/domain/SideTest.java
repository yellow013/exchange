package exchange.lob.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SideTest
{
    @Test
    public void sbeMapping()
    {
        assertEquals(Side.values().length, exchange.lob.api.sbe.Side.values().length);

        Arrays.stream(Side.values()).forEach(domainValue -> {
            final var sbeValue = exchange.lob.api.sbe.Side.valueOf(domainValue.name());
            assertEquals(domainValue.value(), sbeValue.value());
        });
    }
}