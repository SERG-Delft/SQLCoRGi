package com.github.sergdelft.sqlcorgi;

import com.github.sergdelft.sqlcorgi.schema.Column;
import com.github.sergdelft.sqlcorgi.schema.Schema;
import com.github.sergdelft.sqlcorgi.schema.Table;

import java.util.*;

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
     * Creates a schema which can be used in tests. Consists of two tables:
     * 'Movies' => (title => STRING, Director => STRING NULL, year NUMERIC NULL)
     * 't' => (a NUMERIC NULL, b STRING NULL, c NUMERIC NULL)
     *
     * @return the schema as described above.
     */
    public static Schema makeSchema() {

        ArrayList<Column> moviesColumns = new ArrayList<>();
        moviesColumns.add(new Column("title", false, false, Column.DataType.STRING));
        moviesColumns.add(new Column("Director", true, false, Column.DataType.STRING));
        moviesColumns.add(new Column("year", true, false, Column.DataType.NUM));

        Table moviesTable = new Table("Movies", moviesColumns);


        ArrayList<Column> tColumns = new ArrayList<>();
        tColumns.add(new Column("a", true, false, Column.DataType.NUM));
        tColumns.add(new Column("b", true, false, Column.DataType.STRING));
        tColumns.add(new Column("c", true, false, Column.DataType.NUM));

        Table tTable = new Table("t", tColumns);

        HashMap<String, Table> tables = new HashMap<>();
        tables.put(moviesTable.getName(), moviesTable);
        tables.put(tTable.getName(), tTable);

        return new Schema(tables);
    }

    /**
     * Asserts that the right set of coverage targets are generated for a given input query.
     * @param query the input query that needs to be covered.
     * @param schema the schema to use with the query.
     * @param expected the expected output of the generator.
     */
    public static void verify(String query, Schema schema, String... expected) {
        Set<String> resultSet = SQLCorgi.generateRules(query, schema);
        Set<String> expectedSet = new TreeSet<>(Arrays.asList(expected));

        assertThat(resultSet).isEqualTo(expectedSet);
    }

    /**
     * Asserts that the expected targets are contained in the results list.
     * @param query The input query that needs to be covered.
     * @param atLeast The expected output of the {@link SQLCorgi}
     */
    public static void containsAtLeast(String query, String... atLeast) {
        containsAtLeast(query, null, atLeast);
    }

    /**
     * Asserts that the expected targets are contained in the results list.
     * @param query The input query that needs to be covered.
     * @param schema the schema to use for the query under test.
     * @param atLeast The expected output of the {@link SQLCorgi}
     */
    public static void containsAtLeast(String query, Schema schema, String... atLeast) {
        Set<String> resultSet = SQLCorgi.generateRules(query, schema);

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
    @SafeVarargs
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
