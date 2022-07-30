package exchange.lob.fix.incoming;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class FixMessageTest
{

    private static final char SOH_CHAR = '\u0001';
    private static final char PIPE_CHAR = '|';

    @Test
    public void shouldReturnFixMessage()
    {
        final Multimap<Integer, String> multimap = ArrayListMultimap.create();
        multimap.put(1, "firstKey");
        multimap.put(2, "secondKey");
        multimap.put(3, "thirdKey");

        final FixMessage fixMessage = new FixMessage(multimap);
        final String expectedString = "1=firstKey" + SOH_CHAR + "2=secondKey" + SOH_CHAR + "3=thirdKey" + SOH_CHAR;
        assertEquals(fixMessage.toFixString(), expectedString);

    }

    @Test
    public void shouldReturnFixMessageWithDuplicateKeys()
    {
        final Multimap<Integer, String> multimap = ArrayListMultimap.create();
        multimap.put(1, "firstKey");
        multimap.put(2, "secondKey");
        multimap.put(2, "anotherSecondKey");
        multimap.put(3, "thirdKey");

        final FixMessage fixMessage = new FixMessage(multimap);
        final String expectedString = "1=firstKey" + SOH_CHAR + "2=secondKey" + SOH_CHAR + "2=anotherSecondKey" + SOH_CHAR + "3=thirdKey" + SOH_CHAR;
        assertEquals(fixMessage.toFixString(), expectedString);

    }

    @Test
    public void shouldReturnFixMessageWithPipeSeparator()
    {
        final Multimap<Integer, String> multimap = ArrayListMultimap.create();
        multimap.put(1, "firstKey");
        multimap.put(2, "secondKey");
        multimap.put(3, "thirdKey");

        final FixMessage fixMessage = new FixMessage(multimap);
        final String expectedString = "1=firstKey" + PIPE_CHAR + "2=secondKey" + PIPE_CHAR + "3=thirdKey" + PIPE_CHAR;
        assertEquals(fixMessage.toHumanString(), expectedString);

    }

    @Test
    public void shouldReturnFalseIfFixMessageDoesNotHaveTag() throws Exception
    {
        final Multimap<Integer, String> multimap = ArrayListMultimap.create();

        final FixMessage fixMessage = new FixMessage(multimap);
        assertFalse(fixMessage.hasValue(23));
    }

    @Test
    public void shouldReturnFixMessageWithPipeSeparatorWithDuplicateKeys() throws Exception
    {
        final Multimap<Integer, String> multimap = ArrayListMultimap.create();
        multimap.put(1, "firstKey");
        multimap.put(2, "secondKey");
        multimap.put(2, "anotherSecondKey");
        multimap.put(3, "thirdKey");

        final FixMessage fixMessage = new FixMessage(multimap);
        final String expectedString = "1=firstKey" + PIPE_CHAR + "2=secondKey" + PIPE_CHAR + "2=anotherSecondKey" + PIPE_CHAR + "3=thirdKey" + PIPE_CHAR;
        assertEquals(fixMessage.toHumanString(), expectedString);
    }
}
