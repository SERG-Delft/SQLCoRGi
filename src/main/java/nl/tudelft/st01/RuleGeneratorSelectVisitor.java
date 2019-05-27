package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.util.ArrayList;
import java.util.Set;

/**
 * Custom Visitor for SELECT statements.
 */
public class RuleGeneratorSelectVisitor extends SelectVisitorAdapter {
    private Set<String> output;

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void visit(PlainSelect plainSelect) {

        if (output == null || !output.isEmpty()) {
            throw new IllegalArgumentException(
                "To use this visitor, you must first give it an empty list so it can pass along the generated queries."
            );
        }

        handleWhere(plainSelect);
        handleAggregators(plainSelect);
        handleGroupBy(plainSelect);
        handleJoins(plainSelect);

        output = null;
    }

    /**
     * Handles the where part of the query. Adds the results to the output.
     * @param plainSelect Input plainselect from which the expressions have to be derived.
     */
    private void handleWhere(PlainSelect plainSelect) {
        Expression where = plainSelect.getWhere();
        ArrayList<Expression> expressions = new ArrayList<>();

        if (where != null) {
            RuleGeneratorExpressionVisitor ruleGeneratorExpressionVisitor = new RuleGeneratorExpressionVisitor();

            ruleGeneratorExpressionVisitor.setOutput(expressions);
            where.accept(ruleGeneratorExpressionVisitor);
            for (Expression expression : expressions) {
                plainSelect.setWhere(expression);
                output.add(plainSelect.toString());
            }
        }

        plainSelect.setWhere(where);
    }

    /**
     * Handles the aggregators part of the query. Adds the results to the output.
     * @param plainSelect Input plainselect from which the cases have to be derived.
     */
    private void handleAggregators(PlainSelect plainSelect) {
        AggregateFunctions aggregateFunctions = new AggregateFunctions();
        Set<String> outputAfterAggregator = aggregateFunctions.generate(plainSelect);

        output.addAll(outputAfterAggregator);
    }

    /**
     * Handles the group by part of the query. Adds the results to the output.
     * @param plainSelect Input plainselect from which the cases have to be derived.
     */
    private void handleGroupBy(PlainSelect plainSelect) {
        GroupByElement groupBy = plainSelect.getGroupBy();

        if (groupBy != null) {
            GroupBy groupByExpression = new GroupBy();
            Set<String> outputAfterGroupBy = groupByExpression.generate(plainSelect);

            output.addAll(outputAfterGroupBy);
        }
    }

    /**
     * Handles the joins given the plainselect. Adds the results to the output.
     * @param plainSelect The input query for which the mutations have to be generated.
     */
    public void handleJoins(PlainSelect plainSelect) {
        GenJoinWhereExpression genJoinWhereExpression = new GenJoinWhereExpression();
        Set<String> out = genJoinWhereExpression.generateJoinWhereExpressions(plainSelect);

        output.addAll(out);
    }

    public void setOutput(Set<String> output) {
        this.output = output;
    }

}
