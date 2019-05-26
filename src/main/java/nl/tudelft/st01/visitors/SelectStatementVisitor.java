package nl.tudelft.st01.visitors;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import nl.tudelft.st01.visitors.aggregate.GenAggregateFunctions;
import nl.tudelft.st01.visitors.join.GenJoinWhereExpression;
import nl.tudelft.st01.visitors.select.RuleGeneratorExpressionVisitor;

import java.util.ArrayList;
import java.util.Set;

/**
 * A visitor used for generating coverage targets of a SELECT statement.
 */
public class SelectStatementVisitor extends SelectVisitorAdapter {

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
        GenAggregateFunctions genAggregateFunctions = new GenAggregateFunctions();
        Set<String> outputAfterAggregator = genAggregateFunctions.generate(plainSelect);

        output.addAll(outputAfterAggregator);
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
