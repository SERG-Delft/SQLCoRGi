package nl.tudelft.st01;


import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenJoinWhereExpression {
    private Map<String, Expression> output;
    private FromItem fromItem;
    public void generateJoinWhereExpressions(PlainSelect plainSelect) {
        this.fromItem = plainSelect.getFromItem();
        List<Join> joins = plainSelect.getJoins();

        RuleGeneratorFromVisitor fromVisitor = new RuleGeneratorFromVisitor();
        RuleGeneratorOnExpressionVisitor ruleGeneratorOnExpressionVisitor = new RuleGeneratorOnExpressionVisitor();

        fromItem.accept(fromVisitor);

        output = new HashMap<>();
        ruleGeneratorOnExpressionVisitor.setOutput(output);
        joins.get(0).getOnExpression().accept(ruleGeneratorOnExpressionVisitor);

        generateExpressions(joins.get(0), fromItem);
        System.out.println(output);
        output = null;

    }

    /**
     * Generates the WHERE conditions that should be appended to the original statement.
     * Note that the context of the statement must be known in order to identify the keys.
     */
    private List<Expression> generateExpressions(Join join, FromItem fromItem) {
        createInnerJoinExpression(join);
        createLeftJoinExpression(join);
        createRightJoinExpression(join);

        return null;
    }

    private Expression createInnerJoinExpression(Join join) {
        return null;
    }

    private Expression createLeftJoinExpression(Join join) {
        return null;
    }

    private Expression createRightJoinExpression(Join join) {
        return null;
    }



}
