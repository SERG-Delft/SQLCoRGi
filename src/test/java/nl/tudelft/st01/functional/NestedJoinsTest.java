package nl.tudelft.st01.functional;

import org.junit.jupiter.api.Test;

import static nl.tudelft.st01.functional.AssertUtils.containsAtLeast;
import static nl.tudelft.st01.functional.AssertUtils.verify;

/**
 * This test class is related to {@link JoinTest}.
 * The functionality of nested joins is tested and evaluated.
 */
public class NestedJoinsTest {

    /**
     * This test evaluates whether the nested joins are transformed to the correct join type.
     */
    @Test
    public void testNestedJoinCorrectJoinConfigurationTwoJoins() {
        verify("SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id",
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
    public void testNestedJoinCorrectJoinConfigurationThreeJoins() {
        verify("SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id",
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
    public void testNestedJoinNullReductionSingleTableOINonOIRColumnIncluded() {
        containsAtLeast("SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id "
                        + "INNER JOIN d on d.id = a.id WHERE c.length > 1 OR b.length > 1 OR a.length > 1 "
                        + "OR d.length > 1",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id INNER JOIN d ON d.id = a.id "
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
                        + "WHERE ((c.id IS NULL) AND (b.id IS NULL)) AND (b.length > 1 OR a.length > 1 OR d.length > 1)",
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
    public void testNestedJoinNullReductionMultiTableOINonOIRColumnIncluded() {
        containsAtLeast("SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id AND c.id = a.id "
                        + "WHERE c.length > 1 OR b.length > 1 OR a.length > 1",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id AND c.id = a.id "
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
    public void testNestedJoinNullReductionSingleTableOIExcludeOIRColumns() {
        containsAtLeast("SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id "
                        + "WHERE c.id > 1 OR b.id > 1 OR a.id > 1",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
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
    public void testNestedJoinOnConditionColumnsFromOneTable1() {
        verify("SELECT * FROM a INNER JOIN b ON a.id > 0 INNER JOIN c ON c.id = a.id",
                "SELECT * FROM a INNER JOIN b ON a.id > 0 INNER JOIN c ON c.id = a.id",
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
    public void testNestedJoinOnConditionColumnsFromOneTable2() {
        verify("SELECT * FROM a INNER JOIN b ON b.id > 0 INNER JOIN c ON c.id = a.id",
                "SELECT * FROM a INNER JOIN b ON b.id > 0 INNER JOIN c ON c.id = a.id",
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
    public void testNestedJoinOnConditionColumnsFromOneTable3() {
        verify("SELECT * FROM a INNER JOIN b ON b.id > 0 INNER JOIN c ON c.id = a.id",
                "SELECT * FROM a INNER JOIN b ON b.id > 0 INNER JOIN c ON c.id = a.id",
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
    public void testNestedJoinOnConditionColumnsFromOneTable4() {
        verify("SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id > 0 INNER JOIN d ON d.id = a.id",
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

    @Test
    public void testNestedJoinOnConditionColumnsFromOneTableMultipleCases() {
        verify("SELECT * FROM a INNER JOIN b ON a.id = b.id INNER JOIN c ON c.id > 0 INNER JOIN d on d.id > 0",
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
}
