package nl.tudelft.st01.query;

import net.sf.jsqlparser.expression.DoubleValue;

/**
 * Extension of the {@link DoubleValue} class to allow for uniform treatment with other numeric types.
 */
public class NumericDoubleValue extends DoubleValue implements NumericExpression {

    /**
     * Creates a new {@code NumericDoubleValue} instance, which is equivalent to its {@link DoubleValue} counterpart,
     * but provides additional functionality.
     * @param value a string representing the value of the double.
     */
    public NumericDoubleValue(String value) {
        super(value);
    }

    @Override
    public NumericExpression add(int number) {
        return new NumericDoubleValue(Double.toString(this.getValue() + number));
    }
}
