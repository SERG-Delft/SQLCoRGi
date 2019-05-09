package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
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
    private void generateSimpleComparison(ComparisonOperator comparisonOperator) {

        RuleGeneratorValueVisitor valueVisitor = new RuleGeneratorValueVisitor();
        ArrayList<Expression> cases = new ArrayList<>();
        valueVisitor.setColumn((Column) comparisonOperator.getLeftExpression());
        valueVisitor.setOutput(cases);
        comparisonOperator.getRightExpression().accept(valueVisitor);

        output.addAll(cases);
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
    public void visit(IsNullExpression isNullExpression) {

        IsNullExpression isNullExpressionOut = new IsNullExpression();
        isNullExpressionOut.setLeftExpression(isNullExpression.getLeftExpression());
        isNullExpressionOut.setNot(isNullExpression.isNot());
        output.add(isNullExpressionOut);

        IsNullExpression isNullExpressionToggled = new IsNullExpression();
        isNullExpressionToggled.setLeftExpression(isNullExpression.getLeftExpression());
        isNullExpressionToggled.setNot(!isNullExpression.isNot());
        output.add(isNullExpressionToggled);
    }

    @Override
    public void visit(MinorThan minorThan) {
        generateSimpleComparison(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        generateSimpleComparison(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        generateSimpleComparison(notEqualsTo);
    }

    public void setOutput(List<Expression> output) {
        this.output = output;
    }
}
