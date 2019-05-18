package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
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


    /**
     * Takes in a statement and mutates the joins. Each join will have its own set of mutations added to the results.
     * @param plainSelect The statement for which the joins have to be mutated.
     * @return A set of mutated queries in string format.
     */
    public Set<String> generateJoinWhereExpressions(PlainSelect plainSelect) {
        map = new HashMap<>();

        Set<String> result = new TreeSet<>();

        List<Join> joins = plainSelect.getJoins();
        List<JoinWhereItem> joinWhereItems;
        Join join;

        PlainSelect out = plainSelect;

        if (!(joins == null || joins.isEmpty())) {
            Expression whereCondition = plainSelect.getWhere();

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
                    out.setWhere(determineWhereExpression(joinWhereItem.getJoinWhere(), whereCondition));

                    result.add(out.toString());
                    temp.clear();
                }
                map.clear();
            }
        }

        return result;
    }

    /**
     * Depending on whether the original statement has a where expression,
     * determines what the where condition of the mutated statement should be.
     * @param joinWhereExpression The where expression corresponding to the mutated join.
     * @param originalWhereCondition The where expression corresponding to the original query.
     * @return The where condition to be used in the mutated statement.
     */
    private Expression determineWhereExpression(Expression joinWhereExpression, Expression originalWhereCondition) {
        Parenthesis parenthesis = new Parenthesis();
        Parenthesis parenthesisJoinWhere = new Parenthesis();

        Expression out;
        parenthesis.setExpression(originalWhereCondition);
        parenthesisJoinWhere.setExpression(joinWhereExpression);
        if (originalWhereCondition == null) {
            out = joinWhereExpression;
        } else if (!(joinWhereExpression == null)) {
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
        Join leftJoin = createGenericCopyOfJoin(join);
        leftJoin.setLeft(true);
        Join rightJoin = createGenericCopyOfJoin(join);
        rightJoin.setRight(true);
        Join innerJoin = createGenericCopyOfJoin(join);
        innerJoin.setInner(true);

        Expression isNotNulls;
        Expression isNulls;

        BinaryExpression leftJoinExpressionIsNull = new AndExpression(null, null);
        BinaryExpression leftJoinExpressionIsNotNull = new AndExpression(null, null);
        BinaryExpression rightJoinExpressionIsNull = new AndExpression(null, null);
        BinaryExpression rightJoinExpressionIsNotNull = new AndExpression(null, null);

        List<JoinWhereItem> result = new ArrayList<>();
        List<Column> values;
        Stack<Column> columns = new Stack<>();

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
     * Creates a generic shallow copy of the given join.
     * The join type is set to the default: JOIN.
     * @param join The join that should be copied.
     * @return A generic shallow copy of join.
     */
    private Join createGenericCopyOfJoin(Join join) {
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
    private Expression createIsNullExpressions(Stack<Column> columns,
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
}
