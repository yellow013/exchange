package exchange.lob.node.client.response;

import exchange.lob.domain.ExchangeResponseCode;
import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

public class UpdateBalanceResponse implements Encodable
{
    private final ExchangeResponseCode code;

    public UpdateBalanceResponse(final ExchangeResponseCode code)
    {
        this.code = code;
    }

    public ExchangeResponseCode code()
    {
        return code;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(code.value());
    }

    @DecodedBy
    public static UpdateBalanceResponse decode(final BufferDecoder bufferDecoder)
    {
        final ExchangeResponseCode code = ExchangeResponseCode.get(bufferDecoder.decodeShort());
        return new UpdateBalanceResponse(code);
    }
}
