package com.github.sergdelft.sqlcorgi.functional;

import org.junit.jupiter.api.Test;

import static com.github.sergdelft.sqlcorgi.AssertUtils.makeSchema;
import static com.github.sergdelft.sqlcorgi.AssertUtils.verify;

/**
 * This class contains tests that check whether the correct coverage rules are generated for queries containing set
 * operators.
 */
// Justification: Tests may use the same queries, either as input or as part of the expected output.
@SuppressWarnings("checkstyle:multipleStringLiterals")
class SetOperationTest {

    /**
     * Checks that coverage rules are generated for both sides of a {@code UNION} set operator.
     */
    @Test
    void testUnion() {
        verify("SELECT * FROM t WHERE b = 'left' UNION SELECT * FROM t WHERE b = 'right'",

                makeSchema(), "SELECT * FROM t WHERE b = 'left'",
                "SELECT * FROM t WHERE NOT (b = 'left')",
                "SELECT * FROM t WHERE b = 'right'",
                "SELECT * FROM t WHERE NOT (b = 'right')",
                "SELECT * FROM t WHERE b IS NULL"
        );
    }

    /**
     * Checks that coverage rules are generated for both sides of an {@code INTERSECT} set operator.
     */
    @Test
    void testIntersect() {
        verify("SELECT * FROM t WHERE b = 'left' INTERSECT SELECT * FROM t WHERE b = 'right'",

                makeSchema(), "SELECT * FROM t WHERE b = 'left'",
                "SELECT * FROM t WHERE NOT (b = 'left')",
                "SELECT * FROM t WHERE b = 'right'",
                "SELECT * FROM t WHERE NOT (b = 'right')",
                "SELECT * FROM t WHERE b IS NULL"
        );
    }

    /**
     * Checks that coverage rules are generated for both sides of a {@code MINUS} set operator.
     */
    @Test
    void testMinus() {
        verify("SELECT * FROM t WHERE a = 'left' MINUS SELECT * FROM t WHERE a = 'right'",

                null, "SELECT * FROM t WHERE a = 'left'",
                "SELECT * FROM t WHERE NOT (a = 'left')",
                "SELECT * FROM t WHERE a = 'right'",
                "SELECT * FROM t WHERE NOT (a = 'right')"
        );
    }

    /**
     * Checks that coverage rules are generated for both sides of an {@code EXCEPT} set operator.
     */
    @Test
    void testExcept() {
        verify("SELECT * FROM t WHERE a = 'left' EXCEPT SELECT * FROM t WHERE a = 'right'",

                null, "SELECT * FROM t WHERE a = 'left'",
                "SELECT * FROM t WHERE NOT (a = 'left')",
                "SELECT * FROM t WHERE a = 'right'",
                "SELECT * FROM t WHERE NOT (a = 'right')"
        );
    }

}
