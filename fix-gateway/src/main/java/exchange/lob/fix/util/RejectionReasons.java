package exchange.lob.fix.util;

import exchange.lob.api.codecs.fix.OrdRejReason;
import exchange.lob.domain.RejectionReason;

public class RejectionReasons
{
    public static OrdRejReason toFix(final RejectionReason rejectionReason)
    {
        return switch (rejectionReason)
            {
                case DUPLICATE_ORDER -> OrdRejReason.DUPLICATE_ORDER;
                case UNKNOWN_ORDER -> OrdRejReason.UNKNOWN_ORDER;
                case INVALID_PRODUCT -> OrdRejReason.UNKNOWN_SYMBOL;
                case NONE, INSUFFICIENT_LIQUIDITY, INSUFFICIENT_BALANCE, INVALID_USER, NULL_VAL -> OrdRejReason.BROKER_OPTION;
            };
    }
}
