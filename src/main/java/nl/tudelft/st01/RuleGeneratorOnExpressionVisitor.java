package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

import java.util.Set;

public class RuleGeneratorOnExpressionVisitor extends ExpressionVisitorAdapter {
    private Set<Expression> output;

    @Override
    public void visit(AndExpression andExpression) {
        generateRules(andExpression);

    }

    @Override
    public void visit(OrExpression orExpression) {
        generateRules(orExpression);

    }

    @Override
    public void visit(EqualsTo equalsTo) {
        generateRules(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        generateRules(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        generateRules(greaterThanEquals);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        Expression l = isNullExpression.getLeftExpression();
    }

    @Override
    public void visit(MinorThan minorThan) {
        generateRules(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        generateRules(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        generateRules(notEqualsTo);
    }



    private void generateRules(BinaryExpression binaryExpression) {
        Expression left = binaryExpression.getLeftExpression();
        Expression right = binaryExpression.getRightExpression();


        System.out.println(binaryExpression.toString());

        if (left instanceof Column | left instanceof LongValue | left instanceof DoubleValue) {
            output.add(left);
        } else {
            left.accept(this);
        }

        if (right instanceof Column | right instanceof LongValue | right instanceof DoubleValue) {
            output.add(right);
        } else {
            right.accept(this);
        }

        //output = null;

    }

    public void setOutput(Set list) {
        this.output = list;
    }

}
