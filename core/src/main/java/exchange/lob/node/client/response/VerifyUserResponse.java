package exchange.lob.node.client.response;

import exchange.lob.domain.ExchangeResponseCode;
import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

public class VerifyUserResponse implements Encodable
{
    private final ExchangeResponseCode code;
    private final boolean verified;
    private final long userId;

    public VerifyUserResponse(final ExchangeResponseCode code, final boolean verified, final long userId)
    {
        this.code = code;
        this.verified = verified;
        this.userId = userId;
    }

    public ExchangeResponseCode code()
    {
        return code;
    }

    public boolean verified()
    {
        return verified;
    }

    public long userId()
    {
        return userId;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(code.value());
        bufferEncoder.encode(verified);
        bufferEncoder.encode(userId);
    }

    @DecodedBy
    public static VerifyUserResponse decode(final BufferDecoder bufferDecoder)
    {
        final ExchangeResponseCode code = ExchangeResponseCode.get(bufferDecoder.decodeShort());
        final boolean verified = bufferDecoder.decodeBoolean();
        final long userId = bufferDecoder.decodeLong();
        return new VerifyUserResponse(code, verified, userId);
    }
}
