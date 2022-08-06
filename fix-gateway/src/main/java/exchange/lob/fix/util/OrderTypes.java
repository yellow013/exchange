package exchange.lob.fix.util;

import exchange.lob.api.codecs.fix.OrdType;
import exchange.lob.domain.OrderType;

public class OrderTypes
{

    public static OrderType toInternal(OrdType ordType)
    {
        return switch (ordType)
            {
                case LIMIT -> OrderType.LMT;
                case MARKET -> OrderType.MKT;
                case NULL_VAL, ARTIO_UNKNOWN -> OrderType.NULL_VAL;
            };
    }
}
