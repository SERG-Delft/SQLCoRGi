package com.github.sergdelft.sqlcrg.unit.visitors.subqueries;

import com.github.sergdelft.sqlcrg.visitors.subqueries.SubqueryRemover;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link SubqueryRemover} class.
 */
class SubqueryRemoverTest {

    /**
     * Checks that {@link SubqueryRemover#SubqueryRemover(String)} correctly initializes a new {@link SubqueryRemover}.
     */
    @Test
    void testConstructor() {

        SubqueryRemover subqueryRemover = new SubqueryRemover(null);

        assertThat(subqueryRemover.getChild()).isNull();
        assertThat(subqueryRemover.isUpdateChild()).isFalse();
    }

    /**
     * Verifies that {@link SubqueryRemover#visit(SubSelect)} correctly updates the {@link SubqueryRemover} if the
     * visited subquery needs to be removed.
     */
    @Test
    void testVisitSubqueryRemove() {

        SubSelect subSelect = new SubSelect();
        String subqueryString = subSelect.toString();

        SubqueryRemover subqueryRemover = new SubqueryRemover(subqueryString);
        subSelect.accept((ExpressionVisitor) subqueryRemover);

        assertThat(subqueryRemover.getChild()).isNull();
        assertThat(subqueryRemover.isUpdateChild()).isTrue();
    }

    /**
     * Verifies that {@link SubqueryRemover#visit(SubSelect)} does not update the {@link SubqueryRemover} if the
     * visited subquery does not need to be removed.
     */
    @Test
    void testVisitSubqueryNoRemove() {

        SubSelect subSelect = new SubSelect();

        SubqueryRemover subqueryRemover = new SubqueryRemover(null);
        subSelect.accept((ExpressionVisitor) subqueryRemover);

        assertThat(subqueryRemover.getChild()).isNull();
        assertThat(subqueryRemover.isUpdateChild()).isFalse();
    }

    /**
     * Verifies that {@code SubqueryRemover#visitLogicalOperator(BinaryExpression)} correctly updates the
     * {@link SubqueryRemover} if the left subexpression of the logical operator that is being visited must be removed.
     */
    @Test
    void testVisitLogicalRemoveLeft() {

        SubSelect subSelect = new SubSelect();
        String subqueryStr = subSelect.toString();

        NullValue right = new NullValue();
        AndExpression andExpression = new AndExpression(subSelect, right);

        SubqueryRemover subqueryRemover = new SubqueryRemover(subqueryStr);
        andExpression.accept(subqueryRemover);

        assertThat(subqueryRemover.getChild()).isSameAs(right);
        assertThat(subqueryRemover.isUpdateChild()).isTrue();
    }

    /**
     * Verifies that {@code SubqueryRemover#visitLogicalOperator(BinaryExpression)} correctly updates the
     * {@link SubqueryRemover} if the right subexpression of the logical operator that is being visited must be removed.
     */
    @Test
    void testVisitLogicalRemoveRight() {

        SubSelect subSelect = new SubSelect();
        String subqueryStr = subSelect.toString();

        NullValue left = new NullValue();
        OrExpression orExpression = new OrExpression(left, subSelect);

        SubqueryRemover subqueryRemover = new SubqueryRemover(subqueryStr);
        orExpression.accept(subqueryRemover);

        assertThat(subqueryRemover.getChild()).isSameAs(left);
        assertThat(subqueryRemover.isUpdateChild()).isTrue();
    }

    /**
     * Verifies that {@code SubqueryRemover#visitLogicalOperator(BinaryExpression)} correctly updates the
     * {@link SubqueryRemover} if both subexpressions of the logical operator that is being visited must be removed.
     */
    @Test
    void testVisitLogicalRemoveBoth() {

        SubSelect subSelect = new SubSelect();
        String subqueryStr = subSelect.toString();

        OrExpression orExpression = new OrExpression(subSelect, subSelect);

        SubqueryRemover subqueryRemover = new SubqueryRemover(subqueryStr);
        orExpression.accept(subqueryRemover);

        assertThat(subqueryRemover.getChild()).isNull();
        assertThat(subqueryRemover.isUpdateChild()).isTrue();
    }

    /**
     * Verifies that {@code SubqueryRemover#visitLogicalOperator(BinaryExpression)} correctly updates the
     * {@link SubqueryRemover} if the left subexpression of the logical operator that is being visited must be updated.
     */
    @Test
    void testVisitLogicalUpdateLeft() {

        SubSelect subSelect = new SubSelect();
        String subqueryStr = subSelect.toString();

        OrExpression orExpression = new OrExpression(subSelect, new NullValue());

        AndExpression andExpression = new AndExpression(orExpression, new NullValue());

        SubqueryRemover subqueryRemover = new SubqueryRemover(subqueryStr);
        andExpression.accept(subqueryRemover);

        assertThat(subqueryRemover.isUpdateChild()).isFalse();
    }

    /**
     * Verifies that {@code SubqueryRemover#visitLogicalOperator(BinaryExpression)} correctly updates the
     * {@link SubqueryRemover} if the right subexpression of the logical operator that is being visited must be updated.
     */
    @Test
    void testVisitLogicalUpdateRight() {

        SubSelect subSelect = new SubSelect();
        String subqueryStr = subSelect.toString();

        AndExpression andExpression = new AndExpression(subSelect, new NullValue());

        OrExpression orExpression = new OrExpression(new NullValue(), andExpression);

        SubqueryRemover subqueryRemover = new SubqueryRemover(subqueryStr);
        orExpression.accept(subqueryRemover);

        assertThat(subqueryRemover.isUpdateChild()).isFalse();
    }
}
