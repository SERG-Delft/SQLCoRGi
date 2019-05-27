package nl.tudelft.st01.visitors;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import nl.tudelft.st01.AggregateFunctions;
import nl.tudelft.st01.GroupBy;
import nl.tudelft.st01.JoinWhereExpression;
import nl.tudelft.st01.visitors.select.SelectExpressionVisitor;

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
    public void visit(PlainSelect plainSelect) {

        handleWhere(plainSelect);
        handleAggregators(plainSelect);
        handleGroupBy(plainSelect);
        handleHaving(plainSelect);
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
            SelectExpressionVisitor selectExpressionVisitor = new SelectExpressionVisitor(expressions);

            where.accept(selectExpressionVisitor);
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
     * @param plainSelect Input plainselect from which the cases have to be derived.
     */
    private void handleAggregators(PlainSelect plainSelect) {
        AggregateFunctions aggregateFunctions = new AggregateFunctions();
        Set<String> outputAfterAggregator = aggregateFunctions.generate(plainSelect);

        output.addAll(outputAfterAggregator);
    }

    /**
     * Handles the group by part of the query. Adds the results to the output.
     *
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
     * Handles the having part of the query. Adds the results to the output.
     *
     * @param plainSelect Input plainselect from which the cases have to be derived.
     */
    private void handleHaving(PlainSelect plainSelect) {
        Expression having = plainSelect.getHaving();
        if (having != null) {

            List<Expression> expressions = new ArrayList<>();
            SelectExpressionVisitor selectExpressionVisitor = new SelectExpressionVisitor(expressions);

            having.accept(selectExpressionVisitor);
            for (Expression expression : expressions) {
                plainSelect.setHaving(expression);
                output.add(plainSelect.toString());
            }

            plainSelect.setHaving(having);
        }
    }

    /**
     * Handles the joins given the plainselect. Adds the results to the output.
     *
     * @param plainSelect The input query for which the mutations have to be generated.
     */
    private void handleJoins(PlainSelect plainSelect) {
        JoinWhereExpression joinWhereExpression = new JoinWhereExpression();
        Set<String> out = joinWhereExpression.generateJoinWhereExpressions(plainSelect);

        output.addAll(out);
    }

}
