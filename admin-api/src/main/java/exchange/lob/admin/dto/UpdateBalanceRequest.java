package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateBalanceRequest
{
    public final double amount;

    @JsonCreator
    public UpdateBalanceRequest(final @JsonProperty("amount") double amount)
    {
        this.amount = amount;
    }
}
