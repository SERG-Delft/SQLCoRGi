package nl.tudelft.st01.util.query;

import net.sf.jsqlparser.expression.Expression;

/**
 * Provides a uniform way of accessing and modifying instances of implementing classes.
 */
public interface NumericExpression extends Expression {

    /**
     * Adds an integer to the value of this NumericExpression.
     * @param number the integer to be added.
     * @return a new instance of the expression, representing the value resulting from the addition.
     */
    NumericExpression add(int number);

}
