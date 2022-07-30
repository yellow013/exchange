package exchange.lob.node.client.response;

import exchange.lob.domain.ExchangeResponseCode;
import exchange.lob.product.Asset;
import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.ArrayList;
import java.util.List;

public class GetAssetsResponse implements Encodable
{
    private final ExchangeResponseCode code;
    private final List<Asset> assets;

    public GetAssetsResponse(final ExchangeResponseCode code, final List<Asset> assets)
    {
        this.code = code;
        this.assets = assets;
    }

    public ExchangeResponseCode code()
    {
        return code;
    }

    public List<Asset> assets()
    {
        return assets;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(code.value());
        bufferEncoder.encode(assets);
    }

    @DecodedBy
    public static GetAssetsResponse decode(final BufferDecoder bufferDecoder)
    {
        final ExchangeResponseCode code = ExchangeResponseCode.get(bufferDecoder.decodeShort());
        final List<Asset> assets = bufferDecoder.decodeList(Asset::decode, ArrayList::new);
        return new GetAssetsResponse(code, assets);
    }
}
