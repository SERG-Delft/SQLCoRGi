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
    private PlainSelect plainSelect;
    private Expression whereExpression;

    /**
     * Takes in a statement and mutates the joins. Each join will have its own set of mutations added to the results.
     * @param plainSelect The statement for which the joins have to be mutated.
     * @return A set of mutated queries in string format.
     */
    public Set<String> generateJoinWhereExpressions(PlainSelect plainSelect) {
        map = new HashMap<>();
        this.plainSelect = plainSelect;
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

                    temp.clear();
                }
                map.clear();
            }
        }

        out.setWhere(whereCondition);
        out.setJoins(joins);
        return result;
    }

    /**
     * Depending on whether the original statement has a where expression,
     * determines what the where condition of the mutated statement should be.
     * @param joinWhereExpression The where expression corresponding to the mutated join.
     * @param originalWhereCondition The where expression corresponding to the original query.
     * @return The where condition to be used in the mutated statement.
     */
    private static Expression determineWhereExpression(Expression joinWhereExpression,
                                                       Expression originalWhereCondition) {
        Parenthesis parenthesis = new Parenthesis(originalWhereCondition);
        Parenthesis parenthesisJoinWhere = new Parenthesis(joinWhereExpression);

        Expression out;
        if (originalWhereCondition == null) {
            out = joinWhereExpression;
        } else if (joinWhereExpression != null) {
            out = new AndExpression(parenthesisJoinWhere, parenthesis);
        } else {
            out = parenthesis;
        }
        return out;
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
        Join leftJoin = createGenericCopyOfJoin(join);
        leftJoin.setLeft(true);
        Join rightJoin = createGenericCopyOfJoin(join);
        rightJoin.setRight(true);
        Join innerJoin = createGenericCopyOfJoin(join);
        innerJoin.setInner(true);

        Expression isNotNulls;
        Expression isNulls;

        List<JoinWhereItem> result = new ArrayList<>();
        List<Column> values;
        Stack<Column> columns = new Stack<>();

        BinaryExpression leftJoinExpressionIsNull = new AndExpression(null, null);
        BinaryExpression leftJoinExpressionIsNotNull = new AndExpression(null, null);
        BinaryExpression rightJoinExpressionIsNull = new AndExpression(null, null);
        BinaryExpression rightJoinExpressionIsNotNull = new AndExpression(null, null);

        for (Map.Entry<String, List<Column>> s : map.entrySet()) {
            values = map.get(s.getKey());
            columns.addAll(values);
            isNulls = createIsNullExpressions(columns, new AndExpression(null, null), true);

            columns.addAll(values);
            isNotNulls = createIsNullExpressions(columns, new AndExpression(null, null), false);

            if (!s.getKey().equals(join.getRightItem().toString().toLowerCase())) {
                rightJoinExpressionIsNull.setLeftExpression(isNulls);
                rightJoinExpressionIsNotNull.setLeftExpression(isNulls);

                leftJoinExpressionIsNull.setRightExpression(isNulls);
                leftJoinExpressionIsNotNull.setRightExpression(isNotNulls);

                excludeNullColumnsInWhereExpression(values, plainSelect.getWhere());
                excludeNullColumnsInWhereExpression("a", plainSelect.getWhere());
            } else {
                rightJoinExpressionIsNull.setRightExpression(isNulls);
                rightJoinExpressionIsNotNull.setRightExpression(isNotNulls);

                leftJoinExpressionIsNull.setLeftExpression(isNulls);
                leftJoinExpressionIsNotNull.setLeftExpression(isNulls);
            }
        }
        result.add(new JoinWhereItem(innerJoin, null));
        result.add(new JoinWhereItem(leftJoin, leftJoinExpressionIsNull));
        result.add(new JoinWhereItem(leftJoin, leftJoinExpressionIsNotNull));
        result.add(new JoinWhereItem(rightJoin, rightJoinExpressionIsNull));
        result.add(new JoinWhereItem(rightJoin, rightJoinExpressionIsNotNull));

        return result;
    }

    /**
     * Modifies the input expression such that it no longer contains any of the given columns.
     * @param nulls The columns that should be excluded.
     * @param expression The expression that should be modified.
     * @return The modified expression.
     */
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

            isNotNulls = createIsNullExpressions(columns,false);
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
    private static Join createGenericCopyOfJoin(Join join) {
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
