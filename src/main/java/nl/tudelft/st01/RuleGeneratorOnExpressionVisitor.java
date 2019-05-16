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
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleGeneratorOnExpressionVisitor extends ExpressionVisitorAdapter {
    private List<Expression> output;
    private List<Expression> terminals = new ArrayList<>();
    private Map<String, List<Expression>> hashMap = new HashMap();

    @Override
    public void visit(AndExpression andExpression) {
        getTerminalsOnCondition(andExpression);

    }

    @Override
    public void visit(OrExpression orExpression) {
        getTerminalsOnCondition(orExpression);

    }

    @Override
    public void visit(EqualsTo equalsTo) {
        getTerminalsOnCondition(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        getTerminalsOnCondition(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        getTerminalsOnCondition(greaterThanEquals);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        Expression l = isNullExpression.getLeftExpression();
    }

    @Override
    public void visit(MinorThan minorThan) {
        getTerminalsOnCondition(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        getTerminalsOnCondition(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        getTerminalsOnCondition(notEqualsTo);
    }


    /**
     * Retrieves the columns and values used in the on expressions.
     * @param binaryExpression binary expression.
     */
    private void getTerminalsOnCondition(BinaryExpression binaryExpression) {
        Expression left = binaryExpression.getLeftExpression();
        Expression right = binaryExpression.getRightExpression();

        if (left instanceof Column | left instanceof LongValue | left instanceof DoubleValue) {
            if (left instanceof Column) {
                Table table = ((Column) left).getTable();

            }

            //if (!contains(terminals, left)) {
                terminals.add(left);
           // }
        } else {
            left.accept(this);
        }

        if (right instanceof Column | right instanceof LongValue | right instanceof DoubleValue) {
            //if (!contains(terminals, right)) {
                terminals.add(right);
           // }
        } else {
            right.accept(this);
        }

        //terminals = null;

    }

    /**
     * Generates the WHERE conditions that should be appended to the original statement.
     * Note that the context of the statement must be known in order to identify the keys.
     */
    public void generateExpressions(PlainSelect plainSelect) {

    }

    private Expression createInnerJoinExpression(Expression e) {
        return e;
    }

    private Expression createLeftJoinExpression(Expression e) {
        return e;
    }

    private Expression createRightJoinExpression(Expression e) {
        return e;
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
