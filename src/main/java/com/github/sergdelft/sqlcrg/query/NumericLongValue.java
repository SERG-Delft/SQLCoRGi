package com.github.sergdelft.sqlcrg.query;

import net.sf.jsqlparser.expression.LongValue;

/**
 * Extension of the {@link LongValue} class to allow for uniform treatment with other numeric types.
 */
public class NumericLongValue extends LongValue implements NumericValue {

    /**
     * Creates a new {@code NumericLongValue} instance, which is equivalent to its {@link LongValue} counterpart,
     * but provides additional functionality.
     *
     * @param value a string representing the value of the long.
     */
    public NumericLongValue(String value) {
        super(value);
    }

    @Override
    public NumericValue add(int number) {
        return new NumericLongValue(Long.toString(this.getValue() + number));
    }
}
