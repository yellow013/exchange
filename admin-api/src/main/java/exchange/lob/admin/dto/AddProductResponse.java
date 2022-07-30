package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddProductResponse
{
    public final long productId;

    @JsonCreator
    public AddProductResponse(final @JsonProperty("productId") long productId)
    {
        this.productId = productId;
    }
}
