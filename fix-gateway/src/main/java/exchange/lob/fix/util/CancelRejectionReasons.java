package exchange.lob.fix.util;

import exchange.lob.api.codecs.fix.CxlRejReason;
import exchange.lob.domain.RejectionReason;

public class CancelRejectionReasons
{
    public static CxlRejReason toFix(final RejectionReason rejectionReason)
    {
        return switch (rejectionReason)
            {
                case UNKNOWN_ORDER -> CxlRejReason.UNKNOWN_ORDER;
                case INSUFFICIENT_LIQUIDITY, INVALID_USER, INVALID_PRODUCT, DUPLICATE_ORDER, NULL_VAL, NONE, INSUFFICIENT_BALANCE -> null;
            };
    }
}
