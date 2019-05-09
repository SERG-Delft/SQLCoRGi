package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;

/**
 * Allows implementations to be mutated in a uniform way.
 */
public interface NumericExpression extends Expression {

    /**
     * Add number to the value implementing this interface.
     * @param number the number to be added.
     * @return a new instance of the expression, representing the value resulting from the addition.
     */
    NumericExpression add(int number);

}
