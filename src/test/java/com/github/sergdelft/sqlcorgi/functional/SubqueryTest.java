package com.github.sergdelft.sqlcorgi.functional;

import org.junit.jupiter.api.Test;

import static com.github.sergdelft.sqlcorgi.AssertUtils.containsAtLeast;

/**
 * This class contains tests that check whether the correct coverage rules are generated for queries that contain
 * subqueries.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
class SubqueryTest {

    /**
     * Checks that coverage rules are generated for subqueries that are used as FROM item.
     */
    @Test
    void testSubqueryAsFromItem() {
        containsAtLeast("SELECT * FROM (SELECT a FROM t WHERE a > 0) AS t2 WHERE t2.a > 2",

                "SELECT a FROM t WHERE a = -1",
                "SELECT a FROM t WHERE a = 0",
                "SELECT a FROM t WHERE a = 1",
                "SELECT a FROM t WHERE a IS NULL"
        );
    }

    /**
     * Checks that coverage rules are generated for subqueries found in JOINs.
     */
    @Test
    void testSubqueryInJoins() {
        containsAtLeast("SELECT * FROM t JOIN (SELECT a FROM t WHERE a = 'a') AS t2 ON 1 = 1 WHERE t2.a > 2",

                "SELECT a FROM t WHERE a = 'a'",
                "SELECT a FROM t WHERE NOT (a = 'a')",
                "SELECT a FROM t WHERE a IS NULL"
        );
    }

    /**
     * Checks that coverage rules are generated for subqueries found in the WHERE clause.
     */
    @Test
    void testSubqueryInWhere() {
        containsAtLeast("SELECT * FROM t WHERE a IN (SELECT * FROM t WHERE a < 20)",

                "SELECT * FROM t WHERE a IN (SELECT * FROM t WHERE a < 20)",
                "SELECT * FROM t WHERE a NOT IN (SELECT * FROM t WHERE a < 20)",
                "SELECT * FROM t WHERE a IS NULL",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE a = 19)",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE a = 20)",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE a = 21)",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE a IS NULL)"
        );
    }

    /**
     * Checks that coverage rules are generated for subqueries found in the HAVING clause.
     */
    @Test
    void testSubqueryInHaving() {
        containsAtLeast("SELECT * FROM t WHERE a IN (SELECT * FROM t2 WHERE b2 LIKE 'xyz')",

                "SELECT * FROM t WHERE a IN (SELECT * FROM t2 WHERE b2 LIKE 'xyz')",
                "SELECT * FROM t WHERE a NOT IN (SELECT * FROM t2 WHERE b2 LIKE 'xyz')",
                "SELECT * FROM t WHERE a IS NULL",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t2 WHERE b2 LIKE 'xyz')",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t2 WHERE b2 NOT LIKE 'xyz')",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t2 WHERE b2 IS NULL)"
        );
    }

    /**
     * Checks that coverage rules are generated for subqueries found in both the WHERE and HAVING clauses.
     */
    @Test
    void testSameSubqueryInWhereAndHaving() {
        containsAtLeast("SELECT * FROM t WHERE a IN (SELECT * FROM t WHERE b = 'x')"
                        + "HAVING b IN (SELECT * FROM t WHERE b = 'x')",

                "SELECT * FROM t WHERE a IN (SELECT * FROM t WHERE b = 'x')"
                        + " HAVING b IN (SELECT * FROM t WHERE b = 'x')",
                "SELECT * FROM t WHERE a NOT IN (SELECT * FROM t WHERE b = 'x')"
                        + " HAVING b IN (SELECT * FROM t WHERE b = 'x')",
                "SELECT * FROM t WHERE a IS NULL"
                        + " HAVING b IN (SELECT * FROM t WHERE b = 'x')",
                "SELECT * FROM t WHERE a IN (SELECT * FROM t WHERE b = 'x')"
                        + " HAVING b NOT IN (SELECT * FROM t WHERE b = 'x')",
                "SELECT * FROM t WHERE a IN (SELECT * FROM t WHERE b = 'x')"
                        + " HAVING b IS NULL",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE b = 'x')",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE NOT (b = 'x'))",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE b IS NULL)",
                "SELECT * FROM t HAVING EXISTS (SELECT * FROM t WHERE b = 'x')",
                "SELECT * FROM t HAVING EXISTS (SELECT * FROM t WHERE NOT (b = 'x'))",
                "SELECT * FROM t HAVING EXISTS (SELECT * FROM t WHERE b IS NULL)"
        );
    }

    /**
     * Checks that coverage rules are generated recursively for nested subqueries found in the WHERE clause.
     */
    @Test
    void testNestedSubqueries() {
        containsAtLeast("SELECT * FROM t WHERE a IN"
                        + "(SELECT a FROM t WHERE b = 'x' OR b IN"
                        + "(SELECT b FROM t WHERE a = '1'))",

                "SELECT * FROM t WHERE a IN"
                        + " (SELECT a FROM t WHERE b = 'x' OR b IN (SELECT b FROM t WHERE a = '1'))",
                "SELECT * FROM t WHERE a NOT IN "
                        + "(SELECT a FROM t WHERE b = 'x' OR b IN (SELECT b FROM t WHERE a = '1'))",
                "SELECT * FROM t WHERE a IS NULL",
                "SELECT * FROM t WHERE EXISTS (SELECT a FROM t WHERE (b = 'x') AND NOT (b IN"
                        + " (SELECT b FROM t WHERE a = '1')))",
                "SELECT * FROM t WHERE EXISTS (SELECT a FROM t WHERE (NOT (b = 'x')) AND NOT (b IN"
                        + " (SELECT b FROM t WHERE a = '1')))",
                "SELECT * FROM t WHERE EXISTS (SELECT a FROM t WHERE (b IS NULL))",
                "SELECT * FROM t WHERE EXISTS (SELECT a FROM t WHERE NOT (b = 'x') AND (b IN"
                        + " (SELECT b FROM t WHERE a = '1')))",
                "SELECT * FROM t WHERE EXISTS (SELECT a FROM t WHERE NOT (b = 'x') AND (b NOT IN"
                        + " (SELECT b FROM t WHERE a = '1')))",
                "SELECT * FROM t WHERE EXISTS"
                        + " (SELECT a FROM t WHERE EXISTS (SELECT b FROM t WHERE a = '1') AND b = 'x')",
                "SELECT * FROM t WHERE EXISTS"
                        + " (SELECT a FROM t WHERE EXISTS (SELECT b FROM t WHERE NOT (a = '1')) AND b = 'x')",
                "SELECT * FROM t WHERE EXISTS"
                        + " (SELECT a FROM t WHERE EXISTS (SELECT b FROM t WHERE a IS NULL) AND b = 'x')"
        );
    }

}
