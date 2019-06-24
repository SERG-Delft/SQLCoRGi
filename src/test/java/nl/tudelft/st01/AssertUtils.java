package nl.tudelft.st01;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class contains the functionality needed to verify correctness of the generated coverage targets.
 */
public final class AssertUtils {

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
    public static void verify(String query, String... expected) {
        Set<String> resultSet = Generator.generateRules(query);
        Set<String> expectedSet = new TreeSet<>(Arrays.asList(expected));

        assertThat(resultSet).isEqualTo(expectedSet);
    }

    /**
     * Asserts that the expected targets are contained in the results list.
     * @param query The input query that needs to be covered.
     * @param atLeast The expected output of the {@link Generator}
     */
    public static void containsAtLeast(String query, String... atLeast) {
        Set<String> resultSet = Generator.generateRules(query);

        assertThat(resultSet).contains(atLeast);
    }

    /**
     * Asserts that every object in the inputList is equal to an object in the expected list when comparing
     * field-by-field, recursively.
     *
     * @param <T> The type used by the lists.
     * @param inputList The list of arguments that should be compared
     * @param expected The expected outputs that the inputList should be compared with
     */
    @SuppressWarnings("checkstyle:NoWhiteSpaceBefore")
    public static <T> void compareFieldByField(List<T> inputList, T... expected) {
        List<T> expectedList = Arrays.asList(expected);
        if (expectedList.size() != inputList.size()) {
            throw new IllegalArgumentException("Lists have to have the same size");
        }

        for (int i = 0; i < inputList.size(); i++) {
            assertThat(inputList.get(i)).isEqualToComparingFieldByFieldRecursively(expectedList.get(i));
        }
    }
}
