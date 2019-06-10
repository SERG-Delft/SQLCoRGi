package nl.tudelft.st01.visitors.select;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import nl.tudelft.st01.query.NumericDoubleValue;
import nl.tudelft.st01.query.NumericLongValue;
import nl.tudelft.st01.query.NumericValue;
import nl.tudelft.st01.util.exceptions.CanNotBeNullException;

import java.util.List;

import static nl.tudelft.st01.util.cloner.ExpressionCloner.copy;

/**
 * A visitor for values used in equality operators in SELECT expressions. The type of value determines what kind of
 * cases are generated.
 */
public class SelectValueVisitor extends ExpressionVisitorAdapter {

    private Column column;

    private List<Expression> output;

    /**
     * Creates a new visitor which can be used to generate mutations of values in select operators. Mutations are
     * written to {@code output};
     *
     * @param column the {@code Column} that is compared to the visited value.
     * @param output the set to which generated rules should be written. This set must not be null, and must be empty.
     */
    public SelectValueVisitor(Column column, List<Expression> output) {
        if (output == null || !output.isEmpty()) {
            throw new CanNotBeNullException(
                    "A SelectValueVisitor requires an empty, non-null set to which it can write generated mutations."
            );
        }

        this.column = column;
        this.output = output;
    }

    /**
     * Generates modified conditions for numeric values.
     *
     * @param numericValue the numeric value taken from the original expression.
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.UnusedPrivateMethod"})
    private void generateNumericCases(NumericValue numericValue) {
        for (int i = -1; i <= 1; ++i) {
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(copy(column));
            equalsTo.setRightExpression(numericValue.add(i));
            output.add(equalsTo);
        }

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(copy(column));
        output.add(isNullExpression);
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        generateNumericCases(new NumericDoubleValue(doubleValue.toString()));
    }

    @Override
    public void visit(LongValue longValue) {
        generateNumericCases(new NumericLongValue(longValue.toString()));
    }

    @Override
    public void visit(StringValue stringValue) {
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(copy(column));
        equalsTo.setRightExpression(copy(stringValue));
        output.add(equalsTo);

        NotEqualsTo notEqualTo = new NotEqualsTo();
        notEqualTo.setLeftExpression(copy(column));
        notEqualTo.setRightExpression(copy(stringValue));
        output.add(notEqualTo);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(copy(column));
        output.add(isNullExpression);
    }

}
