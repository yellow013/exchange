package exchange.lob;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.awaitility.core.ThrowingRunnable;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class Assertions
{
    public static <T> void assertReflectiveEquals(final Object expected, final T actual)
    {
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    public static <T> void assertReflectiveContainsAll(final Iterable<T> expectedToContain, final Iterable<T> actual, final String... ignoredFields)
    {
        assertThat(actual).usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                .withIgnoredFields(ignoredFields)
                .build())
            .containsAll(expectedToContain);
    }

    public static <T> void assertReflectiveContains(final T expectedToContain, final Iterable<T> actual, final String... ignoredFields)
    {
        assertThat(actual)
            .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                .withIgnoredFields(ignoredFields)
                .build())
            .contains(expectedToContain);
    }

    public static <T> void assertReflectiveContains(
        final T expectedToContain,
        final Iterable<T> actual,
        final Consumer<T> onMatch,
        final String... ignoredFields
    )
    {
        assertReflectiveContains(expectedToContain, actual, ignoredFields);
        StreamSupport.stream(actual.spliterator(), false)
            .filter(a -> reflectiveEquals(expectedToContain, a))
            .findFirst()
            .ifPresent(onMatch);
    }

    private static <T> boolean reflectiveEquals(final Object expected, final T actual)
    {
        try
        {
            assertReflectiveEquals(expected, actual);
            return true;
        }
        catch (final AssertionError error)
        {
            return false;
        }
    }

    public static void assertEventually(final ThrowingRunnable runnable)
    {
        assertEventually(runnable, Duration.ofSeconds(30));
    }

    public static void assertEventually(final ThrowingRunnable runnable, final Duration timeout)
    {
        await()
            .pollInterval(Duration.ofSeconds(1))
            .atMost(timeout)
            .untilAsserted(runnable);
    }
}
