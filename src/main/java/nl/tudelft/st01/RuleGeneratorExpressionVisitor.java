package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;

import java.util.List;

/**
 * Custom visitor for expressions, such as WHERE clauses in SELECT statements.
 */
public class RuleGeneratorExpressionVisitor extends ExpressionVisitorAdapter {

    private List<Expression> output;

    /**
     * Generates modified conditions from a simple comparison.
     * @param comparisonOperator the comparison operator to generate the conditions from.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void generateSimpleComparison(ComparisonOperator comparisonOperator) {

        Expression rightExpression = comparisonOperator.getRightExpression();
        System.out.println(comparisonOperator.getRightExpression().getClass().getName().split("\\.")[4]);

        switch (comparisonOperator.getRightExpression().getClass().getName().split("\\.")[4]) {
            case "LongValue":
                // TODO: Add support for other data types than integers
                LongValue longValue = (LongValue) rightExpression;
                for (int i = -1; i < 2; ++i) {
                    EqualsTo equalsTo = new EqualsTo();
                    equalsTo.setLeftExpression(comparisonOperator.getLeftExpression());
                    equalsTo.setRightExpression(new LongValue(longValue.getValue() + i));
                    output.add(equalsTo);
                }


                // TODO: Add some way to pass along information about nullable attributes, and check for it.
                IsNullExpression isNullExpressionLong = new IsNullExpression();
                isNullExpressionLong.setLeftExpression(comparisonOperator.getLeftExpression());
                output.add(isNullExpressionLong);
                break;
            case "StringValue":
                StringValue stringValue = (StringValue) rightExpression;
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(comparisonOperator.getLeftExpression());
                equalsTo.setRightExpression(comparisonOperator.getRightExpression());
                output.add(equalsTo);

                EqualsTo notEqualsTo = new EqualsTo();
                notEqualsTo.setLeftExpression(comparisonOperator.getLeftExpression());
                notEqualsTo.setRightExpression(comparisonOperator.getRightExpression());
                notEqualsTo.setNot();
                output.add(notEqualsTo);

                // TODO: Add some way to pass along information about nullable attributes, and check for it.
                IsNullExpression isNullExpressionString = new IsNullExpression();
                isNullExpressionString.setLeftExpression(comparisonOperator.getLeftExpression());
                output.add(isNullExpressionString);
                break;
            default:
                System.out.println("Class could not be found or is not yet implemented");
        }
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        generateSimpleComparison(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        generateSimpleComparison(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        generateSimpleComparison(greaterThanEquals);
    }

    @Override
    public void visit(MinorThan minorThan) {
        generateSimpleComparison(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        generateSimpleComparison(minorThanEquals);
    }

    public void setOutput(List<Expression> output) {
        this.output = output;
    }
}
