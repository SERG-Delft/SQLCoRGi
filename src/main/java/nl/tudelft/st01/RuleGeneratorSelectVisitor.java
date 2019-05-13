package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;


import java.util.ArrayList;
import java.util.List;

/**
 * Custom Visitor for SELECT statements.
 */
public class RuleGeneratorSelectVisitor extends SelectVisitorAdapter {

    private List<PlainSelect> output;

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void visit(PlainSelect plainSelect) {

        if (output == null || !output.isEmpty()) {
            throw new IllegalArgumentException(
                "To use this visitor, you must first give it an empty list so it can pass along the generated queries."
            );
        }

        Expression where = plainSelect.getWhere();
        FromItem from = plainSelect.getFromItem();
        Alias alias = from.getAlias();
        List<Join> joins = plainSelect.getJoins();

        System.out.println(joins.get(0).getRightItem());
        System.out.println(from.toString());
        System.out.println(joins.toString());
        System.out.println(alias.toString());

        if (where != null) {
            RuleGeneratorExpressionVisitor ruleGeneratorExpressionVisitor = new RuleGeneratorExpressionVisitor();
            ArrayList<Expression> expressions = new ArrayList<>();
            ruleGeneratorExpressionVisitor.setOutput(expressions);
            where.accept(ruleGeneratorExpressionVisitor);

            for (Expression expression : expressions) {
                PlainSelect plainSelectOut = new PlainSelect();
                plainSelectOut.setSelectItems(plainSelect.getSelectItems());
                plainSelectOut.setFromItem(plainSelect.getFromItem());
                plainSelectOut.setWhere(expression);

                output.add(plainSelectOut);
            }
        }

        output = null;
    }

    public void setOutput(List<PlainSelect> output) {
        this.output = output;
    }
}
