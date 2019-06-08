package nl.tudelft.st01.functional;

import nl.tudelft.st01.Generator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static nl.tudelft.st01.functional.AssertUtils.containsAtLeast;
import static nl.tudelft.st01.functional.AssertUtils.verify;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This class tests if the coverage targets for queries with JOINS are generated correctly.
 *
 * Suppresses the checkstyle multipleStringLiterals violation, because some output sets have queries in common.
 */
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "checkstyle:multipleStringLiterals"})
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
    @CsvSource({"INNER, AND", "INNER, OR", "RIGHT, AND", "RIGHT, OR", "LEFT, AND", "LEFT, OR", "FULL, AND", "FULL, OR"})
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
     * @param joinType The type of join that is used in the query.
     */
    @ParameterizedTest(name = "[{index}] Join type: {0}")
    @CsvSource({"INNER", "RIGHT", "LEFT", "FULL"})
    public void testJoinsOnOneEqualityConditionWithNullableColumnsAndWHEREClause(String joinType) {
        verify("SELECT * FROM TableA " + joinType + " JOIN TableB ON TableA.Var = TableB.Var "
                        + "WHERE TableA.Value > 1",

                "SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value = 2",
                "SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value = 1",
                "SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value = 0",
                "SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE TableA.Value IS NULL",
                "SELECT * FROM TableA INNER JOIN TableB ON TableA.Var = TableB.Var WHERE (TableA.Value > 1)",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.Var = TableB.Var WHERE ((TableB.Var IS NULL) "
                    + "AND (TableA.Var IS NOT NULL)) AND (TableA.Value > 1)",
                "SELECT * FROM TableA LEFT JOIN TableB ON TableA.Var = TableB.Var WHERE ((TableB.Var IS NULL) "
                    + "AND (TableA.Var IS NULL)) AND (TableA.Value > 1)",
                "SELECT * FROM TableA RIGHT JOIN TableB ON TableA.Var = TableB.Var WHERE (TableA.Var IS NULL) "
                    + "AND (TableB.Var IS NOT NULL)",
                "SELECT * FROM TableA RIGHT JOIN TableB ON TableA.Var = TableB.Var WHERE (TableA.Var IS NULL) "
                    + "AND (TableB.Var IS NULL)");
    }

    /**
     * A test for evaluating whether the where expressions are indeed not included in
     * the mutation when they should not be. That is, when either side is evaluated to null.
     * @param where The where expression that should not be appended to the LEFT or RIGHT joins.
     */
    @ParameterizedTest
    @CsvSource({"a.id < b.id", "a.id <> b.id", "b.length = a.length",
            "a.id IS NULL", "b.length IS NULL", "a.size BETWEEN 50.0 AND b.length"})
    public void testJoinWithWhereColumnsExcludedIfSideIsNull(String where) {
        containsAtLeast("SELECT * FROM a INNER JOIN b ON a.id = b.id OR a.length < b.length WHERE " + where,

                "SELECT * FROM a INNER JOIN b ON a.id = b.id OR a.length < b.length WHERE (" + where + ")",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id OR a.length < b.length "
                    + "WHERE (b.id IS NULL) AND (b.length IS NULL) AND (a.id IS NULL) AND (a.length IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id OR a.length < b.length "
                    + "WHERE (b.id IS NULL) AND (b.length IS NULL) "
                    + "AND (a.id IS NOT NULL) AND (a.length IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id OR a.length < b.length "
                    + "WHERE (a.id IS NULL) AND (a.length IS NULL) AND (b.id IS NOT NULL) "
                    + "AND (b.length IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id OR a.length < b.length "
                    + "WHERE (a.id IS NULL) AND (a.length IS NULL) AND (b.id IS NULL) AND (b.length IS NULL)"
        );
    }

    /**
     * A test for evaluation whether the given logical expression in the where clause is correctly modified
     * and appended to the correct join type.
     */
    @Test
    public void testJoinWithWhereLogicalToUnary() {
        containsAtLeast(
            "SELECT * FROM a INNER JOIN b ON b.id = a.id WHERE a.length < 60 AND b.id > 40",

            "SELECT * FROM a LEFT JOIN b ON b.id = a.id "
                + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.length < 60)",
            "SELECT * FROM a LEFT JOIN b ON b.id = a.id WHERE ((b.id IS NULL) AND (a.id IS NULL)) AND (a.length < 60)",
            "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE ((a.id IS NULL) AND (b.id IS NOT NULL)) AND (b.id > 40)",
            "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE (a.id IS NULL) AND (b.id IS NULL)",
            "SELECT * FROM a INNER JOIN b ON b.id = a.id WHERE (a.length = 61) AND (b.id > 40)"
        );
    }


    /**
     * A test for evaluating whether LIKE expressions are handled correctly.
     */
    @Test
    public void testJoinWithWhereLike() {
        containsAtLeast(
                "SELECT * FROM a INNER JOIN b ON b.id = a.id WHERE a.name LIKE 'a%'",

                "SELECT * FROM a INNER JOIN b ON b.id = a.id WHERE (a.name LIKE 'a%')",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.name LIKE 'a%')",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NULL)) AND (a.name LIKE 'a%')",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE (a.id IS NULL) AND (b.id IS NULL)"
        );
    }

    /**
     * A test for evaluating whether a redundant IS NOT NULL expression is included even though its table's id is not.
     */
    @Test
    public void testJoinWithWhereContainsIsNullOfNonExcludedColumn() {
        containsAtLeast(
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE (a.length IS NOT NULL)",

                "SELECT * FROM a INNER JOIN b ON b.id = a.id WHERE (a.length IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.length IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NULL)) AND (a.length IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE (a.id IS NULL) AND (b.id IS NULL)"
        );
    }

    /**
     * A test for evaluating whether IN expressions are handled correctly.
     */
    @Test
    public void testJoinWithWhereIn() {
        containsAtLeast(
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE b.length IN (1, 3, 4)",

                "SELECT * FROM a LEFT JOIN b ON b.id = a.id WHERE (b.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id WHERE (b.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id "
                    + "WHERE ((a.id IS NULL) AND (b.id IS NOT NULL)) AND (b.length IN (1, 3, 4))",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id "
                    + "WHERE ((a.id IS NULL) AND (b.id IS NULL)) AND (b.length IN (1, 3, 4))"
        );
    }

    /**
     * A test for evaluating whether the logical expression is not modified when it should not be.
     */
    @Test
    public void testJoinWithUnaffectedWhereLogical() {
        containsAtLeast(
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE a.id = 10 AND a.length = 30",

                "SELECT * FROM a INNER JOIN b ON b.id = a.id WHERE (a.id = 10) AND (a.length = 30)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.id = 10 AND a.length = 30)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NULL)) AND (a.length = 30)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE (a.id IS NULL) AND (b.id IS NULL)"
        );
    }

    /**
     * A test for evaluating whether BETWEEN expression are not modified when they should not be.
     */
    @Test
    public void testJoinWithUnaffectedWhereBetween() {
        containsAtLeast(
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE a.length BETWEEN 10 AND 40",

                "SELECT * FROM a INNER JOIN b ON b.id = a.id WHERE (a.length BETWEEN 10 AND 40)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.length BETWEEN 10 AND 40)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NULL)) AND (a.length BETWEEN 10 AND 40)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id WHERE (a.id IS NULL) AND (b.id IS NULL)"
        );
    }

    /**
     * A test for verifying that no targets are generated for queries with a simple join.
     */
    @Test
    public void testJoinNoOnConditionSimpleJoin() {
        verify("SELECT * FROM a, b");
    }

    /**
     * A test for verifying that no targets are generated and an exception is thrown for queries with a simple join,
     * with an additional where and having clause.
     */
    @Test
    public void testJoinNoOnConditionSimpleJoinWithWhereClauseException() {
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
            () -> Generator.generateRules("SELECT * FROM a, b WHERE a.id = b.id HAVING a.length > 50")
        );
    }


    /**
     * Tests whether the {@code JoinWhereExpressionGenerator} supports {@code ON} clauses that contain more than two
     * different columns from the same table.
     */
    @Test
    void testJoinOnMultipleColumnsSameTable() {
        containsAtLeast(
                "SELECT acl_roles.* FROM acl_roles INNER JOIN acl_roles_users ON acl_roles_users.user_id = '1' "
                    + "AND acl_roles_users.role_id = acl_roles.id AND acl_roles_users.deleted ='0'WHERE acl_roles"
                    + ".deleted='0'",

                "SELECT acl_roles.* FROM acl_roles INNER JOIN acl_roles_users ON acl_roles_users.user_id = '1'"
                    + " AND acl_roles_users.role_id = acl_roles.id AND acl_roles_users.deleted = '0' WHERE "
                    + "acl_roles.deleted = '0'",
                "SELECT acl_roles.* FROM acl_roles INNER JOIN acl_roles_users ON acl_roles_users.user_id = '1' AND "
                    + "acl_roles_users.role_id = acl_roles.id AND acl_roles_users.deleted = '0' WHERE acl_roles"
                    + ".deleted IS NULL",
                "SELECT acl_roles.* FROM acl_roles INNER JOIN acl_roles_users ON acl_roles_users.user_id = '1' AND "
                    + "acl_roles_users.role_id = acl_roles.id AND acl_roles_users.deleted = '0' WHERE acl_roles"
                    + ".deleted <> '0'",
                "SELECT acl_roles.* FROM acl_roles LEFT JOIN acl_roles_users ON acl_roles_users.user_id = '1' AND "
                    + "acl_roles_users.role_id = acl_roles.id AND acl_roles_users.deleted = '0' WHERE ("
                    + "(acl_roles_users.user_id IS NULL) AND (acl_roles_users.role_id IS NULL) AND "
                    + "(acl_roles_users.deleted IS NULL) AND (acl_roles.id IS NOT NULL)) AND (acl_roles.deleted ="
                    + " '0')",
                "SELECT acl_roles.* FROM acl_roles LEFT JOIN acl_roles_users ON acl_roles_users.user_id = '1' AND "
                    + "acl_roles_users.role_id = acl_roles.id AND acl_roles_users.deleted = '0' WHERE ("
                    + "(acl_roles_users.user_id IS NULL) AND (acl_roles_users.role_id IS NULL) AND "
                    + "(acl_roles_users.deleted IS NULL) AND (acl_roles.id IS NULL)) AND (acl_roles.deleted = '0')",
                "SELECT acl_roles.* FROM acl_roles RIGHT JOIN acl_roles_users ON acl_roles_users.user_id = '1' AND "
                    + "acl_roles_users.role_id = acl_roles.id AND acl_roles_users.deleted = '0' WHERE (acl_roles"
                    + ".id IS NULL) AND (acl_roles_users.user_id IS NULL) AND (acl_roles_users.role_id IS NULL) "
                    + "AND (acl_roles_users.deleted IS NULL)",
                "SELECT acl_roles.* FROM acl_roles RIGHT JOIN acl_roles_users ON acl_roles_users.user_id = '1' AND "
                    + "acl_roles_users.role_id = acl_roles.id AND acl_roles_users.deleted = '0' WHERE (acl_roles"
                    + ".id IS NULL) AND (acl_roles_users.user_id IS NOT NULL) AND (acl_roles_users.role_id IS NOT"
                    + " NULL) AND (acl_roles_users.deleted IS NOT NULL)"
        );
    }

}
