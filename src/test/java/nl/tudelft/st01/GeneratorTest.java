package nl.tudelft.st01;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains tests for {@link Generator}.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
public class GeneratorTest {

    /**
     * A test case to check if an invalid query return nothing.
     */
    @Test
    public void testInvalidQuery() {
        String query = "ELECT * ROM invalid WERE statement = 5";
        Set<String> result = Generator.generateRules(query);
        Set<String> expected = new TreeSet<>();

        assertEquals(expected, result);
    }

    /**
     * A test case to check if a non-select query throws the proper exception.
     */
    @Test
    public void testNonSelectQuery() {
        String query = "ALTER TABLE Customers ADD Email varchar(255);";
        assertThrows(IllegalArgumentException.class, () ->
                Generator.generateRules(query)
        );
    }

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
     * A test case for a simple query containing only one condition with <= as operator.
     */
    @Test
    public void testLessThanEqualsInteger() {
        String query = "SELECT * FROM table WHERE a <= 100";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM table WHERE a = 99");
        expected.add("SELECT * FROM table WHERE a = 100");
        expected.add("SELECT * FROM table WHERE a = 101");
        expected.add("SELECT * FROM table WHERE a IS NULL");

        assertEquals(expected, result);
    }

    /**
     * A test simple test case for the > (GreaterThan) operator.
     */
    @Test
    public void testGreaterThanInteger() {
        String query = "SELECT * FROM Table WHERE x > 28";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM Table WHERE x = 27");
        expected.add("SELECT * FROM Table WHERE x = 28");
        expected.add("SELECT * FROM Table WHERE x = 29");
        expected.add("SELECT * FROM Table WHERE x IS NULL");

        assertEquals(expected, result);
    }

    /**
     * A test simple test case for the > (GreaterThan) operator.
     */
    @Test
    public void testGreaterThanEqualsInteger() {
        String query = "SELECT * FROM Table WHERE x >= 37";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM Table WHERE x = 36");
        expected.add("SELECT * FROM Table WHERE x = 37");
        expected.add("SELECT * FROM Table WHERE x = 38");
        expected.add("SELECT * FROM Table WHERE x IS NULL");

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
        assertEquals(new TreeSet<String>(), result);

    }

    // THIS TEST REQUIRES COMPATIBILITY WITH WHERE AND JOIN. This is not yet implemented,
    // hence why the test is commented.


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
    /**
     * A test case with a BETWEEN condition.
     */
    @Test
    public void testBetweenCondition() {
        String query = "SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37";
        String negatedQuery = "SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37";

        Set<String> result1 = Generator.generateRules(query);
        Set<String> result2 =   Generator.generateRules(negatedQuery);

        Set<String> expected = new TreeSet<>();
        expected.add(query);
        expected.add(negatedQuery);
        expected.add("SELECT * FROM Table1 WHERE x IS NULL");

        assertAll(
            () -> assertEquals(expected, result1),
            () -> assertEquals(expected, result2)
        );
    }

    /**
     * A test case with an IN condition.
     */
    @Test
    public void testInCondition() {
        String query = "SELECT * FROM Table1 WHERE x IN (30, 38)";
        String negatedQuery = "SELECT * FROM Table1 WHERE x NOT IN (30, 38)";

        Set<String> result1 = Generator.generateRules(query);
        Set<String> result2 = Generator.generateRules(negatedQuery);

        Set<String> expected = new TreeSet<>();
        expected.add(query);
        expected.add(negatedQuery);
        expected.add("SELECT * FROM Table1 WHERE x IS NULL");

        assertAll(
            () -> assertEquals(expected, result1),
            () -> assertEquals(expected, result2)
        );
    }

    /**
     * A test case with an LIKE condition.
     */
    @Test
    public void testLikeCondition() {
        String query = "SELECT * FROM Table1 WHERE name LIKE 'John%'";
        String negatedQuery = "SELECT * FROM Table1 WHERE name NOT LIKE 'John%'";

        Set<String> result1 = Generator.generateRules(query);
        Set<String> result2 = Generator.generateRules(negatedQuery);

        Set<String> expected = new TreeSet<>();
        expected.add(query);
        // JSQLParser generates "NOT name LIKE" instead of "name NOT LIKE", they are however identical in behavior,
        // therefore we stick with the behavior used in JSQLParser
        expected.add("SELECT * FROM Table1 WHERE NOT name LIKE 'John%'");
        expected.add("SELECT * FROM Table1 WHERE name IS NULL");

        assertAll(
            () -> assertEquals(expected, result1),
            () -> assertEquals(expected, result2)
        );
    }

    // * ======================================== *
    // * INCOMPATIBLE WITH AGGREGATOR FUNCTIONS.  *
    // * ======================================== *


    /**
     * A test case with 1 column and 1 aggregator, in this case AVG.
     */
    @Test
    public void testAVGAggregator1column1Aggr() {
        String query = "SELECT Director, AVG(Length) FROM Movies GROUP BY Director";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();

        expected.add("SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Director) > 1");

        expected.add("SELECT Director, AVG(Length) FROM Movies GROUP BY Director "
                + "HAVING COUNT(*) > 1");

        expected.add("SELECT Director, AVG(Length) FROM Movies GROUP BY Director "
                + "HAVING COUNT(*) > COUNT(Length) AND COUNT(DISTINCT Length) > 1");

        expected.add("SELECT Director, AVG(Length) FROM Movies GROUP BY Director "
                + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1");

        assertEquals(expected, result);
    }



    /**
     * A test case with 1 column and 2 aggregators, in this case AVG and Sum.
     */
    @Test
    public void testSUMAVGAggregator1column2Aggr() {
        String query = "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();

        expected.add("SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Director) > 1");
        expected.add("SELECT Director, AVG(Score), SUM(Length) FROM Movies "
                    + "GROUP BY Director HAVING COUNT(*) > 1");

        expected.add("SELECT Director, AVG(Score), SUM(Length) FROM Movies "
                    + "GROUP BY Director HAVING COUNT(*) > COUNT(Score) AND COUNT(DISTINCT Score) > 1");

        expected.add("SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(*) > COUNT(Length) AND COUNT(DISTINCT Length) > 1");

        expected.add("SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(Score) > COUNT(DISTINCT Score) AND COUNT(DISTINCT Score) > 1");

        expected.add("SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1");

        assertEquals(expected, result);
    }


    /**
     * A test case with 2 columns and 1 aggregator, in this case MAX.
     */
    @Test
    public void testMAXAggregator2columns1Aggr() {
        String query = "SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();

        expected.add("SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Name) > 1");

        expected.add("SELECT Director, Name, MAX(Length) FROM Movies "
                + "GROUP BY Name HAVING COUNT(*) > 1");

        expected.add("SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name "
                + "HAVING COUNT(*) > COUNT(Length) AND COUNT(DISTINCT Length) > 1");

        expected.add("SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name "
                + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1");

        assertEquals(expected, result);
    }

    /**
     * A test case with 2 columns and 1 aggregator, in this case MAX.
     */
    @Test
    public void testAggrNoGroupBy() {
        String query = "SELECT COUNT(id) FROM role";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();

        expected.add("SELECT COUNT(id) FROM role HAVING COUNT(id) > COUNT(DISTINCT id) AND COUNT(DISTINCT id) > 1");

        assertEquals(expected, result);
    }

    /**
     * A test case with WHERE, JOIN and AGGREGATE parts.
     */
    @Test
    public void testIntegratedWhereJoinAggregate() {
        String query = "SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 > 10 GROUP BY a.id1";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();

        // WHERE RESULTS
        expected.add("SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 = 10 GROUP BY a.id1");
        expected.add("SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 = 11 GROUP BY a.id1");
        expected.add("SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 = 9 GROUP BY a.id1");
        expected.add("SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 IS NULL GROUP BY a.id1");

        // JOIN RESULTS
        expected.add("SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE (a.id1 > 10) GROUP BY a.id1");
        expected.add("SELECT AVG(id) FROM Movies LEFT JOIN a ON Movies.id = a.id1 WHERE ((a.id1 IS NULL) AND"
                + " (Movies.id IS NOT NULL)) AND (a.id1 > 10) GROUP BY a.id1");
        expected.add("SELECT AVG(id) FROM Movies LEFT JOIN a ON Movies.id = a.id1 WHERE ((a.id1 IS NULL) AND"
                + " (Movies.id IS NULL)) AND (a.id1 > 10) GROUP BY a.id1");
        expected.add("SELECT AVG(id) FROM Movies RIGHT JOIN a ON Movies.id = a.id1 WHERE ((Movies.id IS NULL) AND"
                + " (a.id1 IS NOT NULL)) AND (a.id1 > 10) GROUP BY a.id1");
        expected.add("SELECT AVG(id) FROM Movies RIGHT JOIN a ON Movies.id = a.id1 WHERE ((Movies.id IS NULL) AND"
                + " (a.id1 IS NULL)) AND (a.id1 > 10) GROUP BY a.id1");

        // AGGREGATE RESULTS
        expected.add("SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 > 10 GROUP BY a.id1"
                + " HAVING COUNT(*) > 1");
        expected.add("SELECT COUNT(*) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 > 10"
                + " HAVING COUNT(DISTINCT a.id1) > 1");
        expected.add("SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 > 10 GROUP BY a.id1"
                + " HAVING COUNT(id) > COUNT(DISTINCT id) AND COUNT(DISTINCT id) > 1");
        expected.add("SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 > 10 GROUP BY a.id1"
                + " HAVING COUNT(*) > COUNT(id) AND COUNT(DISTINCT id) > 1");


        assertEquals(expected, result);
    }

}
