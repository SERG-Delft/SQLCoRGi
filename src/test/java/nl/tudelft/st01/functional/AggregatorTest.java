package nl.tudelft.st01.functional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static nl.tudelft.st01.functional.AssertUtils.verify;

/**
 * This class tests if the coverage targets for queries with aggregators are generated correctly.
 *
 * Suppresses the checkstyle multipleStringLiterals violation, because some output sets have queries in common.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
public class AggregatorTest {

    /**
     * A test case with 2 columns and 1 aggregator, in this case MAX.
     */
    @Test
    public void testCountNoGroupBy() {
        verify("SELECT COUNT(id) FROM role",

                "SELECT COUNT(id) FROM role HAVING COUNT(id) > COUNT(DISTINCT id) AND COUNT(DISTINCT id) > 1");
    }

    /**
     * A test case for the AVG function since it needs an extra rule.
     *
     * @param func Function name to use in tests
     */
    @ParameterizedTest(name = "[{index}] Join type: {0}")
    @CsvSource({"AVG", "SUM", "MIN", "MAX"})
    public void testOtherFunctionsNoGroupBby(String func) {
        verify("SELECT " + func + "(Points) FROM Customers",

                "SELECT " + func + "(Points) FROM Customers HAVING COUNT(Points) > COUNT(DISTINCT Points) "
                + "AND COUNT(DISTINCT Points) > 1",

                "SELECT " + func + "(Points) FROM Customers "
                + "HAVING COUNT(*) > COUNT(Points) AND COUNT(DISTINCT Points) > 1");
    }

    /**
     * A test case with 1 column and 1 aggregator, in this case AVG.
     */
    @Test
    public void testAVGAggregator1column1Aggr() {
        verify("SELECT Director, AVG(Length) FROM Movies GROUP BY Director",

                "SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Director) > 1",
                "SELECT Director, AVG(Length) FROM Movies GROUP BY Director HAVING COUNT(*) > 1",
                "SELECT Director, AVG(Length) FROM Movies GROUP BY Director HAVING COUNT(*) > COUNT(Length) AND "
                        + "COUNT(DISTINCT Length) > 1",
                "SELECT Director, AVG(Length) FROM Movies GROUP BY Director "
                        + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1");
    }

    /**
     * A test case with 1 column and 2 aggregators, in this case AVG and Sum.
     */
    @Test
    public void testSUMAVGAggregator1column2Aggr() {
        verify("SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director",

                "SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Director) > 1",
                "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director HAVING COUNT(*) > 1",
                "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                        + "HAVING COUNT(*) > COUNT(Length) AND COUNT(DISTINCT Length) > 1",
                "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                        + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1",
                "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                        + "HAVING COUNT(*) > COUNT(Score) AND COUNT(DISTINCT Score) > 1",
                "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                        + "HAVING COUNT(Score) > COUNT(DISTINCT Score) AND COUNT(DISTINCT Score) > 1");
    }

    /**
     * A test case with 2 columns and 1 aggregator, in this case MAX.
     */
    @Test
    public void testMAXAggregator2columns1Aggr() {
        verify("SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name",

                "SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Name) > 1",
                "SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name HAVING COUNT(*) > 1",
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
    public void testDuplicateColumsDistinctOperators() {
        verify("SELECT COUNT(Points), AVG(Score), SUM(Score) FROM Customers",

                "SELECT COUNT(Points), AVG(Score), SUM(Score) FROM Customers "
                + "HAVING COUNT(Points) > COUNT(DISTINCT Points) AND COUNT(DISTINCT Points) > 1",

                "SELECT COUNT(Points), AVG(Score), SUM(Score) FROM Customers "
                + "HAVING COUNT(Score) > COUNT(DISTINCT Score) AND COUNT(DISTINCT Score) > 1",

                "SELECT COUNT(Points), AVG(Score), SUM(Score) FROM Customers "
                + "HAVING COUNT(*) > COUNT(Score) AND COUNT(DISTINCT Score) > 1");
    }
}
