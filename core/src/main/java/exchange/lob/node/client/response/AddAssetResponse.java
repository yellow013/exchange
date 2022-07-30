package exchange.lob.node.client.response;

import exchange.lob.domain.ExchangeResponseCode;
import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

public class AddAssetResponse implements Encodable
{
    private long assetId;
    private ExchangeResponseCode code;

    public AddAssetResponse(final ExchangeResponseCode code, final long assetId)
    {
        this.code = code;
        this.assetId = assetId;
    }

    public long assetId()
    {
        return assetId;
    }

    public void assetId(final long assetId)
    {
        this.assetId = assetId;
    }

    public ExchangeResponseCode code()
    {
        return code;
    }

    public void code(final ExchangeResponseCode code)
    {
        this.code = code;
    }


    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(code.value());
        bufferEncoder.encode(assetId);
    }

    @DecodedBy
    public static AddAssetResponse decode(final BufferDecoder bufferDecoder)
    {
        final ExchangeResponseCode code = ExchangeResponseCode.get(bufferDecoder.decodeShort());
        final long assetId = bufferDecoder.decodeLong();
        return new AddAssetResponse(code, assetId);
    }
}
