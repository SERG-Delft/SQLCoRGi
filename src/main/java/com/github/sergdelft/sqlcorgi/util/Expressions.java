package com.github.sergdelft.sqlcorgi.util;

import com.github.sergdelft.sqlcorgi.util.cloner.ExpressionCloner;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.statement.select.Join;

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

    /**
     * Changes the given {@link Join} to an inner join.
     *
     * @param join the {@code Join} that has to become an inner join.
     */
    public static void setJoinToInner(Join join) {
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
