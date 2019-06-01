package nl.tudelft.st01.util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import nl.tudelft.st01.util.cloner.ExpressionCloner;

/**
 * Provides utility functions for JSQLParser query objects.
 */
public final class Expressions {

    /**
     * No instances of this class can be created.
     */
    private Expressions() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates an {@link EqualsTo} instance. The supplied left and right expressions are copied.
     *
     * @param leftExpression the left side of the expression.
     * @param rightExpression the right side of the expression.
     * @return a new {@link EqualsTo} expression.
     */
    public static EqualsTo createEqualsTo(Expression leftExpression, Expression rightExpression) {
        EqualsTo equalsExpression = new EqualsTo();
        equalsExpression.setLeftExpression(ExpressionCloner.copy(leftExpression));
        equalsExpression.setRightExpression(ExpressionCloner.copy(rightExpression));

        return equalsExpression;
    }

}
