package nl.tudelft.st01.visitors.select;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

import java.util.Collection;

/**
 * A visitor that performs a null reduction transformation given a list of attributes and an expression.
 */
public class NullReducer extends ExpressionVisitorAdapter {

    private final Collection<String> nulls;
    private Expression child;
    private boolean updateChild;

    /**
     * Creates a new {@link NullReducer} that removes all conditions containing attributes in the provided collection.
     *
     * @param nulls the collection of attributes that need to be removed from the expression.
     */
    public NullReducer(Collection<String> nulls) {
        this.nulls = nulls;
        this.child = null;
        this.updateChild = false;
    }

    /**
     * Returns the root expression of the provided expression. Note that the provided expression must be the expression
     * that accepted an instance of the visitor.
     *
     * @param original the expression for which the root must be returned.
     * @return a new root expression, or the provided expression if the root has not changed.
     */
    public Expression getRoot(Expression original) {

        if (this.updateChild) {
            return this.child;
        }
        return original;
    }

    private void visitLogicalOperator(BinaryExpression expr) {

        Expression left = expr.getLeftExpression();
        Expression right = expr.getRightExpression();

        left.accept(this);

        // left is updated or removed
        if (this.updateChild) {
            this.updateChild = false;

            // left is removed
            if (this.child == null) {
                right.accept(this);

                // right is unchanged, tell parent to replace self with right
                if (!this.updateChild) {
                    this.child = right;
                    this.updateChild = true;
                }
                // else this.updateChild and right sets this.child
                return;
            } else {
                // left is updated
                expr.setLeftExpression(this.child);
            }
        }

        // left is not removed, now check right
        right.accept(this);

        // right is updated or removed
        if (this.updateChild) {

            // right is removed
            if (this.child == null) {
                this.child = expr.getLeftExpression();
            } else {
                expr.setRightExpression(this.child);
                this.updateChild = false;
            }
        }
    }


    /**
     * Checks whether the children of this {@link BinaryExpression} have been removed. If they have been removed, signal
     * the parent of this binary expression to remove this child.
     *
     * @param expr the binary expression to check.
     */
    @Override
    protected void visitBinaryExpression(BinaryExpression expr) {
        expr.getLeftExpression().accept(this);
        if (this.updateChild) {
            this.child = null;
            return;
        }
        expr.getRightExpression().accept(this);
        if (this.updateChild) {
            this.child = null;
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
    public void visit(IsNullExpression isNull) {
        if (isNull.isNot()) {
            isNull.getLeftExpression().accept(this);
        }
    }

    @Override
    public void visit(Column column) {
        if (nulls.contains(column.toString())) {
            this.child = null;
            this.updateChild = true;
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        // TODO: Stop here?
    }

    @Override
    public void visit(CaseExpression expr) {
        // TODO: Remove WHEN clauses from the CASE expression that cannot be satisfied, or delete whole CASE
        // expression if no WHEN clauses are left.
    }

    @Override
    public void visit(WhenClause expr) {
        // TODO: Remove WHEN clauses that cannot be satisfied
    }

}
