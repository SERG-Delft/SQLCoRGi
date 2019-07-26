package com.github.sergdelft.sqlcorgi.functional;

import com.github.sergdelft.sqlcorgi.SQLCorgi;
import com.github.sergdelft.sqlcorgi.schema.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.sergdelft.sqlcorgi.AssertUtils.verify;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This class tests if column and table references are correctly resolved.
 *
 * Suppresses the checkstyle multipleStringLiterals violation, because some output sets have queries in common.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
class TableStructureTest {

    private static Schema schema;

    /**
     * Creates a schema with tables 'a', 'b', 'c' and 'd'.
     */
    @BeforeAll
    static void makeSchema() {

        schema = new Schema();

        Table tableT1 = new Table("t1");
        tableT1.addColumn(new Column("a", true, true, Column.DataType.NUM));
        tableT1.addColumn(new Column("b", false, false, Column.DataType.NUM));
        tableT1.addColumn(new Column("c", false, false, Column.DataType.STRING));
        tableT1.addColumn(new Column("d", false, false, Column.DataType.NUM));
        tableT1.addColumn(new Column("amb", false, false, Column.DataType.NUM));
        tableT1.addColumn(new Column("amb", false, false, Column.DataType.NUM));
        schema.addTable(tableT1);

        Table tableT2 = new Table("t2");
        tableT2.addColumn(new Column("a", true, true, Column.DataType.NUM));
        tableT2.addColumn(new Column("b", false, false, Column.DataType.NUM));
        tableT2.addColumn(new Column("c", true, false, Column.DataType.STRING));
        tableT2.addColumn(new Column("x", true, false, Column.DataType.STRING));
        schema.addTable(tableT2);
    }

    /**
     * Tests whether an exception is thrown if a reference is made to a column that cannot be referred to.
     */
    @Test
    void testUnknownColumn() {
        assertThatThrownBy(() -> SQLCorgi.generateRules("SELECT * FROM t1 WHERE x = 1", schema))
                .isInstanceOf(UnknownColumnException.class);
    }

    /**
     * Tests whether a single table can be aliased.
     */
    @Test
    void testAliasSingleTable() {
        verify("SELECT * FROM t1 t3 WHERE t3.a = 1",

                schema,
                "SELECT * FROM t1 t3 WHERE t3.a = 0",
                "SELECT * FROM t1 t3 WHERE t3.a = 1",
                "SELECT * FROM t1 t3 WHERE t3.a = 2",
                "SELECT * FROM t1 t3 WHERE t3.a IS NULL"
        );
    }

    /**
     * Tests whether a single table can no longer be referred to directly if it has an alias.
     */
    @Test
    void testAliasSingleTableShadows() {
        assertThatThrownBy(() -> SQLCorgi.generateRules("SELECT * FROM t1 t3 WHERE t1.a = 1", schema))
                .isInstanceOf(UnknownTableException.class);
    }

    /**
     * Tests whether a single table in parentheses can be aliased.
     */
    @Test
    void testAliasParenthesesTable() {
        verify("SELECT * FROM (t1) t3 WHERE t3.c = 'a'",

                schema,
                "SELECT * FROM (t1) t3 WHERE t3.c = 'a'",
                "SELECT * FROM (t1) t3 WHERE NOT (t3.c = 'a')"
        );
    }

    /**
     * Tests whether a subquery in the FROM clause can be aliased and referred to.
     */
    @Test
    void testAliasSubquery() {
        verify("SELECT * FROM (SELECT * FROM t2) t3 WHERE t3.c = 'a'",

                schema,
                "SELECT * FROM (SELECT * FROM t2) t3 WHERE t3.c = 'a'",
                "SELECT * FROM (SELECT * FROM t2) t3 WHERE NOT (t3.c = 'a')",
                "SELECT * FROM (SELECT * FROM t2) t3 WHERE t3.c IS NULL"
        );
    }

    /**
     * Tests whether a subquery in the FROM clause can be aliased and referred to.
     */
    @Test
    void testDerivedTable() {
        verify("SELECT * FROM (SELECT t1.*, AVG(t2.b) avg FROM t1, t2) t3 WHERE t3.avg + b > 10",

                schema,
                "SELECT t1.*, AVG(t2.b) avg FROM t1, t2"
                        + " HAVING COUNT(t2.b) > COUNT(DISTINCT t2.b) AND COUNT(DISTINCT t2.b) > 1",
                "SELECT t1.*, AVG(t2.b) avg FROM t1, t2 HAVING COUNT(*) > COUNT(t2.b) AND COUNT(DISTINCT t2.b) > 1",
                "SELECT * FROM (SELECT t1.*, AVG(t2.b) avg FROM t1, t2) t3 WHERE t3.avg + b = 9",
                "SELECT * FROM (SELECT t1.*, AVG(t2.b) avg FROM t1, t2) t3 WHERE t3.avg + b = 10",
                "SELECT * FROM (SELECT t1.*, AVG(t2.b) avg FROM t1, t2) t3 WHERE t3.avg + b = 11"
        );
    }

    /**
     * Tests whether a UNION of subqueries in the FROM clause can be aliased and referred to.
     */
    @Test
    void testDerivedTableUnion() {
        verify("SELECT * FROM (SELECT 1 AS a FROM t1 UNION SELECT 2 AS a FROM t1) num WHERE num.a = 2",

                schema,
                "SELECT * FROM (SELECT 1 AS a FROM t1 UNION SELECT 2 AS a FROM t1) num WHERE num.a = 1",
                "SELECT * FROM (SELECT 1 AS a FROM t1 UNION SELECT 2 AS a FROM t1) num WHERE num.a = 2",
                "SELECT * FROM (SELECT 1 AS a FROM t1 UNION SELECT 2 AS a FROM t1) num WHERE num.a = 3"
        );
    }

    /**
     * Tests whether a JOIN in the FROM clause can be aliased and referred to.
     */
    @Test
    void testAliasSimpleJoin() {
        verify("SELECT * FROM (t1 INNER JOIN t2) t3 WHERE t3.d = 'a'",

                // NOTE: there are two spaces between the join and the alias, due to JSQLParser's toString methods.
                schema,
                "SELECT * FROM (t1 INNER JOIN t2)  t3 WHERE t3.d = 'a'",
                "SELECT * FROM (t1 INNER JOIN t2)  t3 WHERE NOT (t3.d = 'a')"
        );
    }

    /**
     * Tests whether a JOIN in the FROM clause can be aliased and prevents the joined tables from being referred to.
     */
    @Test
    void testAliasSimpleJoinShadow() {
        assertThatThrownBy(() -> SQLCorgi.generateRules("SELECT * FROM (t1, t2) t3 WHERE t1.c = 'a'", schema))
                .isInstanceOf(UnknownTableException.class);
    }

    /**
     * Tests whether a JOIN in the FROM clause can be aliased and throws an exception if an ambiguous column
     * reference exists.
     */
    @Test
    void testAliasSimpleJoinAmbiguousColumn() {
        assertThatThrownBy(() -> SQLCorgi.generateRules("SELECT * FROM (t1, t2) t3 WHERE t3.c = 'a'", schema))
                .isInstanceOf(AmbiguousColumnException.class);
    }

    /**
     * Tests whether an exception is thrown if an ambiguous column reference is made to a column appearing in
     * different tables.
     */
    @Test
    void testSimpleJoinAmbiguousColumnDifferentTables() {
        assertThatThrownBy(() -> SQLCorgi.generateRules("SELECT * FROM t1 FULL JOIN t2 ON 1 = 1 WHERE a = 0", schema))
                .isInstanceOf(AmbiguousColumnException.class);
    }

    /**
     * Tests whether an exception is thrown if an ambiguous column reference is made to a column appearing in
     * different tables.
     */
    @Test
    void testSimpleJoinAmbiguousColumnSingleTable() {
        assertThatThrownBy(() -> SQLCorgi.generateRules("SELECT * FROM t1 WHERE amb = 0", schema))
                .isInstanceOf(AmbiguousColumnException.class);
    }

    /**
     * Tests whether a subquery in the WHERE clause can refer to columns of the main query.
     */
    @Test
    void testSubqueryUsesContext() {
        verify("SELECT * FROM t1 WHERE a > ANY (SELECT a FROM t2 WHERE d = x)",

                schema,
                "SELECT * FROM t1 WHERE a > ANY (SELECT a FROM t2 WHERE d = x)",
                "SELECT * FROM t1 WHERE NOT (a > ANY (SELECT a FROM t2 WHERE d = x))",
                "SELECT * FROM t1 WHERE a IS NULL",
                "SELECT * FROM t1 WHERE EXISTS (SELECT a FROM t2 WHERE d = x)",
                "SELECT * FROM t1 WHERE EXISTS (SELECT a FROM t2 WHERE NOT (d = x))",
                "SELECT * FROM t1 WHERE EXISTS (SELECT a FROM t2 WHERE x IS NULL)"
        );
    }

    /**
     * Tests whether a subquery in the WHERE clause can unambiguously refer to its own columns, even if they have the
     * same names as columns in the main query.
     */
    @Test
    void testSubqueryShadowsContext() {
        verify("SELECT * FROM t1 WHERE EXISTS (SELECT * FROM t2 WHERE a > b)",

                schema,
                "SELECT * FROM t1 WHERE EXISTS (SELECT * FROM t2 WHERE a > b)",
                "SELECT * FROM t1 WHERE NOT EXISTS (SELECT * FROM t2 WHERE a > b)",
                "SELECT * FROM t1 WHERE EXISTS (SELECT * FROM t2 WHERE a = b - 1)",
                "SELECT * FROM t1 WHERE EXISTS (SELECT * FROM t2 WHERE a = b)",
                "SELECT * FROM t1 WHERE EXISTS (SELECT * FROM t2 WHERE a = b + 1)",
                "SELECT * FROM t1 WHERE EXISTS (SELECT * FROM t2 WHERE a IS NULL)"
        );
    }

}
