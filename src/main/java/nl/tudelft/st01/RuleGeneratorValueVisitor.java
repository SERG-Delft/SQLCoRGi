package nl.tudelft.st01;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;

import java.util.List;

/**
 * Custom visitor for the values of SELECT statements. The type of value determines what kind of cases need to be
 * generated.
 */
public class RuleGeneratorValueVisitor extends ExpressionVisitorAdapter {

    private Column column;

    private List<Expression> output;

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
        generateNumericCases(new GenDoubleValue(doubleValue.toString()));
    }

    @Override
    public void visit(LongValue longValue) {
        generateNumericCases(new GenLongValue(longValue.toString()));
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public void setOutput(List<Expression> output) {
        this.output = output;
    }
}
