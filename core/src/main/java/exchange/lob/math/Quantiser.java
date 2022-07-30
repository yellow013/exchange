package exchange.lob.math;

import java.math.BigDecimal;
import java.util.function.DoubleUnaryOperator;


public class Quantiser
{
    public static long toMinorUnits(final double value, final byte scale, final DoubleUnaryOperator rounder)
    {
        return (long)rounder.applyAsDouble(value * Math.pow(10, scale));
    }

    public static long toMinorUnits(double value, byte scale)
    {
        return toMinorUnits(value, scale, Math::ceil);
    }

    public static long toMinorUnits(final DecimalDecoder decimal, final byte scale)
    {
        final int scaleDiff = scale - decimal.scale();
        return decimal.value() * (long)Math.pow(10, scaleDiff);
    }

    public static double toMajorUnits(final long value, final byte scale)
    {
        return value / Math.pow(10, scale);
    }

    public static double toMajorUnits(final long value, final short scale)
    {
        return value / Math.pow(10, scale);
    }

    public static double decodeDecimal(final DecimalDecoder decimal)
    {
        return decimal.value() / Math.pow(10, decimal.scale());
    }

    public static void encodeDecimal(final DecimalEncoder decimal, final double doubleValue)
    {
        final short scale = (short)getNumberOfDecimalPlaces(doubleValue);
        final long value = (long)(doubleValue * Math.pow(10, scale));
        decimal.scale(scale).value(value);
    }

    private static int getNumberOfDecimalPlaces(final double value)
    {
        return Math.max(0, BigDecimal.valueOf(value).scale());
    }
}
