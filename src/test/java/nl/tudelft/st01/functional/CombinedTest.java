package nl.tudelft.st01.functional;

import org.junit.jupiter.api.Test;

import static nl.tudelft.st01.functional.AssertUtils.verify;

public class CombinedTest {

    /**
     * Tests the most basic query, for which case no mutations should be generated.
     */
    @Test
    public void testSimpleQueryNoWhere() {
        verify("SELECT * FROM TableA");
    }


    /**
     * A test case with WHERE, JOIN and AGGREGATE parts.
     */
    @Test
    public void testIntegratedWhereJoinAggregate() {
        verify("SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 > 10 GROUP BY a.id1",

                // WHERE RESULTS
                "SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 = 10 GROUP BY a.id1",
                "SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 = 11 GROUP BY a.id1",
                "SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 = 9 GROUP BY a.id1",
                "SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 IS NULL GROUP BY a.id1",

                // JOIN RESULTS
                "SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE (a.id1 > 10) GROUP BY a.id1",
                "SELECT AVG(id) FROM Movies LEFT JOIN a ON Movies.id = a.id1 WHERE ((a.id1 IS NULL) AND"
                        + " (Movies.id IS NOT NULL)) AND (a.id1 > 10) GROUP BY a.id1",
                "SELECT AVG(id) FROM Movies LEFT JOIN a ON Movies.id = a.id1 WHERE ((a.id1 IS NULL) AND"
                        + " (Movies.id IS NULL)) AND (a.id1 > 10) GROUP BY a.id1",
                "SELECT AVG(id) FROM Movies RIGHT JOIN a ON Movies.id = a.id1 WHERE ((Movies.id IS NULL) AND"
                        + " (a.id1 IS NOT NULL)) AND (a.id1 > 10) GROUP BY a.id1",
                "SELECT AVG(id) FROM Movies RIGHT JOIN a ON Movies.id = a.id1 WHERE ((Movies.id IS NULL) AND"
                        + " (a.id1 IS NULL)) AND (a.id1 > 10) GROUP BY a.id1",

                // AGGREGATE RESULTS
                "SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 > 10 GROUP BY a.id1"
                        + " HAVING COUNT(*) > 1",
                "SELECT COUNT(*) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 > 10"
                        + " HAVING COUNT(DISTINCT a.id1) > 1",
                "SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 > 10 GROUP BY a.id1"
                        + " HAVING COUNT(id) > COUNT(DISTINCT id) AND COUNT(DISTINCT id) > 1",
                "SELECT AVG(id) FROM Movies INNER JOIN a ON Movies.id = a.id1 WHERE a.id1 > 10 GROUP BY a.id1"
                        + " HAVING COUNT(*) > COUNT(id) AND COUNT(DISTINCT id) > 1");
    }
}
