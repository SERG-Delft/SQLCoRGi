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

import java.util.List;

public class RuleGeneratorOnExpressionVisitor extends ExpressionVisitorAdapter {
    private List<Expression> output;

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


    /**
     * Generate rules for binary expressions.
     * @param binaryExpression binary expression.
     */
    private void generateRules(BinaryExpression binaryExpression) {
        Expression left = binaryExpression.getLeftExpression();
        Expression right = binaryExpression.getRightExpression();


        System.out.println(binaryExpression.toString());

        if (left instanceof Column | left instanceof LongValue | left instanceof DoubleValue) {
            if (!contains(output, left)) {
                output.add(left);
            }

        } else {
            left.accept(this);
        }

        if (right instanceof Column | right instanceof LongValue | right instanceof DoubleValue) {
            if (!contains(output, right)) {
                output.add(right);
            }
        } else {
            right.accept(this);
        }

        //output = null;

    }

    public void setOutput(List list) {
        this.output = list;
    }


    /**
     * Ensures that the list of columns used in the on expression contains no duplicate column names.
     * NOTE: Suppose the column name "id" in "Movies" and the column name is unique.
     * Yet, the function considers Movies.id to be different from id.
     * NEED REPLACEMENT BY SET. Customization equals method needed.
     * @param list list
     * @param expression e
     * @return boolean
     */
    private static boolean contains(List<Expression> list, Expression expression) {
        if (list == null) {
            return false;
        }
        for (Expression e : list) {
            if (e.toString().toLowerCase().equals(expression.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
