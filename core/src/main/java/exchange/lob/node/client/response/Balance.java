package exchange.lob.node.client.response;

import io.aeronic.codec.BufferDecoder;
import io.aeronic.codec.BufferEncoder;
import io.aeronic.codec.DecodedBy;
import io.aeronic.codec.Encodable;

public class Balance implements Encodable
{

    private final String assetSymbol;
    private final double balance;

    public Balance(final String assetSymbol, final double balance)
    {
        this.assetSymbol = assetSymbol;
        this.balance = balance;
    }

    public String getAssetSymbol()
    {
        return assetSymbol;
    }

    public double getBalance()
    {
        return balance;
    }

    @Override
    public void encode(final BufferEncoder bufferEncoder)
    {
        bufferEncoder.encode(assetSymbol);
        bufferEncoder.encode(balance);
    }

    @DecodedBy
    public static Balance decode(final BufferDecoder bufferDecoder)
    {
        final String assetSymbol = bufferDecoder.decodeString();
        final double balance = bufferDecoder.decodeDouble();
        return new Balance(assetSymbol, balance);
    }
}
