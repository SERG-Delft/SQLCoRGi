package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
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

import java.util.List;

public class NullColumnExclusionVisitor extends ExpressionVisitorAdapter {
    private List<Column> columns;
    private Expression expression;

    @Override
    public void visit(AndExpression andExpression) {
        handleBinaryExpression(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        handleBinaryExpression(orExpression);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        handleComparisonOperator(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {

    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {

    }

    @Override
    public void visit(MinorThan minorThan) {

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {

    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {

    }

    @Override
    public void visit(Column column) {
        if (contains(column)) {
            System.out.println("MUST EXCLUDE!");
            expression = null;
        }
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    private boolean contains(Column column) {
        for (Column c : columns) {
            if (c.toString().toLowerCase().equals(column.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Expression handleBinaryExpression(BinaryExpression binaryExpression) {
        Expression out;
        expression = binaryExpression.getLeftExpression();
        expression.accept(this);

        binaryExpression.setLeftExpression(expression);

        out = expression;

        expression = binaryExpression.getRightExpression();
        expression.accept(this);

        binaryExpression.setRightExpression(expression);

        if (expression != null) {
            out = expression;
        }

        if (binaryExpression.getRightExpression() != null && binaryExpression.getLeftExpression()!= null) {
            System.out.println(binaryExpression);
            return binaryExpression;
        }

        System.out.println(out + "\t" + columns);
        return out;
    }

    private Expression handleComparisonOperator(ComparisonOperator comparisonOperator) {
        Expression left = comparisonOperator.getLeftExpression();
        Expression right = comparisonOperator.getRightExpression();

        if (checkSideComparisonOperator(left) || checkSideComparisonOperator(right)) {
            return null;
        }

        return comparisonOperator;

    }

    private boolean checkSideComparisonOperator(Expression expression) {
        expression.accept(this);

        return expression == null;
    }



}
