package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;

import java.util.List;

public class RuleGeneratorOnExpressionVisitor extends ExpressionVisitorAdapter {
    private List<Expression> output;

    @Override
    public void visit(AndExpression andExpression) {
        Expression l = andExpression.getLeftExpression();
        Expression r = andExpression.getRightExpression();

    }

    @Override
    public void visit(OrExpression orExpression) {
        Expression l = orExpression.getLeftExpression();
        Expression r = orExpression.getRightExpression();
        System.out.println("HERE OR " + l.toString() + r.toString());

    }

    @Override
    public void visit(EqualsTo equalsTo) {
        Expression l = equalsTo.getLeftExpression();
        Expression r = equalsTo.getRightExpression();
        generateRules(l, r);
        System.out.println("HERE " + l.toString() + r.toString());

    }

    @Override
    public void visit(GreaterThan greaterThan) {
        Expression l = greaterThan.getLeftExpression();
        Expression r = greaterThan.getRightExpression();
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        Expression l = greaterThanEquals.getLeftExpression();
        Expression r = greaterThanEquals.getRightExpression();
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        Expression l = isNullExpression.getLeftExpression();
    }

    @Override
    public void visit(MinorThan minorThan) {
        Expression l = minorThan.getLeftExpression();
        Expression r = minorThan.getRightExpression();
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        Expression l = minorThanEquals.getLeftExpression();
        Expression r = minorThanEquals.getRightExpression();
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        Expression l = notEqualsTo.getLeftExpression();
        Expression r = notEqualsTo.getRightExpression();
    }



    private void generateRules(Expression left, Expression right) {
        System.out.println(left.getClass().toString());
        if (!(left instanceof  Expression)) {
            System.out.println("HERE COLUMN");

        } else {
            left.accept(this);
        }

        if (!(right instanceof  Expression)) {
            System.out.println("HERE COLUMN");
        } else {
            right.accept(this);
        }


    }

}
