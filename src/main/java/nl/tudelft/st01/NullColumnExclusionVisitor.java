package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
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
    private List<Column> nullColumns;
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
        handleComparisonOperator(greaterThanEquals);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        /*  If the column is in the null columns list, then either
            a) It is an IS NULL expression, leading to a duplicate.
            b) It is an IS NOT NULL expression, leading to a contradiction.
            Both must be excluded.
            If the column is not in the null columns list, then the column must not be null.
            If it is an IS NULL expression, then there is a contradiction, so it should be excluded.
         */
        if (contains((Column) isNullExpression.getLeftExpression())) {
            expression = null;
        } else if (!isNullExpression.isNot()){
            expression = null;
        } else {
            expression = isNullExpression;
        }
    }

    @Override
    public void visit(MinorThan minorThan) {
        handleComparisonOperator(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        handleComparisonOperator(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        handleComparisonOperator(notEqualsTo);
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        expression = doubleValue;
    }

    @Override
    public void visit(LongValue longValue) {
        expression = longValue;
    }

    @Override
    public void visit(Column column) {
        if (contains(column)) {
            expression = null;
        } else {
            expression = column;
        }
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    public void setNullColumns(List<Column> nullColumns) {
        this.nullColumns = nullColumns;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    private boolean contains(Column column) {
        for (Column c : nullColumns) {
            if (c.toString().toLowerCase().equals(column.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Expression handleBinaryExpression(BinaryExpression binaryExpression) {
        Expression left;
        Expression right;

        expression = binaryExpression.getLeftExpression();
        expression.accept(this);

        left = expression;

        binaryExpression.setLeftExpression(left);

        expression = binaryExpression.getRightExpression();
        expression.accept(this);

        right = expression;

        if (left != null) {
            if (right != null) {
                expression = binaryExpression;
            } else {
                expression = left;
            }
        }

        return expression;
    }

    private Expression handleComparisonOperator(ComparisonOperator comparisonOperator) {
        //expression;
        Expression left = comparisonOperator.getLeftExpression();
        Expression right = comparisonOperator.getRightExpression();

        if (checkSideComparisonOperator(left) || checkSideComparisonOperator(right)) {
            System.out.println("EXCLUDED: " + comparisonOperator);
            expression = null;
            return null;

        }
        System.out.println("PASSED: " + comparisonOperator);
        expression = comparisonOperator;
        return comparisonOperator;

    }

    private boolean checkSideComparisonOperator(Expression expression) {
        expression.accept(this);

        return this.expression == null;
    }



}
