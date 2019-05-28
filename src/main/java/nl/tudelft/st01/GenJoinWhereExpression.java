package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * This class allows for mutating a given query such that a set of mutated queries is returned.
 */
public class GenJoinWhereExpression {
    private Map<String, List<Column>> map;

    private Expression whereExpression;

    /**
     * Takes in a statement and mutates the joins. Each join will have its own set of mutations added to the results.
     * @param plainSelect The statement for which the joins have to be mutated.
     * @return A set of mutated queries in string format.
     */
    public Set<String> generateJoinWhereExpressions(PlainSelect plainSelect) {
        map = new HashMap<>();
        this.whereExpression = plainSelect.getWhere();
        Set<String> result = new TreeSet<>();

        List<Join> joins = plainSelect.getJoins();
        List<JoinWhereItem> joinWhereItems;
        Join join;

        PlainSelect out = plainSelect;
        Expression whereCondition = plainSelect.getWhere();

        if (!(joins == null || joins.isEmpty())) {
            RuleGeneratorOnExpressionVisitor ruleGeneratorOnExpressionVisitor = new RuleGeneratorOnExpressionVisitor();
            ruleGeneratorOnExpressionVisitor.setOutput(map);

            List<Join> temp = new ArrayList<>();
            for (int i = 0; i < joins.size(); i++) {
                join = joins.get(i);
                join.getOnExpression().accept(ruleGeneratorOnExpressionVisitor);
                joinWhereItems = generateJoinMutations(join);
                for (JoinWhereItem joinWhereItem : joinWhereItems) {
                    temp.addAll(joins);
                    temp.set(i, joinWhereItem.getJoin());

                    out.setJoins(temp);
                    out.setWhere(joinWhereItem.getJoinWhere());

                    result.add(out.toString());
                    out.setWhere(whereCondition);
                    out.setJoins(joins);
                    temp.clear();
                }
                map.clear();
            }
        }

        plainSelect.setWhere(whereCondition);
        plainSelect.setJoins(joins);
        return result;
    }

    /**
     * Mutates the given {@link Join} such that it returns a list of {@link JoinWhereItem}s.
     * @param join The join that should be mutated.
     * @return A list of mutated joins and their corresponding where expressions.
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
    private List<JoinWhereItem> generateJoinMutations(Join join) {
        List<JoinWhereItem> result;

        if (map.size() == 1) {
            result = handleJoinSingleTableOnCondition(join, map);
        } else {
            result = handleJoinMultipleTablesOnCondition(join, map);
        }
        return result;
    }

    /**
     * When the map contains more than one table, then generate to corresponding mutations.
     * @param join The join that has to be mutated.
     * @param map The map where each table is mapped to its columns.
     * @return List of JoinWhere items with the mutated results.
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
    private List<JoinWhereItem> handleJoinMultipleTablesOnCondition(Join join, Map<String, List<Column>> map) {
        List<JoinWhereItem> result = new ArrayList<>();
        List<Column> columns;

        JoinOnConditionColumns joinOnConditionColumns = new JoinOnConditionColumns();

        for (Map.Entry<String, List<Column>> s : map.entrySet()) {
            columns = map.get(s.getKey());

            if (!s.getKey().equals(join.getRightItem().toString().toLowerCase())) {
                joinOnConditionColumns.addToLeftColumns(columns);
            } else {
                joinOnConditionColumns.addToRightColumns(columns);
            }
        }

        result.addAll(generateJoinWhereItems(joinOnConditionColumns, join));

        return result;
    }

    /**
     * Generates the join where items for the join on condition columns provided.
     * @param joinOnConditionColumns The object containing the columns.
     * @param join The join used.
     * @return List of a generated JoinWhereItem for each mutation
     */
    private List<JoinWhereItem> generateJoinWhereItems(JoinOnConditionColumns joinOnConditionColumns, Join join) {
        Expression temp = whereExpression;

        List<JoinWhereItem> result = new ArrayList<>();

        Join leftJoin = createGenericCopyOfJoin(join);
        leftJoin.setLeft(true);
        Join rightJoin = createGenericCopyOfJoin(join);
        rightJoin.setRight(true);
        Join innerJoin = createGenericCopyOfJoin(join);
        innerJoin.setInner(true);

        List<Column> left = joinOnConditionColumns.getLeftColumns();
        Set<String> leftTables = joinOnConditionColumns.getLeftTables();

        List<Column> right = joinOnConditionColumns.getRightColumns();
        Set<String> rightTables = joinOnConditionColumns.getRightTables();

        Expression leftColumnsIsNull = createIsNullExpressions(left, true);
        Expression leftColumnsIsNotNull = createIsNullExpressions(left, false);

        Expression rightColumnsIsNull = createIsNullExpressions(right, true);
        Expression rightColumnsIsNotNull = createIsNullExpressions(right, false);

        //TODO: Need unmodified version of the original expression when mutating for each case.
        Expression rightIsNull = excludeInExpression(right, leftTables, temp);
        Expression rightIsNotNull = excludeInExpression(leftTables, temp);

        Expression leftIsNull = excludeInExpression(left, rightTables, temp);
        Expression leftIsNotNull = excludeInExpression(rightTables, temp);

        Expression leftNullRightNotNull = concatenate(leftColumnsIsNull, rightColumnsIsNotNull, false);
        Expression leftNullRightNull = concatenate(leftColumnsIsNull, rightColumnsIsNull, false);

        Expression rightNullLeftNotNull = concatenate(rightColumnsIsNull, leftColumnsIsNotNull, false);
        Expression rightNullLeftNull = concatenate(rightColumnsIsNull, leftColumnsIsNull, false);

        Expression rightJoinLeftIsNull = concatenate(leftNullRightNull, rightIsNull, true);
        Expression rightJoinLeftIsNotNull = concatenate(leftNullRightNotNull, rightIsNotNull, true);

        Expression leftJoinRightIsNull = concatenate(rightNullLeftNull, leftIsNull, true);
        Expression leftJoinRightIsNotNull = concatenate(rightNullLeftNotNull, leftIsNotNull, true);

        result.add(new JoinWhereItem(innerJoin, wrapInParentheses(whereExpression)));
        result.add(new JoinWhereItem(leftJoin, leftJoinRightIsNull));
        result.add(new JoinWhereItem(leftJoin, leftJoinRightIsNotNull));
        result.add(new JoinWhereItem(rightJoin, rightJoinLeftIsNull));
        result.add(new JoinWhereItem(rightJoin, rightJoinLeftIsNotNull));

        return result;
    }

    /**
     * Returns the concatenation of two expression. If either of them is null, the other expression is wrapped
     * in parentheses.
     * @param left The left expression.
     * @param right The right expression.
     * @param wrapBinary True iff both sides should be wrapped in parentheses in case of a binary expression.
     * @return An and expression if both expressions are not null, otherwise, if either is null, the other is returned.
     */
    private Expression concatenate(Expression left, Expression right, boolean wrapBinary) {
        if (left == null) {
            return right;
        } else if (right != null) {
            if (wrapBinary) {
                return new AndExpression(wrapInParentheses(left), wrapInParentheses(right));
            } else {
                return new AndExpression(left, right);
            }

        } else {
            return left;
        }
    }

    /**
     * Wraps the given expression in parentheses if it is not wrapped yet.
     * @param expression The expression to wrap.
     * @return Expression wrapped in parentheses.
     */
    private Expression wrapInParentheses(Expression expression) {
        if (expression == null) {
            return null;
        }
        if (expression instanceof Parenthesis) {
            return expression;
        } else {
            return new Parenthesis(expression);
        }
    }

    /**
     * Modifies the input expression such that it no longer contains any of the given columns.
     * @param nulls The columns that should be excluded.
     * @param expression The expression that should be modified.
     * @return The modified expression.
     */
    /*
    private static Expression excludeInExpression(List<Column> nulls, Expression expression) {
        Expression filteredWhere;
        ColumnExclusionVisitor ceVisitor = new ColumnExclusionVisitor();
        ceVisitor.setNullColumns(nulls);

        if (expression != null) {


            expression.accept(ceVisitor);
            filteredWhere = ceVisitor.getExpression();
            return filteredWhere;
        }
        return null;

    }
    */

    /**
     * Modifies input expression such that it no longer contains any columns part of the table.
     * @param tables The tables from which the columns have to be excluded.
     * @param expression The expression that should be modified.
     * @return The modified expression.
     */
    private static Expression excludeInExpression(Set<String> tables, Expression expression) {
        Expression filteredWhere;
        if (expression != null) {
            ColumnExclusionVisitor ceVisitor = new ColumnExclusionVisitor();
            ceVisitor.setTables(tables);
            expression.accept(ceVisitor);
            filteredWhere = ceVisitor.getExpression();

            return filteredWhere;
        }

        return null;
    }

    /**
     * Modifies input expression such that it no longer contains any columns part of the table.
     * @param columns The columns that should be excluded.
     * @param tables The table from which the columns should be excluded.
     * @param expression The expression that should be modified.
     * @return The modified expression.
     */
    private static Expression excludeInExpression(List<Column> columns, Set<String> tables, Expression expression) {
        Expression filteredWhere;
        if (expression != null) {
            ColumnExclusionVisitor ceVisitor = new ColumnExclusionVisitor();
            ceVisitor.setTables(tables);
            ceVisitor.setNullColumns(columns);
            expression.accept(ceVisitor);
            filteredWhere = ceVisitor.getExpression();

            return filteredWhere;
        }

        return null;
    }


    /**
     * In case the two tables are joined and the on condition only contains tables from one of the two tables,
     * then this method will generate the JoinWhereItems corresponding to the test cases that should be generated.
     * @param join The join from which the JoinWhereitems have to be generated.
     * @param map The map where each table is mapped to its columns.
     * @return List of JoinWhereItems that contain the mutations.
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
    private static List<JoinWhereItem> handleJoinSingleTableOnCondition(Join join, Map<String, List<Column>> map) {
        if (map.size() > 1) {
            throw new IllegalArgumentException("Map may only contain columns from one table");
        }

        Expression isNotNulls;

        List<JoinWhereItem> result = new ArrayList<>();
        List<Column> columns;

        Join leftJoin = createGenericCopyOfJoin(join);
        leftJoin.setLeft(true);
        Join rightJoin = createGenericCopyOfJoin(join);
        rightJoin.setRight(true);
        Join innerJoin = createGenericCopyOfJoin(join);
        innerJoin.setInner(true);

        Expression coreExpression;
        AndExpression andExpression;
        Parenthesis left;
        NotExpression notExpression;
        Parenthesis parenthesis;

        for (Map.Entry<String, List<Column>> s : map.entrySet()) {
            columns = map.get(s.getKey());

            isNotNulls = createIsNullExpressions(columns, false);
            parenthesis = new Parenthesis();
            parenthesis.setExpression(join.getOnExpression());

            notExpression = new NotExpression(parenthesis);

            left = new Parenthesis();
            left.setExpression(notExpression);

            andExpression = new AndExpression(left, isNotNulls);

            if (s.getKey().equals(join.getRightItem().toString().toLowerCase())) {
                result.add(new JoinWhereItem(rightJoin, andExpression));
            } else {
                coreExpression = getCoreExpression(join.getOnExpression());

                Join useJoin;
                if (coreExpression instanceof IsNullExpression) {
                    useJoin = innerJoin;
                } else {
                    useJoin = leftJoin;
                }

                result.add(new JoinWhereItem(innerJoin, null));
                result.add(new JoinWhereItem(useJoin, andExpression));
            }
        }

        return result;
    }

    /**
     * Creates a generic shallow copy of the given join.
     * The join type is set to the default: JOIN.
     * @param join The join that should be copied.
     * @return A generic shallow copy of join.
     */
    public static Join createGenericCopyOfJoin(Join join) {
        Join outJoin = new Join();
        outJoin.setRightItem(join.getRightItem());
        outJoin.setOnExpression(join.getOnExpression());

        return outJoin;
    }

    /**
     * Creates an expression that concatenates {@link IsNullExpression}s
     * containing a {@link Column} using a {@link BinaryExpression}.
     * @param columns The columns that should be used in the concatenation.
     * @param binaryExpression The type of binary expression that should be used in the concatenation.
     * @param isNull Determines whether the column should be checked for IS NULL or IS NOT NULL.
     * @return A concatenation of IsNull expressions that contains each of the given columns.
     */
    private static Expression createIsNullExpressions(Stack<Column> columns,
                                               BinaryExpression binaryExpression, boolean isNull) {
        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setNot(!isNull);
        Parenthesis parenthesis = new Parenthesis();

        if (columns.size() == 1) {
            isNullExpression.setLeftExpression(columns.pop());
            parenthesis.setExpression(isNullExpression);

            return parenthesis;
        } else if (!columns.isEmpty()) {
            isNullExpression.setLeftExpression(columns.pop());
            parenthesis.setExpression(isNullExpression);

            binaryExpression.setRightExpression(parenthesis);
            binaryExpression.setLeftExpression(createIsNullExpressions(columns, binaryExpression, isNull));

            return binaryExpression;
        }

        throw new IllegalStateException("The columns list cannot be empty.");
    }

    /**
     * Simplified version of the create is null expression method.
     * Rather than taking in a stack and an expression, only the columns are needed.
     * @param columns The columns for which the is null expression have to be created.
     * @param isNull True if the columns should be IS NULL. False if the columns should be IS NOT NULL.
     * @return Returns the concatenated expressions.
     */
    private static Expression createIsNullExpressions(List<Column> columns, boolean isNull) {
        Stack<Column> stack = new Stack<>();
        stack.addAll(columns);
        return createIsNullExpressions(stack, new AndExpression(null, null), isNull);
    }


    /**
     * If an expression is nested in parentheses or in a not expression, retrieve the innermost expression that is
     * not either of these.
     * @param expression The expression to evaluate.
     * @return The innermost expression that is not nested in parenthesis or in a not.
     */
    public static Expression getCoreExpression(Expression expression) {
        if (expression instanceof Parenthesis) {
            return getCoreExpression(((Parenthesis) expression).getExpression());
        } else if (expression instanceof NotExpression) {
            return getCoreExpression(((NotExpression) expression).getExpression());
        } else {
            return expression;
        }
    }
}
