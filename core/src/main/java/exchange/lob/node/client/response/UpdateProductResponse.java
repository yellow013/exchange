package exchange.lob.node.client.response;

import exchange.lob.domain.ExchangeResponseCode;

public class UpdateProductResponse
{
    private ExchangeResponseCode code;

    public void code(final ExchangeResponseCode code)
    {
        this.code = code;
    }

    public ExchangeResponseCode code()
    {
        return code;
    }
}
