package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Visitor for SELECT statements.
 */
public class RuleGeneratorSelectVisitor extends SelectVisitorAdapter {
    private static final String COUNT_STRING = "COUNT";

    private List<PlainSelect> output;

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void visit(PlainSelect plainSelect) {

        if (output == null || !output.isEmpty()) {
            throw new IllegalArgumentException(
                "To use this visitor, you must first give it an empty list so it can pass along the generated queries."
            );
        }

        AggregateFunctions aggregateFunctions = new AggregateFunctions();
        List<PlainSelect> outputAfterAggregator = aggregateFunctions.generate(plainSelect);

        Expression where = plainSelect.getWhere();
        GroupByElement groupBy = plainSelect.getGroupBy();

        ArrayList<Expression> whereExpressions = new ArrayList<>();

        if (where != null || groupBy != null) {
            if (where != null) {
                RuleGeneratorExpressionVisitor ruleGeneratorExpressionVisitor = new RuleGeneratorExpressionVisitor();
                ruleGeneratorExpressionVisitor.setOutput(whereExpressions);
                where.accept(ruleGeneratorExpressionVisitor);

                for (PlainSelect plainSelectAfterAggregator : outputAfterAggregator) {
                    for (Expression whereExpression : whereExpressions) {
                        PlainSelect plainSelectOut = UtilityGetters.deepCopy(plainSelectAfterAggregator, true);
                        plainSelectOut.setWhere(whereExpression);

                        output.add(plainSelectOut);
                    }
                }
            } else if (outputAfterAggregator.size() > 1) {
                // TODO this is really ugly and should be fixed in the JOIN Merge Request
                // Only add output from aggregation functions when it is has more than 1 element,
                // otherwise it will interfere with GROUP BY
                for (PlainSelect plainSelectAfterAggregator : outputAfterAggregator) {
                    PlainSelect plainSelectOut = UtilityGetters.deepCopy(plainSelectAfterAggregator, true);

                    output.add(plainSelectOut);
                }
            }

            if (groupBy != null) {
                GroupBy groupByExpression = new GroupBy();
                output.addAll(groupByExpression.generate(plainSelect));
            }
        } else {
            // Since there is no where, we don't need that part.
            // We do want the result of the output from the aggregator part,
            //      so we add those plainSelects to the output list
            output.addAll(outputAfterAggregator);
        }

        output = null;
    }

    public void setOutput(List<PlainSelect> output) {
        this.output = output;
    }
}
