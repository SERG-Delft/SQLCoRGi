package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
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

    /**
     * Generates subexpressions and their combinations for {@link OrExpression}s and {@link AndExpression}d.
     * @param expression an {@link OrExpression} or {@link AndExpression}.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void generateSubExpressions(BinaryExpression expression) {

        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        List<Expression> leftOut = new ArrayList<>();
        List<Expression> rightOut = new ArrayList<>();
        List<Expression> temp = this.output;

        this.output = leftOut;
        left.accept(this);

        this.output = rightOut;
        right.accept(this);

        this.output = temp;

        Expression neutralExpression = right instanceof Parenthesis ? right : new Parenthesis(right);
        if (expression instanceof OrExpression) {
            neutralExpression = new NotExpression(neutralExpression);
        }
        for (Expression decisionExpression : leftOut) {
            this.output.add(new AndExpression(new Parenthesis(decisionExpression), neutralExpression));
        }

        neutralExpression = left instanceof Parenthesis ? left : new Parenthesis(left);
        if (expression instanceof OrExpression) {
            neutralExpression = new NotExpression(neutralExpression);
        }
        for (Expression decisionExpression : rightOut) {
            this.output.add(new AndExpression(neutralExpression, new Parenthesis(decisionExpression)));
        }

        /*try {
            System.out.println(CCJSqlParserUtil.parseCondExpression("a2 = 30"));
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void visit(AndExpression andExpression) {
        generateSubExpressions(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        generateSubExpressions(orExpression);
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
