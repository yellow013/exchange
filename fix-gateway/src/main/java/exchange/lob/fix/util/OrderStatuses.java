package exchange.lob.fix.util;

import exchange.lob.api.codecs.fix.OrdStatus;
import exchange.lob.domain.OrderStatus;

public class OrderStatuses
{

    public static OrdStatus toFix(final OrderStatus orderStatus)
    {
        return switch (orderStatus)
            {
                case NEW -> OrdStatus.NEW;
                case FILLED -> OrdStatus.FILLED;
                case CANCELLED -> OrdStatus.CANCELED;
                case PARTIALLY_FILLED -> OrdStatus.PARTIALLY_FILLED;
                case REJECTED -> OrdStatus.REJECTED;
                case NULL_VAL -> OrdStatus.NULL_VAL;
            };
    }
}
