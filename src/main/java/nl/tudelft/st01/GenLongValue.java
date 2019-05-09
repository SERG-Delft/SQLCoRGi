package nl.tudelft.st01;

import net.sf.jsqlparser.expression.LongValue;

/**
 * Helper class used to allow for a uniform way of modifying {@link NumericExpression}s.
 */
public class GenLongValue extends LongValue implements NumericExpression {

    /**
     * Creates a new instance.
     * @param value the value of the LongValue object.
     */
    public GenLongValue(String value) {
        super(value);
    }

    @Override
    public NumericExpression add(int number) {
        return new GenLongValue(Long.toString(this.getValue() + number));
    }
}
