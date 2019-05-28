package nl.tudelft.st01.functional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static nl.tudelft.st01.functional.AssertUtils.verify;

/**
 * This class tests if the coverage targets for queries with JOINS are generated correctly.
 *
 * Suppresses the checkstyle multipleStringLiterals violation, because some output sets have queries in common.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
public class JoinTest {

    /**
     * Parametrized test for a simple query with different join types with a single join condition which involves
     * nullable columns. All of which should result in the same expected output set.
     *
     * @param joinType Type of join.
     */
    @ParameterizedTest(name = "[{index}] Join type: {0}")
    @CsvSource({"INNER", "RIGHT", "LEFT", "FULL"})
    public void testJoinsOnOneEqualityConditionWithNullableColumns(String joinType) {
        verify("SELECT * FROM TableA " + joinType + " JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull",

                "SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull "
                        + "WHERE (TableB.CanBeNull IS NULL) AND (TableA.CanBeNull IS NOT NULL)",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull "
                        + "WHERE (TableB.CanBeNull IS NULL) AND (TableA.CanBeNull IS NULL)",
                "SELECT * FROM TableA RIGHT JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull "
                        + "WHERE (TableA.CanBeNull IS NULL) AND (TableB.CanBeNull IS NOT NULL)",
                "SELECT * FROM TableA RIGHT JOIN TableB ON TableA.CanBeNull < TableB.CanBeNull "
                        + "WHERE (TableA.CanBeNull IS NULL) AND (TableB.CanBeNull IS NULL)");
    }

    /**
     * A parametrized test for a  query with different join types with a two disjoint join conditions which involves
     * nullable columns. All of which should result in the same expected output set.
     *
     * @param joinType      Type of join.
     * @param conditionType Type of condition, either AND or OR.
     */
    @ParameterizedTest(name = "[{index}] Join type: {0}, Condition type: {1}")
    @CsvSource({"INNER, AND", "INNER, OR", "RIGHT, AND", "INNER, OR", "LEFT, AND", "LEFT, OR", "FULL, AND", "FULL, OR"})
    public void testJoinsOnTwoDisjointConditionsWithNullableColumns(String joinType, String conditionType) {
        verify("SELECT * FROM TableA " + joinType + " JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull "
                        + conditionType + " TableA.CanBeNull2 = TableB.CanBeNull2",

                "SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull "
                        + conditionType + " TableA.CanBeNull2 = TableB.CanBeNull2",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull "
                        + conditionType + " TableA.CanBeNull2 = TableB.CanBeNull2 WHERE (TableB.CanBeNull IS NULL) AND "
                        + "(TableB.CanBeNull2 IS NULL) AND (TableA.CanBeNull IS NOT NULL) AND "
                        + "(TableA.CanBeNull2 IS NOT NULL)",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull "
                        + conditionType + " TableA.CanBeNull2 = TableB.CanBeNull2 WHERE (TableB.CanBeNull IS NULL) AND "
                        + "(TableB.CanBeNull2 IS NULL) AND (TableA.CanBeNull IS NULL) AND (TableA.CanBeNull2 IS NULL)",
                "SELECT * FROM TableA RIGHT JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull "
                        + conditionType + " TableA.CanBeNull2 = TableB.CanBeNull2 WHERE (TableA.CanBeNull IS NULL) AND "
                        + "(TableA.CanBeNull2 IS NULL) AND (TableB.CanBeNull IS NOT NULL) AND "
                        + "(TableB.CanBeNull2 IS NOT NULL)",
                "SELECT * FROM TableA RIGHT JOIN TableB ON TableA.CanBeNull = TableB.CanBeNull "
                        + conditionType + " TableA.CanBeNull2 = TableB.CanBeNull2 WHERE (TableA.CanBeNull IS NULL) AND "
                        + "(TableA.CanBeNull2 IS NULL) AND (TableB.CanBeNull IS NULL) AND (TableB.CanBeNull2 IS NULL)");
    }

    /**
     * A parametrized test for a  query with different on conditions used in the joins.
     *
     * @param on The on condition in the join.
     */
    @ParameterizedTest(name = "[{index}] On Conditions type: {0}")
    @CsvSource({"<", ">", "=", "<=", ">=", "<>"})
    public void testOnConditionsInJoins(String on) {
        verify("SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull " + on + " TableB.CanBeNull",

                "SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull " + on + " TableB.CanBeNull",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull " + on + " TableB.CanBeNull"
                        + " WHERE (TableB.CanBeNull IS NULL) AND (TableA.CanBeNull IS NOT NULL)",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull " + on + " TableB.CanBeNull"
                        + " WHERE (TableB.CanBeNull IS NULL) AND (TableA.CanBeNull IS NULL)",
                "SELECT * FROM TableA RIGHT JOIN TableB ON TableA.CanBeNull " + on + " TableB.CanBeNull"
                        + " WHERE (TableA.CanBeNull IS NULL) AND (TableB.CanBeNull IS NOT NULL)",
                "SELECT * FROM TableA RIGHT JOIN TableB ON TableA.CanBeNull " + on + " TableB.CanBeNull"
                        + " WHERE (TableA.CanBeNull IS NULL) AND (TableB.CanBeNull IS NULL)");
    }

    /**
     * A test for testing joins with on conditions with columns from only one table with an IS NULL expression.
     * This case, the left one.
     */
    @Test
    public void testJoinOnConditionFromSingleTableLeftNullable() {
        verify("SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull IS NULL",

                "SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull IS NULL",
                "SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull IS NULL WHERE"
                        + " (NOT (TableA.CanBeNull IS NULL)) AND (TableA.CanBeNull IS NOT NULL)");
    }

    /**
     * A test for testing joins with on conditions with columns from only one table with a comparison.
     * This case, the left one.
     */
    @Test
    public void testJoinOnConditionFromSingleTableLeftComparison() {
        verify("SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull > 5",

                "SELECT * FROM TableA INNER JOIN TableB ON TableA.CanBeNull > 5",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.CanBeNull > 5 WHERE (NOT (TableA.CanBeNull > 5))"
                        + " AND (TableA.CanBeNull IS NOT NULL)");
    }

    /**
     * A test for testing joins with on conditions with columns from only one table with an IS NULL expression.
     * This case, the right one.
     */
    @Test
    public void testJoinOnConditionFromSingleTableRightNullable() {
        verify("SELECT * FROM TableA INNER JOIN TableB ON TableB.CanBeNull IS NULL",

                "SELECT * FROM TableA RIGHT JOIN TableB ON TableB.CanBeNull IS NULL WHERE"
                        + " (NOT (TableB.CanBeNull IS NULL)) AND (TableB.CanBeNull IS NOT NULL)");
    }

    /**
     * A test for testing joins with on conditions with columns from only one table with a comparison.
     * This case, the right one.
     */
    @Test
    public void testJoinOnConditionFromSingleTableRightComparison() {
        verify("SELECT * FROM TableA INNER JOIN TableB ON TableB.CanBeNull > 5",

                "SELECT * FROM TableA RIGHT JOIN TableB ON TableB.CanBeNull > 5 WHERE (NOT (TableB.CanBeNull > 5))"
                        + " AND (TableB.CanBeNull IS NOT NULL)");
    }

    /**
     * A parametrized test for a query with different join types with a single join condition which involves
     * nullable columns and a WHERE clause. All of which should result in the same expected output set.
     */

    @ParameterizedTest(name = "[{index}] Join type: {0}")
    @CsvSource({"INNER", "RIGHT", "LEFT", "FULL"})
    public void testJoinsOnOneEqualityConditionWithNullableColumnsAndWHEREClause(String joinType) {
        verify("SELECT * FROM TableA " + joinType + " JOIN TableB ON TableA.Var = TableB.Var " +
                        "WHERE TableA.Value > 1",

                "SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value = 2",
                "SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value = 1",
                "SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value = 0",
                "SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value IS NULL",
                "SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE (TableA.Value > 1)",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.Var = TableB.Var WHERE ((TableB.Var IS NULL) " +
                        "AND (TableA.Var IS NOT NULL)) AND (TableA.Value > 1)",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.Var = TableB.Var WHERE ((TableB.Var IS NULL) " +
                        "AND (TableA.Var IS NULL)) AND (TableA.Value > 1)",
                "SELECT * FROM TableA RIGHT JOIN TableB ON TableA.Var = TableB.Var WHERE (TableA.Var IS NULL) " +
                        "AND (TableB.Var IS NOT NULL)",
                "SELECT * FROM TableA RIGHT JOIN TableB ON TableA.Var = TableB.Var WHERE (TableA.Var IS NULL) " +
                        "AND (TableB.Var IS NULL)");
    }

}
