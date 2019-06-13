package nl.tudelft.st01.functional;

import org.junit.jupiter.api.Test;

import static nl.tudelft.st01.AssertUtils.containsAtLeast;
import static nl.tudelft.st01.AssertUtils.verify;

/**
 * This class tests if the coverage targets for queries with WHERE clauses are generated correctly.
 *
 * Suppresses the checkstyle multipleStringLiterals violation, because some output sets have queries in common.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
class ConditionTest {

    /**
     * A test case for a simple query containing only one condition with = as operator.
     */
    @Test
    public void testEqualsInteger() {
        verify("SELECT * FROM Movies WHERE year = 2003",

                "SELECT * FROM Movies WHERE year = 2004",
                "SELECT * FROM Movies WHERE year = 2003",
                "SELECT * FROM Movies WHERE year = 2002",
                "SELECT * FROM Movies WHERE year IS NULL");
    }

    /**
     * A test case for a simple query containing only one condition with < as operator.
     */
    @Test
    void testLessThanInteger() {
        verify("SELECT * FROM table WHERE a < 100",

                "SELECT * FROM table WHERE a = 99",
                "SELECT * FROM table WHERE a = 100",
                "SELECT * FROM table WHERE a = 101",
                "SELECT * FROM table WHERE a IS NULL");
    }

    /**
     * A test case for a simple query containing only one condition with <= as operator.
     */
    @Test
    void testLessThanEqualsInteger() {
        verify("SELECT * FROM table WHERE a <= 100",

                "SELECT * FROM table WHERE a = 99",
                "SELECT * FROM table WHERE a = 100",
                "SELECT * FROM table WHERE a = 101",
                "SELECT * FROM table WHERE a IS NULL");
    }

    /**
     * A test simple test case for the > (GreaterThan) operator.
     */
    @Test
    void testGreaterThanInteger() {
        verify("SELECT * FROM Table WHERE x > 28",

                "SELECT * FROM Table WHERE x = 27",
                "SELECT * FROM Table WHERE x = 28",
                "SELECT * FROM Table WHERE x = 29",
                "SELECT * FROM Table WHERE x IS NULL");
    }

    /**
     * A test simple test case for the > (GreaterThan) operator.
     */
    @Test
    void testGreaterThanEqualsInteger() {
        verify("SELECT * FROM Table WHERE x >= 37",

                "SELECT * FROM Table WHERE x = 36",
                "SELECT * FROM Table WHERE x = 37",
                "SELECT * FROM Table WHERE x = 38",
                "SELECT * FROM Table WHERE x IS NULL");
    }

    /**
     * A test case for a simple query containing only one condition with != as operator.
     */
    @Test
    void testNotEqualToFloat() {
        verify("SELECT * FROM table WHERE a <> 0.0",

                "SELECT * FROM table WHERE a = -1.0",
                "SELECT * FROM table WHERE a = 0.0",
                "SELECT * FROM table WHERE a = 1.0",
                "SELECT * FROM table WHERE a IS NULL");
    }

    /**
     * A test case for a simple query testing for string equality.
     */
    @Test
    void testEqualToString() {
        verify("SELECT * FROM table WHERE a = 'qwerty'",

                "SELECT * FROM table WHERE a = 'qwerty'",
                "SELECT * FROM table WHERE a <> 'qwerty'",
                "SELECT * FROM table WHERE a IS NULL");
    }

    /**
     * A test case for a simple query with IS NULL.
     */
    @Test
    public void testIsNull() {
        verify("SELECT * FROM table WHERE a IS NULL",

                "SELECT * FROM table WHERE a IS NOT NULL",
                "SELECT * FROM table WHERE a IS NULL");
    }

    /**
     * A test case for a simple query with IS NOT NULL.
     */
    @Test
    public void testIsNotNull() {
        verify("SELECT * FROM table WHERE a IS NOT NULL",

                "SELECT * FROM table WHERE a IS NULL",
                "SELECT * FROM table WHERE a IS NOT NULL");
    }

    /**
     * A test case with two conditions, combined with AND.
     */
    @Test
    public void testTwoConditionsWithAND() {
        verify("SELECT * FROM Movies WHERE year > 1950 AND year < 2000",

                "SELECT * FROM Movies WHERE (year = 1949) AND (year < 2000)",
                "SELECT * FROM Movies WHERE (year = 1950) AND (year < 2000)",
                "SELECT * FROM Movies WHERE (year = 1951) AND (year < 2000)",
                "SELECT * FROM Movies WHERE (year > 1950) AND (year = 1999)",
                "SELECT * FROM Movies WHERE (year > 1950) AND (year = 2000)",
                "SELECT * FROM Movies WHERE (year > 1950) AND (year = 2001)",
                "SELECT * FROM Movies WHERE (year IS NULL)");
    }

    /**
     * A test case with two conditions, combined with OR.
     */
    @Test
    public void testTwoConditionsWithOR() {
        verify("SELECT * FROM Movies WHERE year < 2004 OR year > 2010",

                "SELECT * FROM Movies WHERE (year = 2003) AND NOT (year > 2010)",
                "SELECT * FROM Movies WHERE (year = 2004) AND NOT (year > 2010)",
                "SELECT * FROM Movies WHERE (year = 2005) AND NOT (year > 2010)",
                "SELECT * FROM Movies WHERE NOT (year < 2004) AND (year = 2009)",
                "SELECT * FROM Movies WHERE NOT (year < 2004) AND (year = 2010)",
                "SELECT * FROM Movies WHERE NOT (year < 2004) AND (year = 2011)",
                "SELECT * FROM Movies WHERE (year IS NULL)");
    }

    /**
     * A test case with three conditions, combined with AND and OR.
     */
    @Test
    void testThreeConditionsAndOr() {
        verify("SELECT * FROM Table1 WHERE a1 = 11 OR a2 = 22 AND a3 = 33",

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
     * A test case with three conditions, combined with AND and OR. Version 2.
     */
    @Test
    public void testThreeConditionsAndOr2() {
        verify("SELECT * FROM Movies WHERE year < 2004 AND length_minutes > 100 OR year > 2005",

                "SELECT * FROM Movies WHERE ((year < 2004) AND (length_minutes = 99)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE ((year < 2004) AND (length_minutes = 100)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE ((year < 2004) AND (length_minutes = 101)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE ((year < 2004) AND (length_minutes IS NULL)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE NOT (year < 2004 AND length_minutes > 100) AND (year = 2004)",
                "SELECT * FROM Movies WHERE NOT (year < 2004 AND length_minutes > 100) AND (year = 2005)",
                "SELECT * FROM Movies WHERE NOT (year < 2004 AND length_minutes > 100) AND (year = 2006)",
                "SELECT * FROM Movies WHERE NOT length_minutes > 100 AND (year IS NULL)",
                "SELECT * FROM Movies WHERE ((year = 2003) AND (length_minutes > 100)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE ((year = 2004) AND (length_minutes > 100)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE ((year = 2005) AND (length_minutes > 100)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE ((year IS NULL) AND (length_minutes > 100))");
    }

    /**
     * A test case with an alias, due to the AS condition.
     */
    @Test
    public void testAliasing() {
        verify("SELECT * FROM Movies AS M WHERE M.id = 8",

                "SELECT * FROM Movies AS M WHERE M.id = 9",
                "SELECT * FROM Movies AS M WHERE M.id = 8",
                "SELECT * FROM Movies AS M WHERE M.id = 7",
                "SELECT * FROM Movies AS M WHERE M.id IS NULL");
    }

    /**
     * A test case with an IN condition.
     */
    @Test
    void testInCondition() {
        verify("SELECT * FROM Table1 WHERE x IN (30, 38)",

                "SELECT * FROM Table1 WHERE x IN (30, 38)",
                "SELECT * FROM Table1 WHERE x NOT IN (30, 38)",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * A test case with a negated IN condition.
     */
    @Test
    void testInConditionNegated() {
        verify("SELECT * FROM Table1 WHERE x NOT IN (30, 38)",

                "SELECT * FROM Table1 WHERE x IN (30, 38)",
                "SELECT * FROM Table1 WHERE x NOT IN (30, 38)",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * A test case with an LIKE condition.
     */
    @Test
    void testLikeCondition() {
        verify("SELECT * FROM Table1 WHERE name LIKE 'John%'",

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
    void testLikeConditionNegated() {
        verify("SELECT * FROM Table1 WHERE name NOT LIKE 'John%'",

                "SELECT * FROM Table1 WHERE name LIKE 'John%'",
                // JSQLParser generates "NOT name LIKE" instead of "name NOT LIKE", they are however identical in
                // behavior, therefore we stick with the behavior used in JSQLParser
                "SELECT * FROM Table1 WHERE NOT name LIKE 'John%'",
                "SELECT * FROM Table1 WHERE name IS NULL");
    }

    /**
     * A test case with a BETWEEN condition containing `long` values.
     */
    @Test
    void testLongBetweenCondition() {
        verify("SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37",

                "SELECT * FROM Table1 WHERE x = 27",
                "SELECT * FROM Table1 WHERE x = 28",
                "SELECT * FROM Table1 WHERE x = 37",
                "SELECT * FROM Table1 WHERE x = 38",
                "SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * A test case with a negated BETWEEN condition containing `long` values.
     */
    @Test
    void testLongBetweenConditionNegated() {
        verify("SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37",

                "SELECT * FROM Table1 WHERE x = 27",
                "SELECT * FROM Table1 WHERE x = 28",
                "SELECT * FROM Table1 WHERE x = 37",
                "SELECT * FROM Table1 WHERE x = 38",
                "SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * A test case with a BETWEEN condition containing `double` values.
     */
    @Test
    void testDoubleBetweenCondition() {
        verify("SELECT * FROM Table1 WHERE x BETWEEN 14.3 AND 32.2",

                "SELECT * FROM Table1 WHERE x = 13.3",
                "SELECT * FROM Table1 WHERE x BETWEEN 14.3 AND 32.2",
                "SELECT * FROM Table1 WHERE x NOT BETWEEN 14.3 AND 32.2",
                "SELECT * FROM Table1 WHERE x = 14.3",
                "SELECT * FROM Table1 WHERE x = 32.2",
                "SELECT * FROM Table1 WHERE x = 33.2",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * A test case with a negated BETWEEN condition containing `double` values.
     */
    @Test
    void testDoubleBetweenConditionNegated() {
        verify("SELECT * FROM Table1 WHERE x NOT BETWEEN 14.3 AND 32.2",

                "SELECT * FROM Table1 WHERE x = 13.3",
                "SELECT * FROM Table1 WHERE x = 14.3",
                "SELECT * FROM Table1 WHERE x = 32.2",
                "SELECT * FROM Table1 WHERE x = 33.2",
                "SELECT * FROM Table1 WHERE x BETWEEN 14.3 AND 32.2",
                "SELECT * FROM Table1 WHERE x NOT BETWEEN 14.3 AND 32.2",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * A test case with a BETWEEN condition containing `String` values.
     */
    @Test
    void testStringBetweenCondition() {
        verify("SELECT * FROM Table1 WHERE x BETWEEN 'hello' AND 'world'",

                "SELECT * FROM Table1 WHERE x NOT BETWEEN 'hello' AND 'world'",
                "SELECT * FROM Table1 WHERE x BETWEEN 'hello' AND 'world'",
                "SELECT * FROM Table1 WHERE x = 'hello'",
                "SELECT * FROM Table1 WHERE x = 'world'",
                "SELECT * FROM Table1 WHERE x IS NULL");
    }

    /**
     * Tests whether the generator does not generate contradicting conditions, e.g. 'a IS NULL AND a = 3'.
     */
    @Test
    void testMultipleConditionsOnSameAttribute() {
        verify("SELECT * FROM t WHERE a > 3 AND a < 20 OR a = -10",

                "SELECT * FROM t WHERE ((a = 2) AND (a < 20)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE ((a = 3) AND (a < 20)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE ((a = 4) AND (a < 20)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE (a IS NULL)",
                "SELECT * FROM t WHERE ((a > 3) AND (a = 19)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE ((a > 3) AND (a = 20)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE ((a > 3) AND (a = 21)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE NOT (a > 3 AND a < 20) AND (a = 9)",
                "SELECT * FROM t WHERE NOT (a > 3 AND a < 20) AND (a = 10)",
                "SELECT * FROM t WHERE NOT (a > 3 AND a < 20) AND (a = 11)");
    }

    /**
     * Tests whether the generator does not generate contradicting conditions, e.g. 'a IS NULL AND a = 3'.
     */
    @Test
    void testMultipleConditionsOnMultipleAttribute() {
        containsAtLeast("SELECT * FROM t WHERE a > 3 AND b < 20 AND a <> 15",

                "SELECT * FROM t WHERE ((a = 2) AND (b < 20)) AND (a <> 15)",
                "SELECT * FROM t WHERE ((a = 3) AND (b < 20)) AND (a <> 15)",
                "SELECT * FROM t WHERE ((a = 4) AND (b < 20)) AND (a <> 15)",
                "SELECT * FROM t WHERE ((a IS NULL) AND (b < 20))",
                "SELECT * FROM t WHERE (a > 3 AND b < 20) AND (a = 14)",
                "SELECT * FROM t WHERE (a > 3 AND b < 20) AND (a = 15)",
                "SELECT * FROM t WHERE (a > 3 AND b < 20) AND (a = 16)",
                "SELECT * FROM t WHERE ((a > 3) AND (b = 19)) AND (a <> 15)",
                "SELECT * FROM t WHERE ((a > 3) AND (b = 20)) AND (a <> 15)",
                "SELECT * FROM t WHERE ((a > 3) AND (b = 21)) AND (a <> 15)",
                "SELECT * FROM t WHERE ((a > 3) AND (b IS NULL)) AND (a <> 15)");
    }
}
