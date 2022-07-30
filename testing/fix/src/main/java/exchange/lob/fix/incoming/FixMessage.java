package exchange.lob.fix.incoming;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class FixMessage
{
    private final Multimap<Integer, String> multimap;

    public FixMessage(final Multimap<Integer, String> multimap)
    {
        this.multimap = LinkedListMultimap.create(multimap);
    }

    public Collection<String> getValues(int tagId)
    {
        return multimap.get(tagId);
    }

    public String getFirstValue(int tagId)
    {
        return multimap.get(tagId).iterator().next();
    }

    public Optional<String> getValue(final int tagId)
    {
        return hasValue(tagId) ? Optional.of(getFirstValue(tagId)) : Optional.empty();
    }

    public boolean hasValue(int tagId)
    {
        return !multimap.get(tagId).isEmpty();
    }

    public void replace(int tagId, String value)
    {
        final LinkedList<String> values = new LinkedList<String>();
        values.add(value);
        multimap.replaceValues(tagId, values);
    }

    public String toFixString()
    {
        final char tagSeparator = '\u0001';

        return buildString(tagSeparator);
    }

    public String toHumanString()
    {
        final char tagSeparator = '|';
        return buildString(tagSeparator);
    }

    @Override
    public String toString()
    {
        return toHumanString();
    }

    private String buildString(final char tagSeparator)
    {
        final StringBuilder stringBuilder = new StringBuilder();
        final Collection<Map.Entry<Integer, String>> entries = multimap.entries();
        for (Map.Entry<Integer, String> entry : entries)
        {
            stringBuilder.append(entry.getKey()).append('=').append(entry.getValue()).append(tagSeparator);
        }
        return stringBuilder.toString();
    }

}
