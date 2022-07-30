package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class GetUserBalancesResponse
{
    public final Map<String, Double> balances;

    @JsonCreator
    public GetUserBalancesResponse(final @JsonProperty("balances") Map<String, Double> balances)
    {
        this.balances = balances;
    }
}
