package exchange.lob.match.util;


import exchange.lob.domain.Side;

import static exchange.lob.domain.Side.*;

public class Sides
{
    public static Side other(final Side side)
    {
        return switch (side)
            {
                case BID -> ASK;
                case ASK -> BID;
                case NULL_VAL -> NULL_VAL;
            };
    }

    public static exchange.lob.api.sbe.Side other(final exchange.lob.api.sbe.Side side)
    {
        return switch (side)
            {
                case BID -> exchange.lob.api.sbe.Side.ASK;
                case ASK -> exchange.lob.api.sbe.Side.BID;
                case NULL_VAL -> exchange.lob.api.sbe.Side.NULL_VAL;
            };
    }
}
