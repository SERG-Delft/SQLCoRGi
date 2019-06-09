package nl.tudelft.st01.visitors.select;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.Collection;
import java.util.Iterator;

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

    /**
     * Checks whether the left or right expressions of a given logical operator have been updated or removed.
     * Updates the left and right expressions if necessary, or instructs the parent of the logical operator
     * either to replace it with its left or right expression or to remove it altogether.
     *
     * @param expr the logical operator to check, i.e. an AND or OR operator.
     */
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
    public void visit(CaseExpression caseExpression) {

        Expression switchExpression = caseExpression.getSwitchExpression();
        if (switchExpression != null) {
            switchExpression.accept(this);
            if (this.updateChild) {
                this.child = null;
                return;
            }
        }

        Expression elseExpression = caseExpression.getElseExpression();
        if (elseExpression != null) {
            elseExpression.accept(this);
            if (this.updateChild) {
                caseExpression.setElseExpression(this.child);
                this.updateChild = false;
            }
        }

        Iterator<WhenClause> iterator = caseExpression.getWhenClauses().iterator();
        while (iterator.hasNext()) {
            WhenClause whenClause = iterator.next();
            whenClause.accept(this);
            if (this.updateChild) {
                iterator.remove();
                this.updateChild = false;
            }
        }

        if (caseExpression.getWhenClauses().isEmpty()) {
            this.child = null;
            this.updateChild = true;
        }
    }

    @Override
    public void visit(WhenClause expr) {

        expr.getWhenExpression().accept(this);
        if (this.updateChild) {
            if (this.child == null) {
                return;
            } else {
                expr.setWhenExpression(this.child);
                this.updateChild = false;
            }
        }

        expr.getThenExpression().accept(this);
    }

}
