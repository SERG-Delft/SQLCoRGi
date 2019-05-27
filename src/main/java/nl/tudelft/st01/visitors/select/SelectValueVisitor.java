package nl.tudelft.st01.visitors.select;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import nl.tudelft.st01.query.NumericDoubleValue;
import nl.tudelft.st01.query.NumericExpression;
import nl.tudelft.st01.query.NumericLongValue;

import java.util.List;

/**
 * Custom visitor for the values of SELECT statements. The type of value determines what kind of cases need to be
 * generated.
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
            throw new IllegalArgumentException(
                    "A SelectValueVisitor requires an empty, non-null set to which it can write generated mutations."
            );
        }

        this.column = column;
        this.output = output;
    }

    /**
     * Generates modified conditions for numeric values.
     * @param numericExpression the numeric value taken from the original expression.
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.UnusedPrivateMethod"})
    private void generateNumericCases(NumericExpression numericExpression) {
        for (int i = -1; i <= 1; ++i) {
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(column);
            equalsTo.setRightExpression(numericExpression.add(i));
            output.add(equalsTo);
        }

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(column);
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
        equalsTo.setLeftExpression(column);
        equalsTo.setRightExpression(stringValue);
        output.add(equalsTo);

        NotEqualsTo notEqualTo = new NotEqualsTo();
        notEqualTo.setLeftExpression(column);
        notEqualTo.setRightExpression(stringValue);
        output.add(notEqualTo);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(column);
        output.add(isNullExpression);
    }

}
