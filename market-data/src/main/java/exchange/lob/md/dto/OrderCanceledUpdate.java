package exchange.lob.md.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderCanceledUpdate
{
    public final String type = "cancel";
    public final long timestamp;
    public final String symbol;
    public final String side;
    public final double price;
    public final double amount;

    @JsonCreator
    public OrderCanceledUpdate(
        final @JsonProperty("timestamp") long timestamp,
        final @JsonProperty("symbol") String symbol,
        final @JsonProperty("side") String side,
        final @JsonProperty("price") double price,
        final @JsonProperty("amount") double amount
    )
    {
        this.timestamp = timestamp;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.amount = amount;
    }
}
