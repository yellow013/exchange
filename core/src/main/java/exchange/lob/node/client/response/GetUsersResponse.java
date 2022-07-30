package exchange.lob.node.client.response;

import exchange.lob.domain.ExchangeResponseCode;
import exchange.lob.user.User;
import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.ArrayList;
import java.util.List;

public class GetUsersResponse implements Encodable
{
    private final ExchangeResponseCode code;
    private final List<User> users;

    public GetUsersResponse(final List<User> users, final ExchangeResponseCode code)
    {
        this.users = users;
        this.code = code;
    }

    public ExchangeResponseCode code()
    {
        return code;
    }

    public List<User> users()
    {
        return users;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(code.value());
        bufferEncoder.encode(users);
    }

    @DecodedBy
    public static GetUsersResponse decode(final BufferDecoder bufferDecoder)
    {
        final ExchangeResponseCode code = ExchangeResponseCode.get(bufferDecoder.decodeShort());
        final List<User> users = bufferDecoder.decodeList(User::decode, ArrayList::new);
        return new GetUsersResponse(users, code);
    }
}
