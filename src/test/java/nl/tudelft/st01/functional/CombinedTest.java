package nl.tudelft.st01.functional;

import org.junit.jupiter.api.Test;

import static nl.tudelft.st01.functional.AssertUtils.verify;

/**
 * This class tests if the coverage targets for complex queries (with joins, conditionals, aggregation functions, etc.)
 * are generated correctly.
 *
 * Suppresses the checkstyle multipleStringLiterals violation, because some output sets have queries in common.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
public class CombinedTest {

    /**
     * A test case with WHERE, JOIN and AGGREGATE parts.
     */
    @Test
    public void testIntegratedWhereJoinAggregate() {
        verify("SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id < 10 GROUP BY b.id",

                // WHERE RESULTS
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id = 9 GROUP BY b.id",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id = 11 GROUP BY b.id",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id = 10 GROUP BY b.id",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id IS NULL GROUP BY b.id",

                // JOIN RESULTS
                "SELECT AVG(b.id) FROM a RIGHT JOIN b ON a.id = b.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NOT NULL) GROUP BY b.id",
                "SELECT AVG(b.id) FROM a RIGHT JOIN b ON a.id = b.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NULL) GROUP BY b.id",
                "SELECT AVG(b.id) FROM a LEFT JOIN b ON a.id = b.id "
                        + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.id < 10) GROUP BY b.id",
                "SELECT AVG(b.id) FROM a LEFT JOIN b ON a.id = b.id "
                        + "WHERE (b.id IS NULL) AND (a.id IS NULL) GROUP BY b.id",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id "
                        + "WHERE (a.id < 10) GROUP BY b.id",

                // AGGREGATE RESULTS
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id < 10 GROUP BY b.id "
                        + "HAVING COUNT(b.id) > COUNT(DISTINCT b.id) AND COUNT(DISTINCT b.id) > 1",
                "SELECT COUNT(*) FROM a INNER JOIN b ON a.id = b.id WHERE a.id < 10 "
                        + "HAVING COUNT(DISTINCT b.id) > 1",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id < 10 GROUP BY b.id "
                        + "HAVING COUNT(*) > COUNT(b.id) AND COUNT(DISTINCT b.id) > 1",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id < 10 GROUP BY b.id "
                        + "HAVING COUNT(*) > 1");
    }
//
//    @Test
//    public void testJoinWithWhereNonIdIncludedInRightIsNullCass() {
//        verify("SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE a.length < 50 OR b.length > 70",
//
//
//
//
//
//        )
//
//    }


    /**
     * Tests the most basic query, for which case no mutations should be generated.
     */
    @Test
    public void testQueryOnlySelectAndFrom() {
        verify("SELECT * FROM TableA");
    }
}
