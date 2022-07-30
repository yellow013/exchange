package exchange.lob.fix.outgoing;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.nio.ByteBuffer;
import java.util.Collection;

public class ByteBufferMatcher extends TypeSafeMatcher<ByteBuffer>
{

    private final Collection<FixMessage> expected;

    ByteBufferMatcher(final Collection<FixMessage> expected)
    {
        this.expected = expected;
    }

    @Override
    public boolean matchesSafely(final ByteBuffer byteBuffer)
    {
        int matchingAt = 0;
        final byte[] array = byteBuffer.array();
        for (FixMessage message : expected)
        {
            final byte[] bytes = message.toFixString().getBytes();

            for (int j = 0; j < bytes.length; j++)
            {
                if (bytes[j] != array[j + matchingAt])
                {
                    return false;
                }
            }

            matchingAt += bytes.length;
        }
        return true;
    }

    @Override
    public void describeTo(final Description description)
    {
        final StringBuilder humanlyExpected = new StringBuilder();

        for (FixMessage message : expected)
        {
            humanlyExpected.append(message.toFixString());
        }

        description.appendText(humanlyExpected.toString());
    }
}
