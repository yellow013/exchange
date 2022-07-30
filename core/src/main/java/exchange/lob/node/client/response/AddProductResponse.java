package exchange.lob.node.client.response;

import exchange.lob.domain.ExchangeResponseCode;
import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.Encodable;

public class AddProductResponse implements Encodable
{
    private final ExchangeResponseCode code;
    private final long productId;

    public AddProductResponse(final ExchangeResponseCode code, final long productId)
    {
        this.code = code;
        this.productId = productId;
    }

    public ExchangeResponseCode code()
    {
        return code;
    }

    public long productId()
    {
        return productId;
    }


    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(code.value());
        bufferEncoder.encode(productId);
    }

    public static AddProductResponse decode(final BufferDecoder bufferDecoder)
    {
        final ExchangeResponseCode code = ExchangeResponseCode.get(bufferDecoder.decodeShort());
        final long productId = bufferDecoder.decodeLong();
        return new AddProductResponse(code, productId);
    }
}
