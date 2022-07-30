package exchange.lob.math;

import org.agrona.ExpandableDirectByteBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static exchange.lob.math.Quantiser.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class QuantiserTest
{

    private static Stream<Arguments> minorUnitsArgsProvider()
    {
        return Stream.of(
            arguments(123.123, (byte) 2, 12313L),
            arguments(123.129, (byte) 2, 12313L),
            arguments(123.129254243, (byte) 2, 12313L),
            arguments(123.123, (byte) 8, 12312300000L),
            arguments(123.129, (byte) 8, 12312900000L)
        );
    }

    private static Stream<Arguments> decimalDecodingArgsProvider()
    {
        BiFunction<Long, Short, DecimalDecoder> decimalCreator = (value, scale) -> {
            final ExpandableDirectByteBuffer buffer = new ExpandableDirectByteBuffer();
            new DecimalEncoder().wrap(buffer, 0).value(value).scale(scale);
            return new DecimalDecoder().wrap(buffer, 0);
        };

        return Stream.of(
            arguments(decimalCreator.apply(150029L, (short)3), 150.029),
            arguments(decimalCreator.apply(150020L, (short)3), 150.020),
            arguments(decimalCreator.apply(150021L, (short)3), 150.021)
        );
    }

    private static Stream<Arguments> decimalEncodingArgsProvider()
    {
        Supplier<DecimalEncoder> decimalEncoderSupplier = () -> new DecimalEncoder().wrap(new ExpandableDirectByteBuffer(), 0);
        return Stream.of(
            arguments(decimalEncoderSupplier.get(), 150.029, 150029L, (short)3),
            arguments(decimalEncoderSupplier.get(), 150.020, 15002L, (short)2),
            arguments(decimalEncoderSupplier.get(), 150.021, 150021L, (short)3)
        );
    }

    @ParameterizedTest
    @MethodSource("decimalDecodingArgsProvider")
    public void shouldDecodeDecimal(DecimalDecoder decimal, double result)
    {
        assertThat(decodeDecimal(decimal)).isEqualTo(result);
    }

    @ParameterizedTest
    @MethodSource("decimalEncodingArgsProvider")
    public void shouldEncodeDecimal(DecimalEncoder decimal, double result, long value, short scale)
    {
        encodeDecimal(decimal, result);
        final DecimalDecoder decoder = new DecimalDecoder().wrap(decimal.buffer(), 0);
        assertEquals(value, decoder.value());
        assertEquals(scale, decoder.scale());
    }

    @ParameterizedTest
    @MethodSource("minorUnitsArgsProvider")
    public void shouldConvertToMinorUnits(double value, byte scale, long result)
    {
        assertThat(toMinorUnits(value, scale, Math::ceil)).isEqualTo(result);
    }

    private static Stream<Arguments> majorUnitsArgsProvider()
    {
        return Stream.of(
            arguments(12312L, (byte) 2, 123.12),
            arguments(12312300000L, (byte) 8, 123.12300000),
            arguments(12312900000L, (byte) 8, 123.12900000),
            arguments(123L, (byte) 4, 0.0123),
            arguments(1L, (byte) 4, 0.0001)
        );
    }

    @ParameterizedTest
    @MethodSource("majorUnitsArgsProvider")
    public void shouldConvertToMajorUnits(long value, byte scale, double result)
    {
        assertThat(toMajorUnits(value, scale)).isEqualTo(result);
    }
}
