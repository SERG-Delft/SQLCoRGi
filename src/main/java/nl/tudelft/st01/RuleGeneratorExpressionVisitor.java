package nl.tudelft.st01;

import net.sf.jsqlparser.expression.*;
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
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
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

        /* TODO: Use strings to create new objects instead of doing this manually, which is prone to errors
            and requires consistency and invariants (here: don't ever change (parts of) the original expression). */
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

    /**
     * Generates test queries for 'BETWEEN' expressions.
     * @param between a 'BETWEEN' expression.
     */
    @Override
    public void visit(Between between) {

        output.add(between);

        Expression left = between.getLeftExpression();
        Expression start = between.getBetweenExpressionStart();
        Expression end = between.getBetweenExpressionEnd();

        Between betweenFlipped = new Between();
        betweenFlipped.setLeftExpression(left);
        betweenFlipped.setBetweenExpressionStart(start);
        betweenFlipped.setBetweenExpressionEnd(end);
        betweenFlipped.setNot(!between.isNot());
        output.add(betweenFlipped);

        EqualsTo leftBoundaryOffTest = generateEqualsTo(left, start);
        output.add(leftBoundaryOffTest);

        if (start instanceof LongValue) {

            GenLongValue longValue = new GenLongValue(start.toString());
            EqualsTo leftBoundaryOnTest = generateEqualsTo(left, longValue.add(-1));
            output.add(leftBoundaryOnTest);

        } else if (start instanceof DoubleValue) {

            GenDoubleValue doubleValue = new GenDoubleValue(start.toString());
            EqualsTo leftBoundaryOnTest = generateEqualsTo(left, doubleValue.add(-1));
            output.add(leftBoundaryOnTest);

        }

        EqualsTo rightBoundaryOffTest = generateEqualsTo(left, end);
        output.add(rightBoundaryOffTest);

        if (end instanceof LongValue) {

            GenLongValue longValue = new GenLongValue(end.toString());
            EqualsTo rightBoundaryOnTest = generateEqualsTo(left, longValue.add(1));
            output.add(rightBoundaryOnTest);

        } else if (end instanceof DoubleValue) {

            GenDoubleValue doubleValue = new GenDoubleValue(end.toString());
            EqualsTo rightBoundaryOnTest = generateEqualsTo(left, doubleValue.add(1));
            output.add(rightBoundaryOnTest);

        }

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(left);
        output.add(isNullExpression);
    }

    /**
     * Generates test queries for 'IN' expressions.
     * @param inExpression an 'IN' expression.
     */
    @Override
    public void visit(InExpression inExpression) {

        output.add(inExpression);

        InExpression inExpressionFlipped = new InExpression();
        inExpressionFlipped.setLeftExpression(inExpression.getLeftExpression());
        inExpressionFlipped.setRightItemsList(inExpression.getRightItemsList());
        inExpressionFlipped.setNot(!inExpression.isNot());
        output.add(inExpressionFlipped);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(inExpression.getLeftExpression());
        output.add(isNullExpression);
    }

    /**
     * Generates test queries for 'LIKE' expressions.
     * @param likeExpression a LIKE expression.
     */
    @Override
    public void visit(LikeExpression likeExpression) {

        output.add(likeExpression);

        LikeExpression likeExpressionFlipped = new LikeExpression();
        likeExpressionFlipped.setLeftExpression(likeExpression.getLeftExpression());
        likeExpressionFlipped.setRightExpression(likeExpression.getRightExpression());

        // The LikeExpression class' setNot function does not accept any parameters, unlike others.
        // Therefore an if statement is used to check wether to create a NOT expression.
        if (!likeExpression.isNot()) {
            likeExpressionFlipped.setNot();
        }

        output.add(likeExpressionFlipped);

        IsNullExpression isNullExpressionOut = new IsNullExpression();
        isNullExpressionOut.setLeftExpression(likeExpression.getLeftExpression());
        output.add(isNullExpressionOut);
    }

    public void setOutput(List<Expression> output) {
        this.output = output;
    }

    /**
     * Generates an `EqualsTo` expression.
     * @param leftExpression the left side of the expression
     * @param rightExpression the right side of the expression
     * @return the generated `EqualsTo` expression.
     */
    private EqualsTo generateEqualsTo(Expression leftExpression, Expression rightExpression) {

        EqualsTo equalsExpression = new EqualsTo();
        equalsExpression.setLeftExpression(leftExpression);
        equalsExpression.setRightExpression(rightExpression);
        return equalsExpression;
    }
}
