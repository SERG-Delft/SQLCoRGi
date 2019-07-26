package com.github.sergdelft.sqlcorgi.functional;

import com.github.sergdelft.sqlcorgi.schema.Column;
import com.github.sergdelft.sqlcorgi.schema.Schema;
import com.github.sergdelft.sqlcorgi.schema.Table;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.sergdelft.sqlcorgi.AssertUtils.containsAtLeast;
import static com.github.sergdelft.sqlcorgi.AssertUtils.verify;

/*
    This warning has been suppressed. Even though the tests may generate the same partial results, they should
    not be replaced with variables. The tests include an assert, so the warning is false positive.
 */
/**
 * This test class is related to {@link JoinTest}.
 * The functionality of nested joins is tested and evaluated.
 */
@SuppressWarnings({"checkstyle:multiplestringliterals"})
class NestedJoinsTest {

    private static Schema schema;

    /**
     * Creates a schema with tables 'a', 'b', 'c' and 'd'.
     */
    @BeforeAll
    static void makeSchema() {

        Table tableA = new Table("a");
        tableA.addColumn(new Column("id", true, true, Column.DataType.NUM));
        tableA.addColumn(new Column("length", false, false, Column.DataType.NUM));

        Table tableB = new Table("b");
        tableB.addColumn(new Column("id", true, true, Column.DataType.NUM));
        tableB.addColumn(new Column("length", false, false, Column.DataType.NUM));

        Table tableC = new Table("c");
        tableC.addColumn(new Column("id", true, true, Column.DataType.NUM));
        tableC.addColumn(new Column("length", false, false, Column.DataType.NUM));

        Table tableD = new Table("d");
        tableD.addColumn(new Column("id", true, true, Column.DataType.NUM));
        tableD.addColumn(new Column("length", false, false, Column.DataType.NUM));

        schema = new Schema();
        schema.addTable(tableA);
        schema.addTable(tableB);
        schema.addTable(tableC);
        schema.addTable(tableD);
    }

    /**
     * This test evaluates whether the nested joins are transformed to the correct join type.
     */
    @Test
    void testNestedJoinCorrectJoinConfigurationTwoJoins() {
        verify("SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id",

                schema, "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NULL)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (c.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (c.id IS NULL) AND (b.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (c.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (c.id IS NULL)"
        );
    }

    /**
     * This test evaluates whether the nested joins are transformed to the correct join type.
     */
    @Test
    void testNestedJoinCorrectJoinConfigurationThreeJoins() {
        verify("SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id",

                schema,
                "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id"
                        + " WHERE (d.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id"
                        + " WHERE (d.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id"
                        + " WHERE (c.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id"
                        + " WHERE (c.id IS NULL) AND (b.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id"
                        + " WHERE (b.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id"
                        + " WHERE (b.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id RIGHT JOIN d ON d.id = a.id"
                        + " WHERE (a.id IS NULL) AND (d.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id RIGHT JOIN d ON d.id = a.id"
                        + " WHERE (a.id IS NULL) AND (d.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id"
                        + " WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id"
                        + " WHERE (a.id IS NULL) AND (b.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id"
                        + " WHERE (b.id IS NULL) AND (c.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id"
                        + " WHERE (b.id IS NULL) AND (c.id IS NULL)"
        );
    }

    /**
     * This test verifies whether the correct columns are excluded depending on the join configuration. In case of an
     * INNER JOIN in the configuration, the tables used in said join must be included. Unless the tables from said join
     * use one or more tables used in the inspected join. In that case, the columns from those tables are to be
     * excluded. This test examines the behaviour for including columns not used in the on expression
     * in the where expression, even though these columns' tables key is in the on expression.
     * In this case, the outer increment relations only contain a single table.
     */
    @Test
    void testNestedJoinNullReductionSingleTableOINonOIRColumnIncluded() {
        containsAtLeast("SELECT * FROM a LEFT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id "
                        + "INNER JOIN d on d.id = a.id WHERE c.length > 1 OR b.length > 1 OR a.length > 1 "
                        + "OR d.length > 1",

                schema, "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id INNER JOIN d ON d.id ="
                        + " a.id "
                        + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.length > 1 OR d.length > 1)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id "
                        + "WHERE ((b.id IS NULL) AND (a.id IS NULL)) AND (a.length > 1 OR d.length > 1)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id "
                        + "WHERE ((a.id IS NULL) AND (b.id IS NOT NULL)) AND (c.length > 1 OR b.length > 1)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id "
                        + "WHERE ((a.id IS NULL) AND (b.id IS NULL)) AND (c.length > 1 OR b.length > 1)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id "
                        + "WHERE ((c.id IS NULL) AND (b.id IS NOT NULL)) "
                        + "AND (b.length > 1 OR a.length > 1 OR d.length > 1)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id "
                        + "WHERE ((c.id IS NULL) AND (b.id IS NULL)) "
                        + "AND (b.length > 1 OR a.length > 1 OR d.length > 1)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id "
                        + "WHERE ((b.id IS NULL) AND (c.id IS NOT NULL)) AND (c.length > 1)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id "
                        + "WHERE ((b.id IS NULL) AND (c.id IS NULL)) AND (c.length > 1)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id "
                        + "WHERE ((d.id IS NULL) AND (a.id IS NOT NULL)) "
                        + "AND (c.length > 1 OR b.length > 1 OR a.length > 1)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id LEFT JOIN d ON d.id = a.id "
                        + "WHERE ((d.id IS NULL) AND (a.id IS NULL)) "
                        + "AND (c.length > 1 OR b.length > 1 OR a.length > 1)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id RIGHT JOIN d ON d.id = a.id "
                        + "WHERE ((a.id IS NULL) AND (d.id IS NOT NULL)) AND (d.length > 1)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id RIGHT JOIN d ON d.id = a.id "
                        + "WHERE ((a.id IS NULL) AND (d.id IS NULL)) AND (d.length > 1)"
        );
    }

    /**
     * This test verifies whether the correct columns are excluded depending on the join configuration. In case of an
     * INNER JOIN in the configuration, the tables used in said join must be included. Unless the tables from said join
     * use one or more tables used in the inspected join. In that case, the columns from those tables are to be
     * excluded. This test examines the behaviour for including columns not used in the on expression
     * in the where expression, even though these columns' tables key is in the on expression.
     * In this case, the outer increment relations contain multiple tables.
     */
    @Test
    void testNestedJoinNullReductionMultiTableOINonOIRColumnIncluded() {
        containsAtLeast("SELECT * FROM a LEFT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id AND c.id = a.id "
                        + "WHERE c.length > 1 OR b.length > 1 OR a.length > 1",

                schema, "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id AND c.id = a.id "
                        + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.length > 1)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id AND c.id = a.id "
                        + "WHERE ((b.id IS NULL) AND (a.id IS NULL)) AND (a.length > 1)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id AND c.id = a.id "
                        + "WHERE ((a.id IS NULL) AND (b.id IS NOT NULL)) AND (b.length > 1)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id AND c.id = a.id "
                        + "WHERE ((a.id IS NULL) AND (b.id IS NULL)) AND (b.length > 1)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id AND c.id = a.id "
                        + "WHERE ((c.id IS NULL) AND (a.id IS NOT NULL) AND (b.id IS NOT NULL)) "
                        + "AND (b.length > 1 OR a.length > 1)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id AND c.id = a.id "
                        + "WHERE ((c.id IS NULL) AND (a.id IS NULL) AND (b.id IS NULL)) "
                        + "AND (b.length > 1 OR a.length > 1)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id AND c.id = a.id "
                        + "WHERE ((a.id IS NULL) AND (b.id IS NULL) AND (c.id IS NOT NULL)) AND (c.length > 1)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id AND c.id = a.id "
                        + "WHERE ((a.id IS NULL) AND (b.id IS NULL) AND (c.id IS NULL)) AND (c.length > 1)"
        );
    }

    /**
     * This test verifies whether the correct columns are excluded depending on the join configuration. In case of an
     * INNER JOIN in the configuration, the tables used in said join must be included. Unless the tables from said join
     * use one or more tables used in the inspected join. In that case, the columns from those tables are to be
     * excluded. This test examines the behaviour for excluding columns used in the on expression
     * from the where expression.
     */
    @Test
    void testNestedJoinNullReductionSingleTableOIExcludeOIRColumns() {
        containsAtLeast("SELECT * FROM a INNER JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id "
                        + "WHERE c.id > 1 OR b.id > 1 OR a.id > 1",

                schema, "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.id > 1)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id "
                        + "WHERE ((a.id IS NULL) AND (b.id IS NOT NULL)) AND (c.id > 1 OR b.id > 1)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id "
                        + "WHERE ((a.id IS NULL) AND (b.id IS NULL)) AND (c.id > 1)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE ((c.id IS NULL) AND (b.id IS NOT NULL)) AND (b.id > 1 OR a.id > 1)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE ((c.id IS NULL) AND (b.id IS NULL)) AND (a.id > 1)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id "
                        + "WHERE ((b.id IS NULL) AND (c.id IS NOT NULL)) AND (c.id > 1)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (c.id IS NULL)"
        );
    }

    /**
     * This test verifies whether the correct join configuration is used, even when the on condition only
     * contains columns from one table. Case: only the from item is used in the first on condition.
     */
    @Test
    void testNestedJoinOnConditionColumnsFromOneTable1() {
        verify("SELECT * FROM a RIGHT JOIN b ON a.id > 0 INNER JOIN c ON c.id = a.id",

                schema, "SELECT * FROM a INNER JOIN b ON a.id > 0 INNER JOIN c ON c.id = a.id",
                "SELECT * FROM a INNER JOIN b ON a.id > 0 LEFT JOIN c ON c.id = a.id "
                        + "WHERE (c.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a INNER JOIN b ON a.id > 0 LEFT JOIN c ON c.id = a.id "
                        + "WHERE (c.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON a.id > 0 INNER JOIN c ON c.id = a.id "
                        + "WHERE (NOT (a.id > 0)) AND (a.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON a.id > 0 RIGHT JOIN c ON c.id = a.id "
                        + "WHERE (a.id IS NULL) AND (c.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON a.id > 0 RIGHT JOIN c ON c.id = a.id "
                        + "WHERE (a.id IS NULL) AND (c.id IS NULL)"
        );
    }

    /**
     * This test verifies whether the correct join configuration is used, even when the on condition only
     * contains columns from one table. Case: only the right item is used in the first on condition.
     */
    @Test
    void testNestedJoinOnConditionColumnsFromOneTable2() {
        verify("SELECT * FROM a INNER JOIN b ON b.id > 0 LEFT JOIN c ON c.id = a.id",

                schema, "SELECT * FROM a INNER JOIN b ON b.id > 0 INNER JOIN c ON c.id = a.id",
                "SELECT * FROM a INNER JOIN b ON b.id > 0 LEFT JOIN c ON c.id = a.id "
                        + "WHERE (c.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a INNER JOIN b ON b.id > 0 LEFT JOIN c ON c.id = a.id "
                        + "WHERE (c.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id > 0 RIGHT JOIN c ON c.id = a.id "
                        + "WHERE (a.id IS NULL) AND (c.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id > 0 RIGHT JOIN c ON c.id = a.id "
                        + "WHERE (a.id IS NULL) AND (c.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id > 0 LEFT JOIN c ON c.id = a.id "
                        + "WHERE (NOT (b.id > 0)) AND (b.id IS NOT NULL)"
        );
    }

    /**
     * This test verifies whether the correct join configuration is used, even when the on condition only
     * contains columns from one table. Case: only the right item is used in the second on condition.
     */
    @Test
    void testNestedJoinOnConditionColumnsFromOneTable3() {
        verify("SELECT * FROM a LEFT JOIN b ON b.id > 0 INNER JOIN c ON c.id = a.id",

                schema, "SELECT * FROM a INNER JOIN b ON b.id > 0 INNER JOIN c ON c.id = a.id",
                "SELECT * FROM a INNER JOIN b ON b.id > 0 LEFT JOIN c ON c.id = a.id "
                        + "WHERE (c.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a INNER JOIN b ON b.id > 0 LEFT JOIN c ON c.id = a.id "
                        + "WHERE (c.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id > 0 RIGHT JOIN c ON c.id = a.id "
                        + "WHERE (a.id IS NULL) AND (c.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id > 0 RIGHT JOIN c ON c.id = a.id "
                        + "WHERE (a.id IS NULL) AND (c.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id > 0 LEFT JOIN c ON c.id = a.id "
                        + "WHERE (NOT (b.id > 0)) AND (b.id IS NOT NULL)"
        );
    }

    /**
     * This test verifies whether the correct join configuration is used, even when the on condition only
     * contains columns from one table. Case: only the right item is used in the third on condition.
     */
    @Test
    void testNestedJoinOnConditionColumnsFromOneTable4() {
        verify("SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id > 0 INNER JOIN d ON d.id = a.id",

                schema,
                "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id > 0 INNER JOIN d ON d.id = a.id",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id > 0 LEFT JOIN d ON d.id = a.id "
                        + "WHERE (d.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id > 0 LEFT JOIN d ON d.id = a.id "
                        + "WHERE (d.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id > 0 INNER JOIN d ON d.id = a.id "
                        + "WHERE (b.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id > 0 INNER JOIN d ON d.id = a.id "
                        + "WHERE (b.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id > 0 RIGHT JOIN d ON d.id = a.id "
                        + "WHERE (a.id IS NULL) AND (d.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id > 0 RIGHT JOIN d ON d.id = a.id "
                        + "WHERE (a.id IS NULL) AND (d.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id > 0 LEFT JOIN d ON d.id = a.id "
                        + "WHERE (NOT (c.id > 0)) AND (c.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id LEFT JOIN c ON c.id > 0 LEFT JOIN d ON d.id = a.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id LEFT JOIN c ON c.id > 0 LEFT JOIN d ON d.id = a.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NULL)"
        );
    }

    /**
     * This test verifies whether the correct join configuration is used, even when the on condition only
     * contains columns from one table. Case: multiple on condition with columns from only one table.
     */
    @Test
    void testNestedJoinOnConditionColumnsFromOneTableMultipleCases() {
        verify("SELECT * FROM a INNER JOIN b ON a.id = b.id INNER JOIN c ON c.id > 0 RIGHT JOIN d on d.id > 0",

                schema,
                "SELECT * FROM a INNER JOIN b ON a.id = b.id INNER JOIN c ON c.id > 0 INNER JOIN d ON d.id > 0",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id LEFT JOIN c ON c.id > 0 LEFT JOIN d ON d.id > 0 "
                        + "WHERE (b.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id LEFT JOIN c ON c.id > 0 LEFT JOIN d ON d.id > 0 "
                        + "WHERE (b.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id LEFT JOIN c ON c.id > 0 RIGHT JOIN d ON d.id > 0 "
                        + "WHERE (NOT (d.id > 0)) AND (d.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id RIGHT JOIN c ON c.id > 0 LEFT JOIN d ON d.id > 0 "
                        + "WHERE (NOT (c.id > 0)) AND (c.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id LEFT JOIN c ON c.id > 0 LEFT JOIN d ON d.id > 0 "
                        + "WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id LEFT JOIN c ON c.id > 0 LEFT JOIN d ON d.id > 0 "
                        + "WHERE (a.id IS NULL) AND (b.id IS NULL)"
        );
    }

    /**
     * This test verifies whether the implicit inner joins are converted correctly to inner joins. The where clause
     * should be reduced.
     */
    @Test
    void testNestedJoinWithImplicitInnerJoins() {
        verify("SELECT * FROM a, b, c WHERE a.id = b.id AND c.id = a.id",

                schema, "SELECT * FROM a INNER JOIN c ON c.id = a.id INNER JOIN b ON a.id = b.id",
                "SELECT * FROM a INNER JOIN c ON c.id = a.id LEFT JOIN b ON a.id = b.id "
                        + "WHERE (b.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a INNER JOIN c ON c.id = a.id LEFT JOIN b ON a.id = b.id "
                        + "WHERE (b.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a LEFT JOIN c ON c.id = a.id INNER JOIN b ON a.id = b.id "
                        + "WHERE (c.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN c ON c.id = a.id INNER JOIN b ON a.id = b.id "
                        + "WHERE (c.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a LEFT JOIN c ON c.id = a.id RIGHT JOIN b ON a.id = b.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN c ON c.id = a.id RIGHT JOIN b ON a.id = b.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN c ON c.id = a.id LEFT JOIN b ON a.id = b.id "
                        + "WHERE (a.id IS NULL) AND (c.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN c ON c.id = a.id LEFT JOIN b ON a.id = b.id "
                        + "WHERE (a.id IS NULL) AND (c.id IS NULL)"
        );
    }

    /**
     * This test verifies whether the conversion of simple joins is compatible with non simple joins.
     */
    @Test
    void testNestedJoinWithImplicitInnerJoinAndInnerJoin() {
        verify("SELECT * FROM a, b INNER JOIN c ON c.id = b.id WHERE a.id = b.id",

                schema, "SELECT * FROM a INNER JOIN b ON a.id = b.id INNER JOIN c ON c.id = b.id",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (c.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (c.id IS NULL) AND (b.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id INNER JOIN c ON c.id = b.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id INNER JOIN c ON c.id = b.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id RIGHT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (c.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id RIGHT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (c.id IS NULL)"
        );
    }

    /**
     * This test verifies whether the conversion of simple joins is compatible with non simple joins.
     */
    @Test
    void testNestedJoinWithImplicitInnerJoinAndSimpleJoin() {
        verify("SELECT * FROM a, b, c, d WHERE d.id = b.id",

                schema, "SELECT * FROM a, d INNER JOIN b ON d.id = b.id, c",
                "SELECT * FROM a, d LEFT JOIN b ON d.id = b.id, c WHERE (b.id IS NULL) AND (d.id IS NOT NULL)",
                "SELECT * FROM a, d LEFT JOIN b ON d.id = b.id, c WHERE (b.id IS NULL) AND (d.id IS NULL)",
                "SELECT * FROM a, d RIGHT JOIN b ON d.id = b.id, c WHERE (d.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a, d RIGHT JOIN b ON d.id = b.id, c WHERE (d.id IS NULL) AND (b.id IS NULL)");
    }

    /**
     * This test verifies whether the where expression is reduced correctly.
     */
    @Test
    void testNestedJoinWithImplicitInnerJoinReduceWhereCorrectly() {
        verify("SELECT * FROM a, b, c, d WHERE a.length > 50 AND d.id = a.id",

                schema, "SELECT * FROM a INNER JOIN d ON d.id = a.id, b, c WHERE (a.length > 50)",
                "SELECT * FROM a INNER JOIN d ON d.id = a.id, b, c WHERE a.length = 49",
                "SELECT * FROM a INNER JOIN d ON d.id = a.id, b, c WHERE a.length = 50",
                "SELECT * FROM a INNER JOIN d ON d.id = a.id, b, c WHERE a.length = 51",
                "SELECT * FROM a LEFT JOIN d ON d.id = a.id, b, c "
                        + "WHERE ((d.id IS NULL) AND (a.id IS NOT NULL)) AND (a.length > 50)",
                "SELECT * FROM a LEFT JOIN d ON d.id = a.id, b, c "
                        + "WHERE ((d.id IS NULL) AND (a.id IS NULL)) AND (a.length > 50)",
                "SELECT * FROM a RIGHT JOIN d ON d.id = a.id, b, c WHERE (a.id IS NULL) AND (d.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN d ON d.id = a.id, b, c WHERE (a.id IS NULL) AND (d.id IS NULL)"
        );
    }

}
