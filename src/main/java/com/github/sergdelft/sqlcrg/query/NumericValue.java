package com.github.sergdelft.sqlcrg.query;

import net.sf.jsqlparser.expression.Expression;

/**
 * Provides a uniform way of accessing and modifying instances of implementing classes.
 */
public interface NumericValue extends Expression {

    /**
     * Adds an integer to the value of this {@code NumericValue}.
     *
     * @param number the integer to be added.
     * @return a new instance of the value, representing the value resulting from the addition.
     */
    NumericValue add(int number);

}
