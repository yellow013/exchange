package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddAssetRequest
{
    public final String symbol;
    public final byte scale;

    @JsonCreator
    public AddAssetRequest(final @JsonProperty("symbol") String symbol, final @JsonProperty("scale") byte scale)
    {
        this.symbol = symbol;
        this.scale = scale;
    }
}
