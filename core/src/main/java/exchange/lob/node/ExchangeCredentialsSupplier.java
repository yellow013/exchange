package exchange.lob.node;

import io.aeron.security.CredentialsSupplier;
import org.agrona.collections.ArrayUtil;

public class ExchangeCredentialsSupplier implements CredentialsSupplier
{

    private final String name;

    public ExchangeCredentialsSupplier(String name)
    {
        this.name = name;
    }

    @Override
    public byte[] encodedCredentials()
    {
        return name.getBytes();
    }

    @Override
    public byte[] onChallenge(byte[] encodedChallenge)
    {
        return ArrayUtil.EMPTY_BYTE_ARRAY;
    }
}
