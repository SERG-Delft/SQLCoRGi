package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class GenJoinWhereExpression {
    private Map<String, List<Column>> output;
    private FromItem fromItem;

    public void generateJoinWhereExpressions(PlainSelect plainSelect) {
        this.fromItem = plainSelect.getFromItem();
        List<Join> joins = plainSelect.getJoins();

        RuleGeneratorFromVisitor fromVisitor = new RuleGeneratorFromVisitor();
        RuleGeneratorOnExpressionVisitor ruleGeneratorOnExpressionVisitor = new RuleGeneratorOnExpressionVisitor();

        fromItem.accept(fromVisitor);
        output = new HashMap<>();
        ruleGeneratorOnExpressionVisitor.setOutput(output);
        List<JoinWhereItem> joinWhereItems = new ArrayList<>();

        for (int i = 0; i < joins.size(); i++) {
            Join join = joins.get(i);
            join.getOnExpression().accept(ruleGeneratorOnExpressionVisitor);

            joinWhereItems = generateExpressions(join);
            List<Join> temp = new ArrayList<>();
            temp.addAll(joins);
            PlainSelect out = new PlainSelect();
            for (JoinWhereItem joinWhereItem : joinWhereItems) {
                temp.set(i, joinWhereItem.getJoin());
                
            }

            temp.clear();
        }


        output = null;
        fromItem = null;
    }

    /**
     * Generates the WHERE conditions that should be appended to the original statement.
     * Note that the context of the statement must be known in order to identify the keys.
     */
    private List<JoinWhereItem> generateExpressions(Join join) {
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

        for (String s : output.keySet()) {

            List<Column> values = output.get(s);
            Stack<Column> columns = new Stack<>();
            columns.addAll(values);
            isNulls = createIsNullExpressions(columns, new AndExpression(null, null), true);

            columns.addAll(values);
            isNotNulls = createIsNullExpressions(columns, new AndExpression(null, null), false);

            if (!s.equals(join.getRightItem().toString().toLowerCase())) {
                leftJoinExpressionIsNull.setLeftExpression(isNulls);
                leftJoinExpressionIsNotNull.setLeftExpression(isNulls);

                rightJoinExpressionIsNull.setRightExpression(isNulls);
                rightJoinExpressionIsNotNull.setRightExpression(isNotNulls);
            } else {
                leftJoinExpressionIsNull.setRightExpression(isNulls);
                leftJoinExpressionIsNotNull.setRightExpression(isNotNulls);

                rightJoinExpressionIsNull.setLeftExpression(isNulls);
                rightJoinExpressionIsNotNull.setLeftExpression(isNotNulls);
            }
        }
        result.add(new JoinWhereItem(innerJoin, null));
        result.add(new JoinWhereItem(leftJoin, leftJoinExpressionIsNull));
        result.add(new JoinWhereItem(leftJoin, leftJoinExpressionIsNotNull));
        result.add(new JoinWhereItem(rightJoin, rightJoinExpressionIsNull));
        result.add(new JoinWhereItem(rightJoin, rightJoinExpressionIsNotNull));

        return result;
    }



    private Join createGenericCopyOfJoin(Join join) {
        Join outJoin = new Join();
        outJoin.setRightItem(join.getRightItem());
        outJoin.setOnExpression(join.getOnExpression());
        return outJoin;
    }

    private Expression createIsNullExpressions(Stack<Column> columns, BinaryExpression binaryExpression, boolean isNull) {
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

            binaryExpression.setLeftExpression(parenthesis);
            binaryExpression.setRightExpression(createIsNullExpressions(columns, binaryExpression, isNull));
            return binaryExpression;
        }
        // TODO: Throw exception.
        return binaryExpression;
    }
}
