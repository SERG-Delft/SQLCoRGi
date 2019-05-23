package nl.tudelft.st01.FunctionalTests;

import org.junit.jupiter.api.Test;

import static nl.tudelft.st01.FunctionalTests.AssertUtils.assertEquals;

public class ConditionTest {

    /**
     * A test case for a simple query containing only one condition with < as operator.
     */
    @Test
    public void testLessThanInteger() {
        assertEquals("SELECT * FROM table WHERE a < 100",

                "SELECT * FROM table WHERE a = 99",
                "SELECT * FROM table WHERE a = 100",
                "SELECT * FROM table WHERE a = 101",
                "SELECT * FROM table WHERE a IS NULL");
    }

    /**
     * A test case for a simple query containing only one condition with <= as operator.
     */
    @Test
    public void testLessThanEqualsInteger() {
        assertEquals("SELECT * FROM table WHERE a <= 100",

                "SELECT * FROM table WHERE a = 99",
                "SELECT * FROM table WHERE a = 100",
                "SELECT * FROM table WHERE a = 101",
                "SELECT * FROM table WHERE a IS NULL");
    }

    /**
     * A test simple test case for the > (GreaterThan) operator.
     */
    @Test
    public void testGreaterThanInteger() {
        assertEquals("SELECT * FROM Table WHERE x > 28",

                "SELECT * FROM Table WHERE x = 27",
                "SELECT * FROM Table WHERE x = 28",
                "SELECT * FROM Table WHERE x = 29",
                "SELECT * FROM Table WHERE x IS NULL");
    }

    /**
     * A test simple test case for the > (GreaterThan) operator.
     */
    @Test
    public void testGreaterThanEqualsInteger() {
        assertEquals("SELECT * FROM Table WHERE x >= 37",

                "SELECT * FROM Table WHERE x = 36",
                "SELECT * FROM Table WHERE x = 37",
                "SELECT * FROM Table WHERE x = 38",
                "SELECT * FROM Table WHERE x IS NULL");
    }


    /**
     * A test case for a simple query containing only one condition with != as operator.
     */
    @Test
    public void testNotEqualToFloat() {
        assertEquals("SELECT * FROM table WHERE a <> 0.0",

                "SELECT * FROM table WHERE a = -1.0",
                "SELECT * FROM table WHERE a = 0.0",
                "SELECT * FROM table WHERE a = 1.0",
                "SELECT * FROM table WHERE a IS NULL");
    }

    /**
     * A test case for a simple query testing for string equality.
     */
    @Test
    public void testEqualToString() {
        assertEquals("SELECT * FROM table WHERE a = 'qwerty'",

                "SELECT * FROM table WHERE a = 'qwerty'",
                "SELECT * FROM table WHERE a <> 'qwerty'",
                "SELECT * FROM table WHERE a IS NULL");
    }

    /**
     * A test case for a simple query with IS NULL.
     */
    @Test
    public void testIsNull() {
        assertEquals("SELECT * FROM table WHERE a IS NOT NULL",

                "SELECT * FROM table WHERE a IS NOT NULL",
                "SELECT * FROM table WHERE a IS NULL");
    }

    /**
     * A test case with three conditions, combined with AND and OR.
     */
    @Test
    public void testThreeConditionsAndOr() {
        assertEquals("SELECT * FROM Table1 WHERE a1 = 11 OR a2 = 22 AND a3 = 33",

                "SELECT * FROM Table1 WHERE (a1 = 10) AND NOT (a2 = 22 AND a3 = 33)",
                "SELECT * FROM Table1 WHERE (a1 = 11) AND NOT (a2 = 22 AND a3 = 33)",
                "SELECT * FROM Table1 WHERE (a1 = 12) AND NOT (a2 = 22 AND a3 = 33)",
                "SELECT * FROM Table1 WHERE (a1 IS NULL) AND NOT (a2 = 22 AND a3 = 33)",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 21) AND (a3 = 33))",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 33))",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 23) AND (a3 = 33))",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 IS NULL) AND (a3 = 33))",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 32))",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 34))",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 IS NULL))");
    }

    /**
     * A test case with a BETWEEN condition.
     */
    @Test
    public void testBetweenCondition() {
        assertEquals("SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37",

                "SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * A test case with a negated BETWEEN condition.
     */
    @Test
    public void testBetweenConditionNegated() {
        assertEquals("SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37",

                "SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * A test case with an IN condition.
     */
    @Test
    public void testInCondition() {
        assertEquals("SELECT * FROM Table1 WHERE x IN (30, 38)",

                "SELECT * FROM Table1 WHERE x IN (30, 38)",
                "SELECT * FROM Table1 WHERE x NOT IN (30, 38)",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * A test case with a negated IN condition.
     */
    @Test
    public void testInConditionNegated() {
        assertEquals("SELECT * FROM Table1 WHERE x NOT IN (30, 38)",

                "SELECT * FROM Table1 WHERE x IN (30, 38)",
                "SELECT * FROM Table1 WHERE x NOT IN (30, 38)",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * A test case with an LIKE condition.
     */
    @Test
    public void testLikeCondition() {
        assertEquals("SELECT * FROM Table1 WHERE name LIKE 'John%'",

                "SELECT * FROM Table1 WHERE name LIKE 'John%'",
                // JSQLParser generates "NOT name LIKE" instead of "name NOT LIKE", they are however identical in
                // behavior, therefore we stick with the behavior used in JSQLParser
                "SELECT * FROM Table1 WHERE NOT name LIKE 'John%'",
                "SELECT * FROM Table1 WHERE name IS NULL");
    }

    /**
     * A test case with an LIKE condition.
     */
    @Test
    public void testLikeConditionNegated() {
        assertEquals("SELECT * FROM Table1 WHERE name NOT LIKE 'John%'",

                "SELECT * FROM Table1 WHERE name LIKE 'John%'",
                // JSQLParser generates "NOT name LIKE" instead of "name NOT LIKE", they are however identical in
                // behavior, therefore we stick with the behavior used in JSQLParser
                "SELECT * FROM Table1 WHERE NOT name LIKE 'John%'",
                "SELECT * FROM Table1 WHERE name IS NULL");
    }
}
