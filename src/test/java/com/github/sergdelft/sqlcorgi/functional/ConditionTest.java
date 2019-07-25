package com.github.sergdelft.sqlcorgi.functional;

import com.github.sergdelft.sqlcorgi.schema.Column;
import com.github.sergdelft.sqlcorgi.schema.Schema;
import com.github.sergdelft.sqlcorgi.schema.Table;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.github.sergdelft.sqlcorgi.AssertUtils.containsAtLeast;
import static com.github.sergdelft.sqlcorgi.AssertUtils.verify;

/**
 * This class tests if the coverage targets for queries with WHERE clauses are generated correctly.
 *
 * Suppresses the checkstyle multipleStringLiterals violation, because some output sets have queries in common.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
class ConditionTest {

    private Schema makeSchema() {


        ArrayList<Column> moviesColumns = new ArrayList<>();
        moviesColumns.add(new Column("year", true, false, Column.DataType.NUM));

        Table moviesTable = new Table("Movies", moviesColumns);


        ArrayList<Column> tColumns = new ArrayList<>();
        tColumns.add(new Column("a", true, false, Column.DataType.NUM));
        tColumns.add(new Column("b", true, false, Column.DataType.STRING));

        Table tTable = new Table("t", tColumns);

        HashMap<String, Table> tables = new HashMap<>();
        tables.put(moviesTable.getName(), moviesTable);
        tables.put(tTable.getName(), tTable);

        return new Schema(tables);
    }

    /**
     * A test case for a simple query containing only one condition with = as operator.
     */
    @Test
    void testEqualsInteger() {
        verify("SELECT * FROM Movies WHERE year = 2003",

                makeSchema(), "SELECT * FROM Movies WHERE year = 2004",
                "SELECT * FROM Movies WHERE year = 2003",
                "SELECT * FROM Movies WHERE year = 2002",
                "SELECT * FROM Movies WHERE year IS NULL"
        );
    }

    /**
     * A test case for a simple query containing only one condition with < as operator.
     */
    @Test
    void testLessThanInteger() {
        verify("SELECT * FROM Movies WHERE year < 100",

                makeSchema(), "SELECT * FROM Movies WHERE year = 99",
                "SELECT * FROM Movies WHERE year = 100",
                "SELECT * FROM Movies WHERE year = 101",
                "SELECT * FROM Movies WHERE year IS NULL"
        );
    }

    /**
     * A test case for a simple query containing only one condition with <= as operator.
     */
    @Test
    void testLessThanEqualsInteger() {
        verify("SELECT * FROM Movies WHERE year <= 100",

                makeSchema(), "SELECT * FROM Movies WHERE year = 99",
                "SELECT * FROM Movies WHERE year = 100",
                "SELECT * FROM Movies WHERE year = 101",
                "SELECT * FROM Movies WHERE year IS NULL"
        );
    }

    /**
     * A test simple test case for the > (GreaterThan) operator.
     */
    @Test
    void testGreaterThanInteger() {
        verify("SELECT * FROM Movies WHERE year > 28",

                makeSchema(), "SELECT * FROM Movies WHERE year = 27",
                "SELECT * FROM Movies WHERE year = 28",
                "SELECT * FROM Movies WHERE year = 29",
                "SELECT * FROM Movies WHERE year IS NULL"
        );
    }

    /**
     * A test simple test case for the > (GreaterThan) operator.
     */
    @Test
    void testGreaterThanEqualsInteger() {
        verify("SELECT * FROM Movies WHERE year >= 37",

                null, "SELECT * FROM Movies WHERE year = 36",
                "SELECT * FROM Movies WHERE year = 37",
                "SELECT * FROM Movies WHERE year = 38"
        );
    }

    /**
     * A test case for a simple query containing only one condition with != as operator.
     */
    @Test
    void testNotEqualToFloat() {
        verify("SELECT * FROM table WHERE a <> 0.0",

                null, "SELECT * FROM table WHERE a = -1.0",
                "SELECT * FROM table WHERE a = 0.0",
                "SELECT * FROM table WHERE a = 1.0"
        );
    }

    /**
     * A test case for a simple query testing for string equality.
     */
    @Test
    void testEqualToString() {
        verify("SELECT * FROM table WHERE a = 'qwerty'",

                null, "SELECT * FROM table WHERE a = 'qwerty'",
                "SELECT * FROM table WHERE NOT (a = 'qwerty')"
        );
    }

    /**
     * A test case for a simple query with IS NULL.
     */
    @Test
    void testIsNull() {
        verify("SELECT * FROM table WHERE a IS NULL",

                null, "SELECT * FROM table WHERE a IS NOT NULL",
                "SELECT * FROM table WHERE a IS NULL"
        );
    }

    /**
     * A test case for a simple query with IS NOT NULL.
     */
    @Test
    void testIsNotNull() {
        verify("SELECT * FROM table WHERE a IS NOT NULL",

                null, "SELECT * FROM table WHERE a IS NULL",
                "SELECT * FROM table WHERE a IS NOT NULL"
        );
    }

    /**
     * A test case with two conditions, combined with AND.
     */
    @Test
    void testTwoConditionsWithAND() {
        verify("SELECT * FROM Movies WHERE year > 1950 AND year < 2000",

                null, "SELECT * FROM Movies WHERE (year = 1949) AND (year < 2000)",
                "SELECT * FROM Movies WHERE (year = 1950) AND (year < 2000)",
                "SELECT * FROM Movies WHERE (year = 1951) AND (year < 2000)",
                "SELECT * FROM Movies WHERE (year > 1950) AND (year = 1999)",
                "SELECT * FROM Movies WHERE (year > 1950) AND (year = 2000)",
                "SELECT * FROM Movies WHERE (year > 1950) AND (year = 2001)"
        );
    }

    /**
     * A test case with two conditions, combined with OR.
     */
    @Test
    void testTwoConditionsWithOR() {
        verify("SELECT * FROM Movies WHERE year < 2004 OR year > 2010",

                null, "SELECT * FROM Movies WHERE (year = 2003) AND NOT (year > 2010)",
                "SELECT * FROM Movies WHERE (year = 2004) AND NOT (year > 2010)",
                "SELECT * FROM Movies WHERE (year = 2005) AND NOT (year > 2010)",
                "SELECT * FROM Movies WHERE NOT (year < 2004) AND (year = 2009)",
                "SELECT * FROM Movies WHERE NOT (year < 2004) AND (year = 2010)",
                "SELECT * FROM Movies WHERE NOT (year < 2004) AND (year = 2011)"
        );
    }

    /**
     * A test case with two parenthesized conditions, combined with OR.
     */
    @Test
    void testTwoParenthesisedConditionsWithOR() {
        verify("SELECT * FROM Movies WHERE (year = 1996) OR (year = 2019)",

                null, "SELECT * FROM Movies WHERE (year = 1995) AND NOT (year = 2019)",
                "SELECT * FROM Movies WHERE (year = 1996) AND NOT (year = 2019)",
                "SELECT * FROM Movies WHERE (year = 1997) AND NOT (year = 2019)",
                "SELECT * FROM Movies WHERE NOT (year = 1996) AND (year = 2018)",
                "SELECT * FROM Movies WHERE NOT (year = 1996) AND (year = 2019)",
                "SELECT * FROM Movies WHERE NOT (year = 1996) AND (year = 2020)"
        );
    }

    /**
     * A test case with three conditions, combined with AND and OR.
     */
    @Test
    void testThreeConditionsAndOr() {
        verify("SELECT * FROM Table1 WHERE a1 = 11 OR a2 = 22 AND a3 = 33",

                null, "SELECT * FROM Table1 WHERE (a1 = 10) AND NOT (a2 = 22 AND a3 = 33)",
                "SELECT * FROM Table1 WHERE (a1 = 11) AND NOT (a2 = 22 AND a3 = 33)",
                "SELECT * FROM Table1 WHERE (a1 = 12) AND NOT (a2 = 22 AND a3 = 33)",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 21) AND (a3 = 33))",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 33))",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 23) AND (a3 = 33))",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 32))",
                "SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 34))"
        );
    }

    /**
     * A test case with three conditions, combined with AND and OR. Version 2.
     */
    @Test
    void testThreeConditionsAndOr2() {
        verify("SELECT * FROM Movies WHERE year < 2004 AND length_minutes > 100 OR year > 2005",

                null, "SELECT * FROM Movies WHERE ((year < 2004) AND (length_minutes = 99)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE ((year < 2004) AND (length_minutes = 100)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE ((year < 2004) AND (length_minutes = 101)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE NOT (year < 2004 AND length_minutes > 100) AND (year = 2004)",
                "SELECT * FROM Movies WHERE NOT (year < 2004 AND length_minutes > 100) AND (year = 2005)",
                "SELECT * FROM Movies WHERE NOT (year < 2004 AND length_minutes > 100) AND (year = 2006)",
                "SELECT * FROM Movies WHERE ((year = 2003) AND (length_minutes > 100)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE ((year = 2004) AND (length_minutes > 100)) AND NOT (year > 2005)",
                "SELECT * FROM Movies WHERE ((year = 2005) AND (length_minutes > 100)) AND NOT (year > 2005)"
        );
    }

    /**
     * A test case with an alias, due to the AS condition.
     */
    @Test
    void testAliasing() {
        verify("SELECT * FROM Movies AS M WHERE M.id = 8",

                null, "SELECT * FROM Movies AS M WHERE M.id = 9",
                "SELECT * FROM Movies AS M WHERE M.id = 8",
                "SELECT * FROM Movies AS M WHERE M.id = 7"
        );
    }

    /**
     * A test case with an IN condition.
     */
    @Test
    void testInCondition() {
        verify("SELECT * FROM Table1 WHERE x IN (30, 38)",

                null, "SELECT * FROM Table1 WHERE x IN (30, 38)",
                "SELECT * FROM Table1 WHERE x NOT IN (30, 38)",
                "SELECT * FROM Table1 WHERE x IS NULL"
        );
    }

    /**
     * A test case with a negated IN condition.
     */
    @Test
    void testInConditionNegated() {
        verify("SELECT * FROM Table1 WHERE x NOT IN (30, 38)",

                null, "SELECT * FROM Table1 WHERE x IN (30, 38)",
                "SELECT * FROM Table1 WHERE x NOT IN (30, 38)",
                "SELECT * FROM Table1 WHERE x IS NULL"
        );
    }

    /**
     * A test case with a LIKE condition.
     */
    @Test
    void testLikeCondition() {
        verify("SELECT * FROM Table1 WHERE name LIKE 'John%'",

                null, "SELECT * FROM Table1 WHERE name LIKE 'John%'",
                // JSQLParser generates "NOT name LIKE" instead of "name NOT LIKE", they are however identical in
                // behavior, therefore we stick with the behavior used in JSQLParser
                "SELECT * FROM Table1 WHERE name NOT LIKE 'John%'",
                "SELECT * FROM Table1 WHERE name IS NULL"
        );
    }

    /**
     * A test case with an ILIKE condition.
     */
    @Test
    void testILikeCondition() {
        verify("SELECT * FROM Table1 WHERE name ILIKE 'john%'",

                null, "SELECT * FROM Table1 WHERE name ILIKE 'john%'",
                "SELECT * FROM Table1 WHERE name NOT ILIKE 'john%'"
        );
    }

    /**
     * A test case with a negated LIKE condition.
     */
    @Test
    void testLikeConditionNegated() {
        verify("SELECT * FROM Table1 WHERE name NOT LIKE 'John%'",

                null, "SELECT * FROM Table1 WHERE name LIKE 'John%'",
                "SELECT * FROM Table1 WHERE name NOT LIKE 'John%'"
        );
    }

    /**
     * A test case with a BETWEEN condition containing `long` values.
     */
    @Test
    void testLongBetweenCondition() {
        verify("SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37",

                null, "SELECT * FROM Table1 WHERE x = 27",
                "SELECT * FROM Table1 WHERE x = 28",
                "SELECT * FROM Table1 WHERE x = 37",
                "SELECT * FROM Table1 WHERE x = 38",
                "SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37"
        );
    }

    /**
     * A test case with a negated BETWEEN condition containing `long` values.
     */
    @Test
    void testLongBetweenConditionNegated() {
        verify("SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37",

                null, "SELECT * FROM Table1 WHERE x = 27",
                "SELECT * FROM Table1 WHERE x = 28",
                "SELECT * FROM Table1 WHERE x = 37",
                "SELECT * FROM Table1 WHERE x = 38",
                "SELECT * FROM Table1 WHERE x BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x NOT BETWEEN 28 AND 37",
                "SELECT * FROM Table1 WHERE x IS NULL"
        );
    }

    /**
     * A test case with a BETWEEN condition containing `double` values.
     */
    @Test
    void testDoubleBetweenCondition() {
        verify("SELECT * FROM Table1 WHERE x BETWEEN 14.3 AND 32.2",

                null, "SELECT * FROM Table1 WHERE x = 13.3",
                "SELECT * FROM Table1 WHERE x BETWEEN 14.3 AND 32.2",
                "SELECT * FROM Table1 WHERE x NOT BETWEEN 14.3 AND 32.2",
                "SELECT * FROM Table1 WHERE x = 14.3",
                "SELECT * FROM Table1 WHERE x = 32.2",
                "SELECT * FROM Table1 WHERE x = 33.2",
                "SELECT * FROM Table1 WHERE x IS NULL"
        );
    }

    /**
     * A test case with a negated BETWEEN condition containing `double` values.
     */
    @Test
    void testDoubleBetweenConditionNegated() {
        verify("SELECT * FROM Table1 WHERE x NOT BETWEEN 14.3 AND 32.2",

                null, "SELECT * FROM Table1 WHERE x = 13.3",
                "SELECT * FROM Table1 WHERE x = 14.3",
                "SELECT * FROM Table1 WHERE x = 32.2",
                "SELECT * FROM Table1 WHERE x = 33.2",
                "SELECT * FROM Table1 WHERE x BETWEEN 14.3 AND 32.2",
                "SELECT * FROM Table1 WHERE x NOT BETWEEN 14.3 AND 32.2",
                "SELECT * FROM Table1 WHERE x IS NULL"
        );
    }

    /**
     * A test case with a BETWEEN condition containing `String` values.
     */
    @Test
    void testStringBetweenCondition() {
        verify("SELECT * FROM t WHERE b BETWEEN 'hello' AND 'world'",

                makeSchema(), "SELECT * FROM t WHERE b NOT BETWEEN 'hello' AND 'world'",
                "SELECT * FROM t WHERE b BETWEEN 'hello' AND 'world'",
                "SELECT * FROM t WHERE b = 'hello'",
                "SELECT * FROM t WHERE b = 'world'",
                "SELECT * FROM t WHERE b IS NULL"
        );
    }

    /**
     * Tests whether the generator does not generate contradicting conditions, e.g. 'a IS NULL AND a = 3'.
     */
    @Test
    void testMultipleConditionsOnSameAttribute() {
        verify("SELECT * FROM t WHERE a > 3 AND a < 20 OR a = -10",

                makeSchema(), "SELECT * FROM t WHERE ((a = 2) AND (a < 20)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE ((a = 3) AND (a < 20)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE ((a = 4) AND (a < 20)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE (a IS NULL)",
                "SELECT * FROM t WHERE ((a > 3) AND (a = 19)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE ((a > 3) AND (a = 20)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE ((a > 3) AND (a = 21)) AND NOT (a = -10)",
                "SELECT * FROM t WHERE NOT (a > 3 AND a < 20) AND (a = -9)",
                "SELECT * FROM t WHERE NOT (a > 3 AND a < 20) AND (a = -10)",
                "SELECT * FROM t WHERE NOT (a > 3 AND a < 20) AND (a = -11)"
        );
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
                "SELECT * FROM t WHERE ((a > 3) AND (b IS NULL)) AND (a <> 15)"
        );
    }

    /**
     * This test verifies whether the binary expression is converted to an off by one addition and subtraction.
     */
    @Test
    void testNumericBinaryExpressionToAdditionAndSubtraction() {
        verify("SELECT * FROM t1 WHERE t1.c1 = t1.c2 - 10",

                null, "SELECT * FROM t1 WHERE t1.c1 = t1.c2 - 10",
                "SELECT * FROM t1 WHERE t1.c1 = t1.c2 - 10 + 1",
                "SELECT * FROM t1 WHERE t1.c1 = t1.c2 - 10 - 1",
                "SELECT * FROM t1 WHERE t1.c2 IS NULL",
                "SELECT * FROM t1 WHERE t1.c1 IS NULL"
        );
    }

    /**
     * This test verifies whether the binary expression is converted to an off by addition and subtraction on the
     * right side only.
     */
    @Test
    void testNumericBinaryExpressionRightSideConvertedOnly() {
        verify("SELECT * FROM t1 WHERE t1.c1 + 7 > t1.c2 - 8",

                null, "SELECT * FROM t1 WHERE t1.c1 + 7 = t1.c2 - 8",
                "SELECT * FROM t1 WHERE t1.c1 + 7 = t1.c2 - 8 + 1",
                "SELECT * FROM t1 WHERE t1.c1 + 7 = t1.c2 - 8 - 1",
                "SELECT * FROM t1 WHERE t1.c2 IS NULL",
                "SELECT * FROM t1 WHERE t1.c1 IS NULL"
        );
    }

    /**
     * This test verifies whether the signed expression is converted correctly such that one is added and subtracted
     * in the generated rules. The columns in the signed expression must also be checked for null.
     */
    @Test
    void testSignedBinaryExpressionToAdditionAndSubtraction() {
        verify("SELECT * FROM t1 WHERE t1.c1 = -(5 + t1.c2)",

                null, "SELECT * FROM t1 WHERE t1.c1 = -(5 + t1.c2) + 1",
                "SELECT * FROM t1 WHERE t1.c1 = -(5 + t1.c2) - 1",
                "SELECT * FROM t1 WHERE t1.c1 = -(5 + t1.c2)",
                "SELECT * FROM t1 WHERE t1.c2 IS NULL",
                "SELECT * FROM t1 WHERE t1.c1 IS NULL"
        );
    }
}
