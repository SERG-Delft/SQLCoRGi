package nl.tudelft.st01;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Contains tests for {@link Generator}.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
public class GeneratorTest {

    /**
     * A test case for a simple query containing only one condition with < as operator.
     */
    @Test
    public void testLessThanInteger() {
        String query = "SELECT * FROM table WHERE a < 100";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM table WHERE a = 99");
        expected.add("SELECT * FROM table WHERE a = 100");
        expected.add("SELECT * FROM table WHERE a = 101");
        expected.add("SELECT * FROM table WHERE a IS NULL");

        assertEquals(expected, result);
    }

    /**
     * A test case for a simple query containing only one condition with != as operator.
     */
    @Test
    public void testNotEqualToFloat() {
        String query = "SELECT * FROM table WHERE a <> 0.0";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM table WHERE a = -1.0");
        expected.add("SELECT * FROM table WHERE a = 0.0");
        expected.add("SELECT * FROM table WHERE a = 1.0");
        expected.add("SELECT * FROM table WHERE a IS NULL");

        assertEquals(expected, result);
    }

    /**
     * A test case for a simple query testing for string equality.
     */
    @Test
    public void testEqualToString() {
        String query = "SELECT * FROM table WHERE a = 'qwerty'";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM table WHERE a = 'qwerty'");
        expected.add("SELECT * FROM table WHERE a <> 'qwerty'");
        expected.add("SELECT * FROM table WHERE a IS NULL");

        assertEquals(expected, result);
    }

    /**
     * A test case for a simple query with IS NULL.
     */
    @Test
    public void testIsNull() {
        String query = "SELECT * FROM table WHERE a IS NOT NULL";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM table WHERE a IS NOT NULL");
        expected.add("SELECT * FROM table WHERE a IS NULL");

        assertEquals(expected, result);
    }

    /**
     * A test case with three conditions, combined with AND and OR.
     */
    @Test
    public void testThreeConditionsAndOr() {
        String query = "SELECT * FROM Table1 WHERE a1 = 11 OR a2 = 22 AND a3 = 33";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM Table1 WHERE (a1 = 10) AND NOT (a2 = 22 AND a3 = 33)");
        expected.add("SELECT * FROM Table1 WHERE (a1 = 11) AND NOT (a2 = 22 AND a3 = 33)");
        expected.add("SELECT * FROM Table1 WHERE (a1 = 12) AND NOT (a2 = 22 AND a3 = 33)");
        expected.add("SELECT * FROM Table1 WHERE (a1 IS NULL) AND NOT (a2 = 22 AND a3 = 33)");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 21) AND (a3 = 33))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 33))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 23) AND (a3 = 33))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 IS NULL) AND (a3 = 33))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 32))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 34))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 IS NULL))");

        assertEquals(expected, result);
    }

    /**
     * Parametrized test for a simple query with different join types with a single join condition which involves
     * nullable columns. All of which should result in the same expected output set.
     *
     * @param joinType Type of join.
     */

    @ParameterizedTest
    @CsvSource({"INNER", "RIGHT", "LEFT", "FULL"})
    public void testJoinsOnOneEqualityConditionWithNullableColumns(String joinType) {
        String query = "SELECT * FROM TableA " + joinType + " JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull");
        expected.add("SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull "
                + "WHERE (TableB.CanBeNull IS NULL) AND (TableA.CanBeNull IS NOT NULL)");
        expected.add("SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull "
                + "WHERE (TableB.CanBeNull IS NULL) AND (TableA.CanBeNull IS NULL)");
        expected.add("SELECT * FROM TableA RIGHT JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull "
                + "WHERE (TableA.CanBeNull IS NULL) AND (TableB.CanBeNull IS NOT NULL)");
        expected.add("SELECT * FROM TableA RIGHT JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull "
                + "WHERE (TableA.CanBeNull IS NULL) AND (TableB.CanBeNull IS NULL)");

        assertEquals(expected, result);
    }

    /**
     * A parametrized test for a  query with different join types with a two disjoint join conditions which involves
     * nullable columns. All of which should result in the same expected output set.
     *
     * @param joinType Type of join.
     */
    @ParameterizedTest
    @CsvSource({"INNER", "RIGHT", "LEFT", "FULL"})
    public void testJoinsOnTwoDisjointConditionsWithNullableColumns(String joinType) {
        String query = "SELECT * FROM TableA " + joinType + " JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull"
                + " OR TableA.CanBeNull2 = TableB.CanBeNull2";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull OR "
                + "TableA.CanBeNull2 = TableB.CanBeNull2");
        expected.add("SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull OR "
                + "TableA.CanBeNull2 = TableB.CanBeNull2 WHERE (TableB.CanBeNull IS NULL) AND "
                + "(TableB.CanBeNull2 IS NULL) AND (TableA.CanBeNull IS NOT NULL) AND (TableA.CanBeNull2 IS NOT NULL)");
        expected.add("SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull OR "
                + "TableA.CanBeNull2 = TableB.CanBeNull2 WHERE (TableB.CanBeNull IS NULL) AND "
                + "(TableB.CanBeNull2 IS NULL) AND (TableA.CanBeNull IS NULL) AND (TableA.CanBeNull2 IS NULL)");
        expected.add("SELECT * FROM TableA RIGHT JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull OR "
                + "TableA.CanBeNull2 = TableB.CanBeNull2 WHERE (TableA.CanBeNull IS NULL) AND "
                + "(TableA.CanBeNull2 IS NULL) AND (TableB.CanBeNull IS NOT NULL) AND (TableB.CanBeNull2 IS NOT NULL)");
        expected.add("SELECT * FROM TableA RIGHT JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull OR "
                + "TableA.CanBeNull2 = TableB.CanBeNull2 WHERE (TableA.CanBeNull IS NULL) AND "
                + "(TableA.CanBeNull2 IS NULL) AND (TableB.CanBeNull IS NULL) AND (TableB.CanBeNull2 IS NULL)");

        assertEquals(expected, result);
    }

    /**
     * Tests the most basic query, for which case no mutations should be generated.
     */
    @Test
    public void testSimpleQueryNoWhere() {
        String query = "SELECT * FROM TableA";
        Set<String> result = Generator.generateRules(query);
        assertEquals(result, new TreeSet<String>());

    }

    // THIS TEST REQUIRES COMPATIBILITY WITH WHERE AND JOIN. This is not yet implemented,
    // hence why the test is commented.
    /*
    /**
     * A parametrized test for a query with different join types with a single join condition which involves
     * nullable columns and a WHERE clause. All of which should result in the same expected output set.
     */
    /*
    @ParameterizedTest
    @CsvSource({"INNER", "RIGHT", "LEFT", "FULL"})
    public void testJoinsOnOneEqualityConditionWithNullableColumnsAndWHEREClause(String joinType) {
        String query = "SELECT * FROM TableA " + joinType + " JOIN TableB ON TableA.Var = TableB.Var " +
                "WHERE TableA.Value > 1";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value = 2");
        expected.add("SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value = 1");
        expected.add("SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value = 0");
        expected.add("SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value IS NULL");
        expected.add("SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE (TableA.Value > 1)");
        expected.add("SELECT * FROM TableA LEFT JOIN TableB ON TableA.Var = TableB.Var WHERE ((TableB.Var IS NULL) " +
                "AND (TableA.Var IS NOT NULL)) AND (TableA.Value > 1)");
        expected.add("SELECT * FROM TableA LEFT JOIN TableB ON TableA.Var = TableB.Var WHERE ((TableB.Var IS NULL) " +
                "AND (TableA.Var IS NULL)) AND (TableA.Value > 1)");
        expected.add("SELECT * FROM TableA RIGHT JOIN TableB ON TableA.Var = TableB.Var WHERE (TableA.Var IS NULL) " +
                "AND (TableB.Var IS NOT NULL)");
        expected.add("SELECT * FROM TableA RIGHT JOIN TableB ON TableA.Var = TableB.Var WHERE (TableA.Var IS NULL) " +
                "AND (TableB.Var IS NULL)");

        assertEquals(expected, result);
    }
    */
}
