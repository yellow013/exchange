package exchange.lob.admin.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddProductRequest
{
    public final long baseAssetId;
    public final long counterAssetId;
    public final long makerFee;
    public final long takerFee;

    @JsonCreator
    public AddProductRequest(
        final @JsonProperty("baseAssetId") long baseAssetId,
        final @JsonProperty("counterAssetId") long counterAssetId,
        final @JsonProperty("makerFee") long makerFee,
        final @JsonProperty("takerFee") long takerFee
    )
    {
        this.baseAssetId = baseAssetId;
        this.counterAssetId = counterAssetId;
        this.makerFee = makerFee;
        this.takerFee = takerFee;
    }
}
