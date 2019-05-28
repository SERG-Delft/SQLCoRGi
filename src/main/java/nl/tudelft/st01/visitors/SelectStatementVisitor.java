package nl.tudelft.st01.visitors;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import nl.tudelft.st01.AggregateFunctionsGenerator;
import nl.tudelft.st01.GroupByGenerator;
import nl.tudelft.st01.JoinWhereExpressionGenerator;
import nl.tudelft.st01.visitors.select.SelectExpressionVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static nl.tudelft.st01.JoinWhereExpressionGenerator.genericCopyOfJoin;

/**
 * A visitor used for generating coverage targets of a SELECT statement.
 */
public class SelectStatementVisitor extends SelectVisitorAdapter {

    private Set<String> output;

    /**
     * Creates a new visitor which can be used to generate coverage rules for queries.
     * Any rules that are generated will be written to {@code output}.
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
     * Generates coverage rules for the WHERE clause of the query that is being visited.
     * The generated rules are stored in the {@code output} set.
     *
     * @param plainSelect the {@code PlainSelect} for which coverage targets need to be generated.
     */
    private void handleWhere(PlainSelect plainSelect) {
        Expression where = plainSelect.getWhere();
        if (where != null) {

            List<Expression> expressions = new ArrayList<>();
            SelectExpressionVisitor selectExpressionVisitor = new SelectExpressionVisitor(expressions);

            List<Join> joins = plainSelect.getJoins();
            if (joins != null) {
                List<Join> innerJoins = new ArrayList<>();
                for (int i = 0; i < joins.size(); i++) {
                    innerJoins.add(genericCopyOfJoin(joins.get(i)));
                    innerJoins.get(i).setInner(true);
                }
                plainSelect.setJoins(innerJoins);

            }

            where.accept(selectExpressionVisitor);
            for (Expression expression : expressions) {
                plainSelect.setWhere(expression);
                output.add(plainSelect.toString());
            }

            plainSelect.setWhere(where);
        }
    }

    /**
     * Generates coverage rules for the Aggregate functions of the query that is being visited.
     * The generated rules are stored in the {@code output} set.
     *
     * @param plainSelect the {@code PlainSelect} for which coverage targets need to be generated.
     */
    private void handleAggregators(PlainSelect plainSelect) {
        AggregateFunctionsGenerator aggregateFunctionsGenerator = new AggregateFunctionsGenerator();
        Set<String> outputAfterAggregator = aggregateFunctionsGenerator.generate(plainSelect);

        output.addAll(outputAfterAggregator);
    }

    /**
     * Generates coverage rules for the GROUP BY clause of the query that is being visited.
     * The generated rules are stored in the {@code output} set.
     *
     * @param plainSelect the {@code PlainSelect} for which coverage targets need to be generated.
     */
    private void handleGroupBy(PlainSelect plainSelect) {
        GroupByElement groupBy = plainSelect.getGroupBy();

        if (groupBy != null) {
            GroupByGenerator groupByGeneratorExpression = new GroupByGenerator();
            Set<String> outputAfterGroupBy = groupByGeneratorExpression.generate(plainSelect);

            output.addAll(outputAfterGroupBy);
        }
    }

    /**
     * Generates coverage rules for the HAVING clause of the query that is being visited.
     * The generated rules are stored in the {@code output} set.
     *
     * @param plainSelect the {@code PlainSelect} for which coverage targets need to be generated.
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
     * Generates coverage rules for the JOIN operators of the query that is being visited.
     * The generated rules are stored in the {@code output} set.
     *
     * @param plainSelect the {@code PlainSelect} for which coverage targets need to be generated.
     */
    private void handleJoins(PlainSelect plainSelect) {
        JoinWhereExpressionGenerator joinWhereExpressionGenerator = new JoinWhereExpressionGenerator();
        Set<String> out = joinWhereExpressionGenerator.generateJoinWhereExpressions(plainSelect);

        output.addAll(out);
    }

}
