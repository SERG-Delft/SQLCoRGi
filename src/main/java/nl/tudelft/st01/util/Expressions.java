package nl.tudelft.st01.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

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
     * Creates a deep copy of an {@link Expression}. This can be useful if you need to modify part of an expression,
     * but other parts of your code need to use the unmodified expression.
     *
     * @param expression the expression that needs to be copied.
     * @return a copy of {@code expression}.
     */
    // Justification: We do not want to print the stack trace of JSQLParser here.
    @SuppressWarnings("PMD.PreserveStackTrace")
    public static Expression copy(Expression expression) {

        try {
            return CCJSqlParserUtil.parseCondExpression(expression.toString(), false);
        } catch (JSQLParserException e) {
            throw new IllegalStateException("Could not copy expression: " + expression);
        }
    }

    /**
     * Creates an {@link EqualsTo} instance. The supplied left and right expressions are copied.
     *
     * @param leftExpression the left side of the expression.
     * @param rightExpression the right side of the expression.
     * @return a new {@link }EqualsTo` expression.
     */
    public static EqualsTo createEqualsTo(Expression leftExpression, Expression rightExpression) {
        EqualsTo equalsExpression = new EqualsTo();
        equalsExpression.setLeftExpression(copy(leftExpression));
        equalsExpression.setRightExpression(copy(rightExpression));

        return equalsExpression;
    }

}
