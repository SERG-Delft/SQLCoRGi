package nl.tudelft.st01;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains tests for {@link Generator}.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
public class GeneratorTest {

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
        String query = "SELECT * FROM Table WHERE x >= 28";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM Table WHERE x = 27");
        expected.add("SELECT * FROM Table WHERE x = 28");
        expected.add("SELECT * FROM Table WHERE x = 29");
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
     * A test case with a BETWEEN condition.
     */
    @Test
    public void testBetweenCondition() {
        String query = "SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37";
        String queryFlipped = "SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37";

        Set<String> result1 = Generator.generateRules(query);
        Set<String> result2 =   Generator.generateRules(queryFlipped);

        Set<String> expected = new TreeSet<>();
        expected.add(query);
        expected.add(queryFlipped);
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
        String query = "SELECT * FROM Table1 WHERE x IN (28, 37)";
        String queryFlipped = "SELECT * FROM Table1 WHERE x NOT IN (28, 37)";

        Set<String> result1 = Generator.generateRules(query);
        Set<String> result2 =   Generator.generateRules(queryFlipped);

        Set<String> expected = new TreeSet<>();
        expected.add(query);
        expected.add(queryFlipped);
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
        String queryFlipped = "SELECT * FROM Table1 WHERE name NOT LIKE 'John%'";

        Set<String> result1 = Generator.generateRules(query);
        Set<String> result2 =   Generator.generateRules(queryFlipped);

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

}
