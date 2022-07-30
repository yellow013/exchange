package exchange.lob.node.client.response;

import exchange.lob.domain.ExchangeResponseCode;
import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

public class AddUserResponse implements Encodable
{
    private ExchangeResponseCode code;
    private long userId;

    public AddUserResponse(final ExchangeResponseCode code, final long userId)
    {
        this.code = code;
        this.userId = userId;
    }

    public void code(final ExchangeResponseCode code)
    {
        this.code = code;
    }

    public void userId(final long userId)
    {
        this.userId = userId;
    }

    public long userId()
    {
        return userId;
    }

    public ExchangeResponseCode code()
    {
        return code;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(code.value());
        bufferEncoder.encode(userId);
    }

    @DecodedBy
    public static AddUserResponse decode(final BufferDecoder bufferDecoder)
    {
        final ExchangeResponseCode code = ExchangeResponseCode.get(bufferDecoder.decodeShort());
        final long userId = bufferDecoder.decodeLong();
        return new AddUserResponse(code, userId);
    }
}
