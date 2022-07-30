package exchange.lob.domain;

public enum ExchangeResponseCode
{
    SUCCESS((short)100),

    NO_OP((short)0),

    FAILURE((short)-1),

    DUPLICATE_PRODUCT((short)-2),

    INVALID_USER((short)-3),

    INVALID_ASSET((short)-4),

    INVALID_PRODUCT((short)-5),

    INVALID_CANCELLATION((short)-6),

    BALANCE_OVERDRAWN((short)-7),

    INSUFFICIENT_BALANCE((short)-8),

    INSUFFICIENT_LIQUIDITY((short)-9),

    INVALID_USERNAME((short)-10),

    INVALID_PASSWORD((short)-11),

    /**
     * To be used to represent not present or null.
     */
    NULL_VAL((short)-32768);

    private final short value;

    ExchangeResponseCode(final short value)
    {
        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public static ExchangeResponseCode get(final short value)
    {
        switch (value)
        {
            case 100: return SUCCESS;
            case 0: return NO_OP;
            case -1: return FAILURE;
            case -2: return DUPLICATE_PRODUCT;
            case -3: return INVALID_USER;
            case -4: return INVALID_ASSET;
            case -5: return INVALID_PRODUCT;
            case -6: return INVALID_CANCELLATION;
            case -7: return BALANCE_OVERDRAWN;
            case -8: return INSUFFICIENT_BALANCE;
            case -9: return INSUFFICIENT_LIQUIDITY;
            case -10: return INVALID_USERNAME;
            case -11: return INVALID_PASSWORD;
            case -32768: return NULL_VAL;
        }

        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
