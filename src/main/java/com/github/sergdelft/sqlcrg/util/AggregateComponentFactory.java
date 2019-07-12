package com.github.sergdelft.sqlcrg.util;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;

/**
 * Utility class that can be used to quickly create statements with COUNT(*), COUNT(DISTINCT __).
 */
public final class AggregateComponentFactory {

    public static final String COUNT_STRING = "COUNT";

    /**
     * No instance of this class should be created.
     */
    private AggregateComponentFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates a `__ GREATER THAN 1` expression.
     *
     * @param expr expression to fill in the __
     * @return `expr GREATER THAN 1` object
     */
    public static GreaterThan createGreaterThanOne(Expression expr) {
        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(expr);
        greaterThan.setRightExpression(new DoubleValue("1"));

        return greaterThan;
    }

    /**
     * Generates a COUNT(*) object.
     *
     * @return a COUNT(*) object
     */
    public static Function createCountAllColumns() {
        Function count = new Function();
        count.setName(COUNT_STRING);
        count.setAllColumns(true);

        return count;
    }

    /**
     * Generates a COUNT(DISTINCT __) object.
     *
     * @param expression expression to fill in the __
     * @param distinct toggles whether or not you want to include DISTINCT
     * @return a COUNT(DISTINCT __) object
     */
    public static Function createCountColumn(Expression expression, boolean distinct) {
        Function countColumn = new Function();
        countColumn.setName(COUNT_STRING);
        ExpressionList parameters = new ExpressionList(expression);
        countColumn.setParameters(parameters);
        countColumn.setDistinct(distinct);

        return countColumn;
    }
}
