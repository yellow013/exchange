package exchange.lob.fix.util;


import exchange.lob.domain.Side;

import static exchange.lob.domain.Side.*;

public class Sides
{

    public static exchange.lob.api.codecs.fix.Side toFix(Side side)
    {
        return switch (side)
            {
                case BID -> exchange.lob.api.codecs.fix.Side.BUY;
                case ASK -> exchange.lob.api.codecs.fix.Side.SELL;
                case NULL_VAL -> exchange.lob.api.codecs.fix.Side.NULL_VAL;
            };
    }

    public static Side toInternal(exchange.lob.api.codecs.fix.Side side)
    {
        return switch (side)
            {
                case BUY -> BID;
                case SELL -> ASK;
                case NULL_VAL, ARTIO_UNKNOWN -> NULL_VAL;
            };
    }
}
