package exchange.lob.node.client.response;

import exchange.lob.domain.ExchangeResponseCode;
import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

import java.util.ArrayList;
import java.util.List;

public class GetBalancesResponse implements Encodable
{
    private final ExchangeResponseCode code;
    private final List<Balance> balances;

    public GetBalancesResponse(final ExchangeResponseCode code, final List<Balance> balances)
    {
        this.code = code;
        this.balances = balances;
    }

    public List<Balance> balances()
    {
        return balances;
    }

    public ExchangeResponseCode code()
    {
        return code;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(code.value());
        bufferEncoder.encode(balances);
    }

    @DecodedBy
    public static GetBalancesResponse decode(final BufferDecoder bufferDecoder)
    {
        final ExchangeResponseCode code = ExchangeResponseCode.get(bufferDecoder.decodeShort());
        final List<Balance> balances = bufferDecoder.decodeList(Balance::decode, ArrayList::new);
        return new GetBalancesResponse(code, balances);
    }
}
