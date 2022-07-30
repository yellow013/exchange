package exchange.lob.fix.incoming;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FixMessageFactoryTest
{
    private static final byte ONE = 1;
    private static final byte TWO = 1;
    private static final byte THREE = 1;

    @Test
    public void shouldConstructFixMessage()
    {
        final byte[] msg1 = {ONE, TWO, THREE};

        final FixMessageStreamFactory fixMessageStreamFactory = new FixMessageStreamFactory(new FixMessageHandler()
        {
            @Override
            public void onFixMessage(final FixMessage fixMessage)
            {
                assertEquals(fixMessage.getFirstValue(1), new String(msg1, 0, 1));
                assertEquals(fixMessage.getFirstValue(2), new String(msg1, 1, 2));

            }
        });

        fixMessageStreamFactory.messageStart();
        fixMessageStreamFactory.onTag(1, msg1, 0, 1);
        fixMessageStreamFactory.onTag(2, msg1, 1, 2);
        fixMessageStreamFactory.messageEnd();
    }
}
