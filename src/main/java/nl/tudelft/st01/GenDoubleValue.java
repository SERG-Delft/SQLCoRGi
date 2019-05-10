package nl.tudelft.st01;

import net.sf.jsqlparser.expression.DoubleValue;

/**
 * Helper class used to allow for a uniform way of modifying {@link NumericExpression}s.
 */
public class GenDoubleValue extends DoubleValue implements NumericExpression {

    /**
     * Creates a new instance.
     * @param value the value of the DoubleValue object.
     */
    public GenDoubleValue(String value) {
        super(value);
    }

    @Override
    public NumericExpression add(int number) {
        return new GenDoubleValue(Double.toString(this.getValue() + number));
    }
}
