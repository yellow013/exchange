package exchange.lob.fix.byteoperations;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class BitsTest
{

    byte[] data;

    @BeforeEach
    public void setUp()
    {
        data = new byte[20];
    }

    @Test
    public void shouldWriteAndReadLong() throws Exception
    {
        long expectedValue = 12893471928734L;
        Bits.writeLong(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readLong(data, 3));
    }

    @Test
    public void shouldWriteAndReadNegativeLong() throws Exception
    {
        long expectedValue = -722393471928734L;
        Bits.writeLong(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readLong(data, 3));
    }

    @Test
    public void shouldWriteAndReadInt() throws Exception
    {
        int expectedValue = 51345234;
        Bits.writeInt(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readInt(data, 3));
    }

    @Test
    public void shouldWriteAndReadNegativeInt() throws Exception
    {
        int expectedValue = -34574234;
        Bits.writeInt(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readInt(data, 3));
    }

    @Test
    public void shouldWriteAndReadShort() throws Exception
    {
        short expectedValue = 3452;
        Bits.writeShort(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readShort(data, 3));
    }

    @Test
    public void shouldWriteAndReadNegativeShort() throws Exception
    {
        short expectedValue = -8123;
        Bits.writeShort(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readShort(data, 3));
    }

    @Test
    public void shouldWriteAndReadByte() throws Exception
    {
        byte expectedValue = 23;
        Bits.writeShort(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readShort(data, 3));
    }

    @Test
    public void shouldWriteAndReadNegativeByte() throws Exception
    {
        byte expectedValue = -13;
        Bits.writeByte(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readByte(data, 3));
    }

    @Test
    public void shouldWriteAndReadBoolean() throws Exception
    {
        Bits.writeBoolean(data, 3, true);
        assertTrue(Bits.readBoolean(data, 3));
        Bits.writeBoolean(data, 3, false);
        assertFalse(Bits.readBoolean(data, 3));
    }

}
