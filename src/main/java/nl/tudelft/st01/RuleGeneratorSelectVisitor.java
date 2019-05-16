package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Visitor for SELECT statements.
 */
public class RuleGeneratorSelectVisitor extends SelectVisitorAdapter {

    private List<PlainSelect> outputAfterAggregator;

    private List<PlainSelect> output;

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void visit(PlainSelect plainSelect) {

        if (output == null || !output.isEmpty()) {
            throw new IllegalArgumentException(
                "To use this visitor, you must first give it an empty list so it can pass along the generated queries."
            );
        }

        GenAggregateFunctions genAggregateFunctions = new GenAggregateFunctions();
        outputAfterAggregator = genAggregateFunctions.generate(plainSelect);

        Expression where = plainSelect.getWhere();
        if (where != null) {
            RuleGeneratorExpressionVisitor ruleGeneratorExpressionVisitor = new RuleGeneratorExpressionVisitor();
            ArrayList<Expression> expressions = new ArrayList<>();
            ruleGeneratorExpressionVisitor.setOutput(expressions);
            where.accept(ruleGeneratorExpressionVisitor);

            for (PlainSelect plainSelectAfterAggregator : outputAfterAggregator) {
                for (Expression expression : expressions) {
                    PlainSelect plainSelectOut = deepCopy(plainSelectAfterAggregator);
                    plainSelectOut.setWhere(expression);

                    output.add(plainSelectOut);
                }
            }
        } else {
            // Since there is no where, we don't need that part.
            // We do want the result of the output from the aggregator part,
            //      so we add those plainSelects to the output list
            for (PlainSelect p : outputAfterAggregator) {
                output.add(p);
            }
        }

        output = null;
    }

    public void setOutput(List<PlainSelect> output) {
        this.output = output;
    }


    /** Returns a deep copy of a plainSelect. The idea here is that you use this
     *  to get a copy of the object, then again add the attributes that you wanted
     *  to change in the first place.
     *
     * @param plainSelect - object to copy
     * @return deep copy of object
     */
    private PlainSelect deepCopy(PlainSelect plainSelect) {
        PlainSelect newPlainSelect = new PlainSelect();

        newPlainSelect.setSelectItems(plainSelect.getSelectItems());
        newPlainSelect.setFromItem(plainSelect.getFromItem());
        newPlainSelect.setHaving(plainSelect.getHaving());
        newPlainSelect.setWhere(plainSelect.getWhere());
        newPlainSelect.setJoins(plainSelect.getJoins());
        newPlainSelect.setGroupByElement(plainSelect.getGroupBy());

        return newPlainSelect;
    }
}
