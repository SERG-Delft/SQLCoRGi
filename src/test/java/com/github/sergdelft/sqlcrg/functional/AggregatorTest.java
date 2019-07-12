package com.github.sergdelft.sqlcrg.functional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.github.sergdelft.sqlcrg.AssertUtils.verify;

/**
 * This class tests if the coverage targets for queries with aggregators are generated correctly.
 *
 * Suppresses the checkstyle multipleStringLiterals violation, because some output sets have queries in common.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
class AggregatorTest {

    /**
     * A test case with the COUNT clause, to ensure the single rule that is required for the
     * count clause is generated (and no more).
     */
    @Test
    void testCountNoGroupBy() {
        verify("SELECT COUNT(id) FROM role",

                "SELECT COUNT(id) FROM role HAVING COUNT(id) > COUNT(DISTINCT id) AND COUNT(DISTINCT id) > 1");
    }

    /**
     * A test case for the COUNT(_) clause. This query doesn't focus on GROUP BY,
     * but specifically on the WHERE clause.
     */
    @Test
    void testCountOnColumnWithWhere() {
        verify("SELECT COUNT(id) FROM Movies WHERE length_minutes < 100",

            "SELECT COUNT(id) FROM Movies WHERE length_minutes = 101",
            "SELECT COUNT(id) FROM Movies WHERE length_minutes = 100",
            "SELECT COUNT(id) FROM Movies WHERE length_minutes = 99",
            "SELECT COUNT(id) FROM Movies WHERE length_minutes IS NULL",
            "SELECT COUNT(id) FROM Movies WHERE length_minutes < 100 "
                + "HAVING COUNT(id) > COUNT(DISTINCT id) AND COUNT(DISTINCT id) > 1");
    }

    /**
     * A test case for the COUNT(*) clause. This query doesn't focus on GROUP BY,
     * but specifically on the WHERE clause.
     */
    @Test
    void testCountAllWithWhere() {
        verify("SELECT COUNT(*) FROM Movies WHERE length_minutes < 100",

            "SELECT COUNT(*) FROM Movies WHERE length_minutes = 101",
            "SELECT COUNT(*) FROM Movies WHERE length_minutes = 100",
            "SELECT COUNT(*) FROM Movies WHERE length_minutes = 99",
            "SELECT COUNT(*) FROM Movies WHERE length_minutes IS NULL");
    }

    /**
     * A test case for the COUNT(*) clause, which has to be handled differently from a normal
     * COUNT(column) case since COUNT(*) does not have any columns in them, thus returning
     * a nullPointer when trying to access them.
     *
     * It also focuses on the GROUP BY clause, as this is often paired with Aggregate functions.
     */
    @Test
    void testCountAllWithGroupBy() {
        verify("SELECT director, COUNT(*) FROM Movies GROUP BY director",

            "SELECT director, COUNT(*) FROM Movies GROUP BY director HAVING COUNT(*) > 1",
            "SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT director) > 1");
    }

    /**
     * A test case for all the aggregators but COUNT since they all need 2 rules.
     * Since there is no Group By statement here, the 2 rules for Group By are not generated.
     *
     * @param func Function name to use in tests
     */
    @ParameterizedTest(name = "[{index}] Aggregate function: {0}")
    @CsvSource({"AVG", "SUM", "MIN", "MAX"})
    void testOtherAggregateFunctionsNoGroupBy(String func) {
        verify("SELECT " + func + "(Points) FROM Customers",

                "SELECT " + func + "(Points) FROM Customers "
                    + "HAVING COUNT(Points) > COUNT(DISTINCT Points) AND COUNT(DISTINCT Points) > 1",
                "SELECT " + func + "(Points) FROM Customers "
                    + "HAVING COUNT(*) > COUNT(Points) AND COUNT(DISTINCT Points) > 1");
    }

    /**
     * A test case with 1 column and 1 aggregator, in this case AVG.
     * In this test all of the 4 rules from the aggregator class are generated.
     */
    @Test
    void testAVGAggregator1column1Aggr() {
        verify("SELECT Director, AVG(Length) FROM Movies GROUP BY Director",

                "SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Director) > 1",
                "SELECT Director, AVG(Length) FROM Movies GROUP BY Director HAVING COUNT(*) > 1",
                "SELECT Director, AVG(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(*) > COUNT(Length) AND COUNT(DISTINCT Length) > 1",
                "SELECT Director, AVG(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1");
    }

    /**
     * A test case with 1 column and 2 aggregators, in this case AVG and Sum.
     * In this test you can clearly see what rules are generated for any aggregator function
     * (2 of them for the GROUP BY, and 2 per Aggregator)
     */
    @Test
    void testSUMAVGAggregator1column2Aggr() {
        verify("SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director",

                // generated for GROUP BY
                "SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Director) > 1",
                "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director HAVING COUNT(*) > 1",
                // generated per column used in an aggregator
                // for Length (used in SUM)
                "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(*) > COUNT(Length) AND COUNT(DISTINCT Length) > 1",
                "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1",
                // for Score (used in AVG)
                "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(*) > COUNT(Score) AND COUNT(DISTINCT Score) > 1",
                "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(Score) > COUNT(DISTINCT Score) AND COUNT(DISTINCT Score) > 1");
    }

    /**
     * A test case with 2 columns and 1 aggregator, in this case MAX.
     * In this case you can clearly see the 2 rules for GROUP BY and the 2 rules for the aggregators.
     */
    @Test
    void testMAXAggregator2columns1Aggr() {
        verify("SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name",

                // Group By
                "SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Name) > 1",
                "SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name HAVING COUNT(*) > 1",
                // Aggregator
                "SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name HAVING COUNT(*) > COUNT(Length) AND "
                    + "COUNT(DISTINCT Length) > 1",
                "SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name "
                    + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1");
    }

    /**
     * A test case with 2 columns and 3 aggregators.
     * This test shows that some results can be combined (AVG and SUM use the same column, so the
     * rules for that one are re-used).
     *
     */
    @Test
    void testDuplicateColumsDistinctOperators() {
        verify("SELECT COUNT(Points), AVG(Score), SUM(Score) FROM Customers",

                "SELECT COUNT(Points), AVG(Score), SUM(Score) FROM Customers "
                    + "HAVING COUNT(Points) > COUNT(DISTINCT Points) AND COUNT(DISTINCT Points) > 1",
                "SELECT COUNT(Points), AVG(Score), SUM(Score) FROM Customers "
                    + "HAVING COUNT(Score) > COUNT(DISTINCT Score) AND COUNT(DISTINCT Score) > 1",
                "SELECT COUNT(Points), AVG(Score), SUM(Score) FROM Customers "
                    + "HAVING COUNT(*) > COUNT(Score) AND COUNT(DISTINCT Score) > 1");
    }

    /**
     * A test case for a simple GROUP BY query.
     * This shows the two rules that are generated for the GROUP BY statement
     */
    @Test
    void testBasicGroupBy() {
        verify("SELECT Director, Name FROM Movies GROUP BY Name",

                "SELECT Director, Name FROM Movies GROUP BY Name HAVING COUNT(*) > 1",
                "SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Name) > 1");
    }

    /**
     * A test case for a GROUP BY query with WHERE clause.
     * Here you see the combination of handling the where clause and the group by in seperate rules.
     */
    @Test
    void testGroupByWithWhere() {
        verify("SELECT Director FROM Movies WHERE title = 'Finding Nemo' GROUP BY Director",

                // Where clause
                "SELECT Director FROM Movies WHERE title <> 'Finding Nemo' GROUP BY Director",
                "SELECT Director FROM Movies WHERE title = 'Finding Nemo' GROUP BY Director",
                "SELECT Director FROM Movies WHERE title IS NULL GROUP BY Director",
                // Group By
                "SELECT Director FROM Movies WHERE title = 'Finding Nemo' GROUP BY Director HAVING COUNT(*) > 1",
                "SELECT COUNT(*) FROM Movies WHERE title = 'Finding Nemo' HAVING COUNT(DISTINCT Director) > 1");
    }

    /**
     * A test case for a HAVING query. Here you see that the rules generated for HAVING
     * are suspiciously similar to the rules generated for the where clause.
     */
    @Test
    void testHaving() {
        verify("SELECT Director FROM Movies GROUP BY Director HAVING Director LIKE 'B%'",

                // HAVING clause
                "SELECT Director FROM Movies GROUP BY Director HAVING Director NOT LIKE 'B%'",
                "SELECT Director FROM Movies GROUP BY Director HAVING Director LIKE 'B%'",
                "SELECT Director FROM Movies GROUP BY Director HAVING Director IS NULL",
                // Group By clause
                "SELECT Director FROM Movies GROUP BY Director HAVING COUNT(*) > 1 AND Director LIKE 'B%'",
                "SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Director) > 1 AND Director LIKE 'B%'");
    }

    /**
     * A test case for a HAVING query with WHERE clause.
     * Here is where it all comes together, and you can see we generate 1 rule that is a copy of the
     * original rule, and then 2 rules specifically per clause. See the comments in the code.
     */
    @Test
    void testHavingWithWhere() {
        verify("SELECT Director FROM Movies WHERE title = 'Finding Nemo' "
                + "GROUP BY Director HAVING Director LIKE 'A%'",

                // Copy Of Original Clause
                "SELECT Director FROM Movies WHERE title = 'Finding Nemo' "
                    + "GROUP BY Director HAVING Director LIKE 'A%'",
                //  WHERE
                "SELECT Director FROM Movies WHERE title <> 'Finding Nemo' "
                    + "GROUP BY Director HAVING Director LIKE 'A%'",
                "SELECT Director FROM Movies WHERE title IS NULL "
                    + "GROUP BY Director HAVING Director LIKE 'A%'",

                // GROUP BY
                "SELECT Director FROM Movies WHERE title = 'Finding Nemo' "
                    + "GROUP BY Director HAVING COUNT(*) > 1 AND Director LIKE 'A%'",
                "SELECT COUNT(*) FROM Movies WHERE title = 'Finding Nemo' "
                    + "HAVING COUNT(DISTINCT Director) > 1 AND Director LIKE 'A%'",
                // HAVING
                "SELECT Director FROM Movies WHERE title = 'Finding Nemo' "
                    + "GROUP BY Director HAVING Director IS NULL",
                "SELECT Director FROM Movies WHERE title = 'Finding Nemo' "
                    + "GROUP BY Director HAVING Director NOT LIKE 'A%'");
    }
}
