package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
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
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Visitor allows for extracting the column used in the given expression.
 */
public class RuleGeneratorOnExpressionVisitor extends ExpressionVisitorAdapter {

    private Map<String, List<Expression>> output = new HashMap();

    @Override
    public void visit(AndExpression andExpression) {
        getColumnsOnCondition(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        getColumnsOnCondition(orExpression);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        getColumnsOnCondition(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        getColumnsOnCondition(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        getColumnsOnCondition(greaterThanEquals);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        isNullExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(MinorThan minorThan) {
        getColumnsOnCondition(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        getColumnsOnCondition(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        getColumnsOnCondition(notEqualsTo);
    }

    @Override
    public void visit(Column column) {
        updateColumnList(column);
    }

    /**
     * Retrieves the columns and values used in the on expressions.
     * @param binaryExpression binary expression.
     */
    private void getColumnsOnCondition(BinaryExpression binaryExpression) {
        Expression left = binaryExpression.getLeftExpression();
        Expression right = binaryExpression.getRightExpression();

        left.accept(this);
        right.accept(this);
    }

    /**
     * Stores each column corresponding to its table.
     * @param e The column that should be added.
     */
    private void updateColumnList(Column e) {
        String table = e.getTable().toString().toLowerCase();

        if (!output.containsKey(table)) {
            List<Expression> list = new ArrayList<>();
            list.add(e);
            output.put(table, list);
        } else if (!contains(output.get(table), e)) {
            output.get(table).add(e);
        }
    }



    public void setOutput(Map map) {
        this.output = map;
    }

    /**
     * Ensures that the list of columns used in the on expression contains no duplicate column names.
     * NOTE: Suppose the column name "id" in "Movies" and the column name is unique.
     * Yet, the function considers Movies.id to be different from id.
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