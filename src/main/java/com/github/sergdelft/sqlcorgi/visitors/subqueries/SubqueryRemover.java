package com.github.sergdelft.sqlcorgi.visitors.subqueries;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * This visitor removes subqueries from an {@link Expression} and repairs it.
 */
public class SubqueryRemover extends ExpressionVisitorAdapter {

    private Expression child;

    private boolean updateChild;
    private String subquery;

    /**
     * Creates a new instance that will remove the provided subquery from the {@link Expression}s it visits.
     *
     * @param subquery a string representation of the subquery that needs to be removed.
     */
    public SubqueryRemover(String subquery) {
        this.subquery = subquery;
        this.child = null;
        this.updateChild = false;
    }

    /**
     * Removes or repairs AND and OR operators.
     *
     * @param binaryExpression the logical operator to visit.
     */
    private void visitLogicalOperator(BinaryExpression binaryExpression) {

        Expression right = binaryExpression.getRightExpression();
        binaryExpression.getLeftExpression().accept(this);
        if (this.updateChild) {
            this.updateChild = false;
            if (this.child != null) {
                binaryExpression.setLeftExpression(this.child);
            } else {
                right.accept(this);

                if (!this.updateChild) {
                    this.child = right;
                    this.updateChild = true;
                }
                return;
            }
        }

        right.accept(this);
        if (this.updateChild) {
            if (this.child == null) {
                this.child = binaryExpression.getLeftExpression();
            } else {
                binaryExpression.setRightExpression(this.child);
                this.updateChild = false;
            }
        }
    }

    @Override
    public void visit(SubSelect subSelect) {

        if (subSelect.toString().equals(subquery)) {
            this.child = null;
            this.updateChild = true;
        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitLogicalOperator(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitLogicalOperator(orExpression);
    }

    @Override
    public void visit(AllComparisonExpression expr) {
        expr.getSubSelect().accept((ExpressionVisitor) this);
    }

    @Override
    public void visit(AnyComparisonExpression expr) {
        expr.getSubSelect().accept((ExpressionVisitor) this);
    }

    public Expression getChild() {
        return child;
    }

    public boolean isUpdateChild() {
        return updateChild;
    }

}
