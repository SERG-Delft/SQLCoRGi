package com.github.sergdelft.sqlcorgi.unit.visitors.subqueries;

import com.github.sergdelft.sqlcorgi.visitors.subqueries.SubqueryFinder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link SubqueryFinder} class.
 */
class SubqueryFinderTest {

    private static final String SUB_NULL = "null";
    private static final String SUB_NULL_PAR = "(null)";

    /**
     * Checks that subqueries are correctly returned in a {@code Map} when visiting an {@link Expression} and calling
     * {@link SubqueryFinder#getSubqueries()} afterwards.
     */
    @Test
    void testGetSubqueries() {

        SubSelect subIn = new SubSelect();
        subIn.setUseBrackets(false);

        SubSelect subExists = new SubSelect();

        InExpression inExpression = new InExpression(new Column(), subIn);
        ExistsExpression existsExpression = new ExistsExpression();
        existsExpression.setRightExpression(subExists);

        AndExpression andExpression = new AndExpression(inExpression, existsExpression);

        SubqueryFinder subqueryFinder = new SubqueryFinder();
        andExpression.accept(subqueryFinder);
        Map<String, SubSelect> subqueries = subqueryFinder.getSubqueries();

        assertThat(subqueries).containsOnlyKeys(SUB_NULL, SUB_NULL_PAR);
        assertThat(subqueries.get(SUB_NULL)).isSameAs(subIn);
        assertThat(subqueries.get(SUB_NULL_PAR)).isSameAs(subExists);
    }
}
