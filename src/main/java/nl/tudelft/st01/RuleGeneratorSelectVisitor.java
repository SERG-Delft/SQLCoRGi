package nl.tudelft.st01;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
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

        if (plainSelect.getGroupBy() != null) {
            // First statement for branch coverage
            PlainSelect plainSelectOut = new PlainSelect();

            GreaterThan gt = new GreaterThan();
            Function count = new Function();
            count.setName("count");
            count.setAllColumns(true);

            gt.setLeftExpression(count);
            gt.setRightExpression(new DoubleValue("1"));

            plainSelectOut.setSelectItems(plainSelect.getSelectItems());
            plainSelectOut.setFromItem(plainSelect.getFromItem());
            plainSelectOut.setGroupByElement(plainSelect.getGroupBy());
            plainSelectOut.setHaving(gt);

            output.add(plainSelectOut);

            // Second statement for branch coverage
            PlainSelect plainSelectOut2 = new PlainSelect();

            GreaterThan gt2 = new GreaterThan();
            Function count2 = new Function();
            List groupByValue = new ArrayList<Expression>();
            groupByValue.add(plainSelect.getGroupBy().getGroupByExpressions().get(0));
            count2.setName("count");
            ExpressionList parameters = new ExpressionList();
            parameters.setExpressions(groupByValue);
            count2.setParameters(parameters);
            count2.setDistinct(true);

            gt2.setLeftExpression(count2);
            gt2.setRightExpression(new DoubleValue("1"));

            SelectExpressionItem selectItem = new SelectExpressionItem(count);
            selectItem.setExpression(count);

            plainSelectOut2.addSelectItems(selectItem);
            plainSelectOut2.setFromItem(plainSelect.getFromItem());
            plainSelectOut2.setGroupByElement(plainSelect.getGroupBy());
            plainSelectOut2.setHaving(gt2);

            output.add(plainSelectOut2);
        }

        output = null;
    }

    public void setOutput(List<PlainSelect> output) {
        this.output = output;
    }
}
