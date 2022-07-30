package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddAssetResponse
{
    public long assetId;

    @JsonCreator
    public AddAssetResponse(final @JsonProperty("assetId") long assetId)
    {
        this.assetId = assetId;
    }
}
