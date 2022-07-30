package exchange.lob.node.client.response;

import exchange.lob.domain.ExchangeResponseCode;
import exchange.lob.product.Product;
import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.ArrayList;
import java.util.List;

public class GetProductsResponse implements Encodable
{
    private final ExchangeResponseCode code;
    private final List<Product> products;

    public GetProductsResponse(final ExchangeResponseCode code, final List<Product> products)
    {
        this.code = code;
        this.products = products;
    }

    public List<Product> products()
    {
        return products;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(code.value());
        bufferEncoder.encode(products);
    }

    @DecodedBy
    public static GetProductsResponse decode(final BufferDecoder bufferDecoder)
    {
        final ExchangeResponseCode code = ExchangeResponseCode.get(bufferDecoder.decodeShort());
        final List<Product> products = bufferDecoder.decodeList(Product::decode, ArrayList::new);
        return new GetProductsResponse(code, products);
    }
}
