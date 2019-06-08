package nl.tudelft.st01.visitors;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import nl.tudelft.st01.AggregateFunctionsGenerator;
import nl.tudelft.st01.GroupByGenerator;
import nl.tudelft.st01.JoinWhereExpressionGenerator;
import nl.tudelft.st01.visitors.select.SelectExpressionVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static nl.tudelft.st01.util.cloner.SelectCloner.copy;

/**
 * A visitor used for generating coverage targets of a SELECT statement.
 */
public class SelectStatementVisitor extends SelectVisitorAdapter {

    private Set<String> output;
    private List<SelectBody> statements;

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
        this.statements = new ArrayList<>();
    }

    @Override
    public void visit(PlainSelect plainSelect) {

        handleWhere(plainSelect);
        for (SelectBody selectBody : this.statements) {
            this.output.add(selectBody.toString());
        }
        handleAggregators(plainSelect);
        handleGroupBy(plainSelect);
        handleHaving(plainSelect);
        handleJoins(plainSelect);


        //applyNullReduction();

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

            PlainSelect copy = (PlainSelect) copy(plainSelect);
            where = copy.getWhere();

            List<Join> joins = copy.getJoins();
            if (joins != null) {
                for (Join join : joins) {
                    join.setInner(true);
                    join.setRight(false);
                    join.setLeft(false);
                    join.setOuter(false);
                    join.setSemi(false);
                    join.setCross(false);
                    join.setSimple(false);
                    join.setNatural(false);
                    join.setFull(false);
                }
            }

            List<Expression> expressions = new ArrayList<>();
            SelectExpressionVisitor selectExpressionVisitor = new SelectExpressionVisitor(expressions);

            where.accept(selectExpressionVisitor);
            copy.setWhere(null);
            for (Expression expression : expressions) {

                PlainSelect selectCopy = (PlainSelect) copy(copy);
                selectCopy.setWhere(expression);
                statements.add(selectCopy);
            }
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
