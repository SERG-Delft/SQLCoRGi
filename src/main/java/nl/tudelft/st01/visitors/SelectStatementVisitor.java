package nl.tudelft.st01.visitors;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import nl.tudelft.st01.visitors.aggregate.GenAggregateFunctions;
import nl.tudelft.st01.visitors.join.GenJoinWhereExpression;
import nl.tudelft.st01.visitors.select.RuleGeneratorExpressionVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A visitor used for generating coverage targets of a SELECT statement.
 */
public class SelectStatementVisitor extends SelectVisitorAdapter {

    private Set<String> output;

    /**
     * Creates a new visitor which can be used to generate coverage rules for queries. Any rules that are generated
     * will be written to {@code output}.
     *
     * @param output the set to which generated rules should be written. This set must not be null, and must be empty.
     */
    public SelectStatementVisitor(Set<String> output) {
        if (output == null || !output.isEmpty()) {
            throw new IllegalArgumentException(
                "A SelectStatementVisitor requires an empty, non-null set to which it can output generated rules."
            );
        }

        this.output = output;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void visit(PlainSelect plainSelect) {

        handleWhere(plainSelect);
        handleAggregators(plainSelect);
        handleJoins(plainSelect);

        output = null;
    }

    /**
     * Generates coverage rules for the WHERE clause of the query that is being visited. The generated rules are stored
     * in the {@code output} set.
     *
     * @param plainSelect the {@code PlainSelect} for which coverage targets need to be generated.
     */
    private void handleWhere(PlainSelect plainSelect) {

        Expression where = plainSelect.getWhere();
        if (where != null) {

            List<Expression> expressions = new ArrayList<>();
            RuleGeneratorExpressionVisitor ruleGeneratorExpressionVisitor = new RuleGeneratorExpressionVisitor();
            ruleGeneratorExpressionVisitor.setOutput(expressions);

            where.accept(ruleGeneratorExpressionVisitor);
            for (Expression expression : expressions) {
                plainSelect.setWhere(expression);
                output.add(plainSelect.toString());
            }

            plainSelect.setWhere(where);
        }
    }

    /**
     * Handles the aggregators part of the query. Adds the results to the output.
     *
     * @param plainSelect the {@code PlainSelect} for which coverage targets need to be generated.
     */
    private void handleAggregators(PlainSelect plainSelect) {
        GenAggregateFunctions genAggregateFunctions = new GenAggregateFunctions();
        Set<String> outputAfterAggregator = genAggregateFunctions.generate(plainSelect);

        output.addAll(outputAfterAggregator);
    }

    /**
     * Generates coverage rules for the JOIN operators of the query that is being visited. The generated rules are
     * stored in the {@code output} set.
     * @param plainSelect the {@code PlainSelect} for which coverage targets need to be generated.
     */
    private void handleJoins(PlainSelect plainSelect) {
        GenJoinWhereExpression genJoinWhereExpression = new GenJoinWhereExpression();
        Set<String> out = genJoinWhereExpression.generateJoinWhereExpressions(plainSelect);

        output.addAll(out);
    }

}
