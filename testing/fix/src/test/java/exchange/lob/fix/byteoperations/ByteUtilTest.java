package exchange.lob.fix.byteoperations;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;


public class ByteUtilTest
{
    @Test
    public void shouldFormatIpAddressAsString()
    {
        final int offset = 0;
        final int address = Bits.readInt(new byte[]{ (byte)192, (byte)168, (byte)70, (byte)12 }, offset);

        assertEquals("192.168.70.12", ByteUtil.formatIntAsIpAddress(address));
    }

    @Test
    public void shouldPackFourUnsignedShortIntegersIntoLong()
    {
        final int int1 = 45454;
        final int int2 = 32234;
        final int int3 = 12231;
        final int int4 = 65000;

        final long longValue = ByteUtil.packLongWithUnsignedShortInts(int1, int2, int3, int4);

        final byte[] longBytes = new byte[8];
        Bits.writeLong(longBytes, 0, longValue);

        assertEquals(int1, Bits.readShort(longBytes, 0) & 0xFFFF);
        assertEquals(int2, Bits.readShort(longBytes, 2) & 0xFFFF);
        assertEquals(int3, Bits.readShort(longBytes, 4) & 0xFFFF);
        assertEquals(int4, Bits.readShort(longBytes, 6) & 0xFFFF);
    }

    @Test
    public void shouldThrowExceptionForIntLargerThanMaxUnsignedShort()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            final int int1 = (1 << 16);
            final int int2 = 32234;
            final int int3 = 12231;
            final int int4 = 65000;

            ByteUtil.packLongWithUnsignedShortInts(int1, int2, int3, int4);
        });
    }

    @Test
    public void shouldWriteLongAsAscii()
    {
        final byte[] longAsAsciiBytes = new byte[19];

        final int offset = 0;
        ByteUtil.writeLongAsAscii(longAsAsciiBytes, offset, 0L);
        assertArrayEquals("0000000000000000000".getBytes(StandardCharsets.US_ASCII), longAsAsciiBytes);

        ByteUtil.writeLongAsAscii(longAsAsciiBytes, offset, 7L);
        assertArrayEquals("0000000000000000007".getBytes(StandardCharsets.US_ASCII), longAsAsciiBytes);

        ByteUtil.writeLongAsAscii(longAsAsciiBytes, offset, 900000000000000000L);
        assertArrayEquals("0900000000000000000".getBytes(StandardCharsets.US_ASCII), longAsAsciiBytes);

        ByteUtil.writeLongAsAscii(longAsAsciiBytes, offset, 99999999999999999L);
        assertArrayEquals("0099999999999999999".getBytes(StandardCharsets.US_ASCII), longAsAsciiBytes);

        ByteUtil.writeLongAsAscii(longAsAsciiBytes, offset, 899999999999999999L);
        assertArrayEquals("0899999999999999999".getBytes(StandardCharsets.US_ASCII), longAsAsciiBytes);
    }

    @Test
    public void shouldThrowExceptionWhenOutOfRangeForMaxLong()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            final byte[] longAsAscii = new byte[19];

            ByteUtil.writeLongAsAscii(longAsAscii, 0, Long.MAX_VALUE);
        });
    }

    @Test
    public void shouldThrowExceptionWhenOutOfRangeForMinLong()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            final byte[] longAsAscii = new byte[19];

            ByteUtil.writeLongAsAscii(longAsAscii, 0, Long.MIN_VALUE);
        });
    }

    @Test
    public void shouldThrowExceptionWhenJustOutOfRange()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            final byte[] longAsAscii = new byte[19];

            ByteUtil.writeLongAsAscii(longAsAscii, 0, 9000000000000000001L);
        });
    }

    @Test
    public void shouldIdentifyDigitsInAsciiCharacters() throws UnsupportedEncodingException
    {
        assertTrue(ByteUtil.isAsciiDigit("0".getBytes(StandardCharsets.US_ASCII)[0]), "Is a digit");
        assertTrue(ByteUtil.isAsciiDigit("1".getBytes(StandardCharsets.US_ASCII)[0]), "Is a digit");
        assertTrue(ByteUtil.isAsciiDigit("2".getBytes(StandardCharsets.US_ASCII)[0]), "Is a digit");
        assertTrue(ByteUtil.isAsciiDigit("3".getBytes(StandardCharsets.US_ASCII)[0]), "Is a digit");
        assertTrue(ByteUtil.isAsciiDigit("4".getBytes(StandardCharsets.US_ASCII)[0]), "Is a digit");
        assertTrue(ByteUtil.isAsciiDigit("5".getBytes(StandardCharsets.US_ASCII)[0]), "Is a digit");
        assertTrue(ByteUtil.isAsciiDigit("6".getBytes(StandardCharsets.US_ASCII)[0]), "Is a digit");
        assertTrue(ByteUtil.isAsciiDigit("7".getBytes(StandardCharsets.US_ASCII)[0]), "Is a digit");
        assertTrue(ByteUtil.isAsciiDigit("8".getBytes(StandardCharsets.US_ASCII)[0]), "Is a digit");
        assertTrue(ByteUtil.isAsciiDigit("9".getBytes(StandardCharsets.US_ASCII)[0]), "Is a digit");

        assertFalse(ByteUtil.isAsciiDigit("/".getBytes(StandardCharsets.US_ASCII)[0]), "Not a digit");
        assertFalse(ByteUtil.isAsciiDigit("A".getBytes(StandardCharsets.US_ASCII)[0]), "Not a digit");
        assertFalse(ByteUtil.isAsciiDigit("z".getBytes(StandardCharsets.US_ASCII)[0]), "Not a digit");
        assertFalse(ByteUtil.isAsciiDigit(";".getBytes(StandardCharsets.US_ASCII)[0]), "Not a digit");
    }

    @Test
    public void shouldReadIntegerFromAsciiBytes()
        throws UnsupportedEncodingException
    {
        checkIntegerEncodedAsAsciiValue("0");
        checkIntegerEncodedAsAsciiValue("00");
        checkIntegerEncodedAsAsciiValue("1");
        checkIntegerEncodedAsAsciiValue("10");
        checkIntegerEncodedAsAsciiValue("010");
        checkIntegerEncodedAsAsciiValue("7");
        checkIntegerEncodedAsAsciiValue("123");
        checkIntegerEncodedAsAsciiValue("99999");
    }

    @Test
    public void shouldReadLongFromAsciiBytes()
        throws UnsupportedEncodingException
    {
        checkLongEncodedAsAsciiValue("0");
        checkLongEncodedAsAsciiValue("00");
        checkLongEncodedAsAsciiValue("1");
        checkLongEncodedAsAsciiValue("10");
        checkLongEncodedAsAsciiValue("010");
        checkLongEncodedAsAsciiValue("7");
        checkLongEncodedAsAsciiValue("123");
        checkLongEncodedAsAsciiValue("99999");
        checkLongEncodedAsAsciiValue("999999999999");
        checkLongEncodedAsAsciiValue("" + Long.MAX_VALUE);
    }

    @Test
    public void shouldConfirmIsIntegerValue() throws UnsupportedEncodingException
    {
        final byte[] bytes = "123".getBytes(StandardCharsets.US_ASCII);
        assertTrue(ByteUtil.isInteger(bytes, 0, bytes.length));
    }

    @Test
    public void shouldConfirmIsNotIntegerValue() throws UnsupportedEncodingException
    {
        final byte[] bytes = "1;3".getBytes(StandardCharsets.US_ASCII);
        assertFalse(ByteUtil.isInteger(bytes, 0, bytes.length));
    }

    @Test
    public void shouldReplaceSohWithBar()
        throws Exception
    {
        final byte[] source = "A test string \u0001 containing \u0001 ASCII SOH\u0001".getBytes(StandardCharsets.US_ASCII);
        final byte[] expected = "A test string | containing | ASCII SOH|".getBytes(StandardCharsets.US_ASCII);

        final byte soh = (byte)1;
        final byte bar = (byte)124;
        ByteUtil.replace(source, 0, source.length, soh, bar);

        assertArrayEquals(expected, source);
    }

    @Test
    public void shouldEncodeStringAsAscii() throws UnsupportedEncodingException
    {
        final String testSequence = "This is a string of characters that should encode into ascii bytes as simple as 123.";

        final byte[] result = new byte[testSequence.length()];

        ByteUtil.asciiEncode(testSequence, 0, result, 0, result.length);

        assertArrayEquals(testSequence.getBytes(StandardCharsets.US_ASCII), result);
    }

    @Test
    public void shouldOnlyAllowUsAscii()
    {
        assertThrows(IllegalArgumentException.class, () -> {
            final String testSequence = "The £ symbol is not allowed in US-ASCII";

            final byte[] result = new byte[testSequence.length()];

            ByteUtil.asciiEncode(testSequence, 0, result, 0, result.length);
        });
    }


    private void checkIntegerEncodedAsAsciiValue(final String integerAsString)
        throws UnsupportedEncodingException
    {
        final byte[] bytes = integerAsString.getBytes(StandardCharsets.US_ASCII);
        final int value = ByteUtil.readIntFromAscii(bytes, 0, bytes.length);
        assertEquals(Integer.parseInt(integerAsString), value);
    }

    private void checkLongEncodedAsAsciiValue(final String longAsString)
        throws UnsupportedEncodingException
    {
        final byte[] bytes = longAsString.getBytes(StandardCharsets.US_ASCII);
        final long value = ByteUtil.readLongFromAscii(bytes, 0, bytes.length);
        assertEquals(Long.parseLong(longAsString), value);
    }

}
