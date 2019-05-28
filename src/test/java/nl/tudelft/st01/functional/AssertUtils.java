package nl.tudelft.st01.functional;

import nl.tudelft.st01.Generator;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class contains the functionality needed to verify correctness of the generated coverage targets.
 */
final class AssertUtils {

    /**
     * Prevents instantiation of {@link AssertUtils}.
     *
     * @throws UnsupportedOperationException always
     */
    protected AssertUtils() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Asserts that the right set of coverage targets are generated for a given input query.
     *
     * @param query The input query that needs to be covered
     * @param expected The expected output of the {@link Generator}
     */
    static void verify(String query, String... expected) {
        Set<String> resultSet = Generator.generateRules(query);
        Set<String> expectedSet = new TreeSet<>(Arrays.asList(expected));

        assertThat(resultSet).isEqualTo(expectedSet);
    }

    /**
     * Asserts that the expected targets are contained in the results list.
     * @param query The input query that needs to be covered.
     * @param atLeast The expected output of the {@link Generator}
     */
    static void containsAtLeast(String query, String... atLeast) {
        Set<String> resultSet = Generator.generateRules(query);

        assertThat(resultSet).contains(atLeast);
    }
}
