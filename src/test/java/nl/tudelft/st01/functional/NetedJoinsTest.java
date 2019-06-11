package nl.tudelft.st01.functional;

import nl.tudelft.st01.Generator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static nl.tudelft.st01.functional.AssertUtils.containsAtLeast;
import static nl.tudelft.st01.functional.AssertUtils.verify;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test class is related to {@link JoinTest}.
 * The functionality of nested joins is tested and evaluated.
 */
public class NetedJoinsTest {

    /**
     * This test evaluates whether the nested joins are transformed to the correct join type.
     */
    @Test
    public void testNestedJoinCorrectJoinConfiguration() {
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
                        + "WHERE (b.id IS NULL) AND (c.id IS NULL)");
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
                        + "WHERE ((a.id IS NULL) AND (d.id IS NULL)) AND (d.length > 1)");
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
                        + "WHERE ((a.id IS NULL) AND (d.id IS NULL)) AND (d.length > 1)");
    }

    @Test
    public void test() {
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




}