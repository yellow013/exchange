package exchange.lob.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetAssetsResponse
{
    public final List<Asset> assets;

    @JsonCreator
    public GetAssetsResponse(final @JsonProperty("assets") List<Asset> assets)
    {
        this.assets = assets;
    }

    public static class Asset
    {
        public long assetId;
        public String symbol;
        public byte scale;

        @JsonCreator
        public Asset(
            final @JsonProperty("assetId") long assetId,
            final @JsonProperty("symbol") String symbol,
            final @JsonProperty("scale") byte scale
        )
        {
            this.assetId = assetId;
            this.symbol = symbol;
            this.scale = scale;
        }

        public static Asset toResponse(final exchange.lob.product.Asset asset)
        {
            return new Asset(asset.getAssetId(), asset.getSymbol(), asset.getScale());
        }
    }
}
