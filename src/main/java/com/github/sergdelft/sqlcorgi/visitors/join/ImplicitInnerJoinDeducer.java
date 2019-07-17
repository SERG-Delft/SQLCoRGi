package com.github.sergdelft.sqlcorgi.visitors.join;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.List;


/**
 * This class is used to deduce whether the input query contains an implicit inner join. If so, it is converted to
 * an inner join. The joins list and the where expression are updated accordingly.
 */
public class ImplicitInnerJoinDeducer extends ExpressionVisitorAdapter {
    private String rightTable;
    //private JoinWhereItem output;
    private boolean update;
    private FromItem fromItem;
    private Join join;
    private List<Join> joins;

    public ImplicitInnerJoinDeducer(Join join, FromItem fromItem, List<Join> joins, JoinWhereItem output) {
        this.rightTable = join.getRightItem().toString().toLowerCase();
        this.fromItem = fromItem;
        this.join = join;
        output.getJoin();
        this.joins = joins;
        update = false;
    }

    @Override
    public void visit(AndExpression andExpression) {
        handleExpressionLogical(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        handleExpressionLogical(orExpression);
    }

    @Override
    public void visit(SubSelect subSelect) {

    }

    @Override
    public void visit(EqualsTo equalsTo) {
        if (!foundImplicit && equalsTo.getLeftExpression() instanceof Column && equalsTo.getRightExpression() instanceof Column) {
            Column left = (Column) equalsTo.getLeftExpression();
            Column right = (Column) equalsTo.getRightExpression();

            checkImplicitJoin(left, right);
            foundImplicit = true;
        }
    }

    /**
     * Checks whether the given two columns are related to the implicit inner join.
     * @param left The right column of the expression.
     * @param right The left column in the expression.
     * @return True if there is an implicit inner join, false otherwise.
     */
    private boolean checkImplicitJoin(Column left, Column right) {
        Table t1;
        Table t2;
        // TODO: Alias stuff
        t1 = left.getTable();
        t2 = right.getTable();

        String t1String = t1.toString().toLowerCase();
        String t2String = t2.toString().toLowerCase();
        String leftString = null;

        if (t1String.equals(rightTable)) {
            leftString = t2.toString().toLowerCase();
        } else if (t2String.toLowerCase().equals(rightTable)) {
            leftString = t1.toString().toLowerCase();
        }

        if (leftString != null) {
            String jString;
            String fromString = fromItem.toString().toLowerCase();
            if (leftString.equals(fromString) && !rightTable.equals(fromString)) {
                join.setInner(true);
                join.setSimple(false);

                EqualsTo expression = new EqualsTo();
                expression.setRightExpression(right);
                expression.setLeftExpression(left);

                join.setOnExpression(expression);
                update = true;
            }

            if (!update) {
                for (Join j : joins) {
                    jString = j.getRightItem().toString().toLowerCase();
                    if (j.isSimple() && (leftString.equals(jString) || rightTable.equals(jString))) {
                        j.setSimple(false);
                        j.setInner(true);

                        EqualsTo expression = new EqualsTo();
                        expression.setRightExpression(right);
                        expression.setLeftExpression(left);

                        j.setOnExpression(expression);
                        update = true;
                        break;
                    }
                }
            }
        }

        return update;
    }

    public Expression getExpression() {
        return expression;
    }

    public List<Join> getJoins() {
        return joins;
    }
}