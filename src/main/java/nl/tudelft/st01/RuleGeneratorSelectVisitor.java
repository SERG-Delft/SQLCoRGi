package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
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
        handleJoins(plainSelect);

        Expression expression = plainSelect.getWhere();
        List<Expression> expressions;

        expressions = handleWhere(plainSelect);
        handleAggregators(plainSelect, expressions);


        plainSelect.setWhere(expression);

        output = null;
    }

    /**
     * Handles the where part of the query.
     * @param plainSelect Input plainselect from which the expression have to be derived.
     * @return List of mutated where expressions.
     */
    private List<Expression> handleWhere(PlainSelect plainSelect) {
        Expression where = plainSelect.getWhere();
        ArrayList<Expression> expressions = new ArrayList<>();

        if (where != null) {
            RuleGeneratorExpressionVisitor ruleGeneratorExpressionVisitor = new RuleGeneratorExpressionVisitor();

            ruleGeneratorExpressionVisitor.setOutput(expressions);
            where.accept(ruleGeneratorExpressionVisitor);

        }
        // TODO: convert to ps and set where for each case.
        return expressions;
    }

    /**
     * Handles the aggregators part of the query. Bases its results on the expression generated in the where handler.
     * Adds the results to the output.
     * @param plainSelect Input plainselect from which the cases have to be derived.
     * @param expressions The expressions generated in the where handler.
     */
    private void handleAggregators(PlainSelect plainSelect, List<Expression> expressions) {
        GenAggregateFunctions genAggregateFunctions = new GenAggregateFunctions();
        List<PlainSelect> outputAfterAggregator = genAggregateFunctions.generate(plainSelect);


        for (PlainSelect ps : outputAfterAggregator) {
            if (expressions == null || expressions.isEmpty()) {
                output.add(ps.toString());
            } else {
                for (Expression e : expressions) {
                    ps.setWhere(e);
                    output.add(ps.toString());
                }
            }
        }
        // TODO: No cartesian product for ps and the expression, just take original.
    }

    public void setOutput(Set<String> output) {
        this.output = output;
    }

    /**
     * Handles the joins given the plainselect.
     * @param plainSelect The input query for which the mutations have to be generated.
     */
    public void handleJoins(PlainSelect plainSelect) {
        GenJoinWhereExpression genJoinWhereExpression = new GenJoinWhereExpression();
        Set<String> out = genJoinWhereExpression.generateJoinWhereExpressions(plainSelect);

        output.addAll(out);
    }
}
