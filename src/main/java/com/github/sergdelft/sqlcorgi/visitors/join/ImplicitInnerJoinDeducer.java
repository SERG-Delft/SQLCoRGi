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

import java.util.LinkedList;
import java.util.List;


/**
 * This class is used to deduce whether the input query contains an implicit inner join. If so, it is converted to
 * an inner join. The joins list and the where expression are updated accordingly.
 */
public class ImplicitInnerJoinDeducer extends ExpressionVisitorAdapter {
    private String rightTable;
    private boolean update;
    private final FromItem fromItem;

    private boolean foundImplicit;

    private Join join;
    private List<Join> joins;
    private Expression expression;
    private List<String> linked;


    /**
     * Constructor.
     * @param join The join that is evaluated.
     * @param fromItem The from item of the from clause.
     * @param joins The list of joins in the from clause.
     */
    public ImplicitInnerJoinDeducer(Join join, FromItem fromItem, List<Join> joins) {
        this.rightTable = join.getRightItem().toString().toLowerCase();
        this.fromItem = fromItem;
        this.join = join;
        this.joins = joins;
        update = false;
        foundImplicit = false;

        linked = new LinkedList<>();

        linked.add(fromItem.toString().toLowerCase());
        for (Join j : joins) {
            linked.add(j.getRightItem().toString().toLowerCase());
        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        andExpression.getLeftExpression().accept(this);

        if (expression != null) {
            andExpression.setLeftExpression(expression);
        }

        if (!update) {
            andExpression.getRightExpression().accept(this);
            if (update) {
                expression = andExpression.getLeftExpression();
                update = false;
            } else {
                expression = andExpression;
            }
        } else {
            update = false;
            expression = andExpression.getRightExpression();
        }
    }

    @Override
    public void visit(OrExpression orExpression) {
       // This method must be empty!
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

        }
    }

    /**
     * Checks whether the given two columns are related to the implicit inner join.
     * @param left The right column of the expression.
     * @param right The left column in the expression.
     */
    private void checkImplicitJoin(Column left, Column right) {
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

                linked.remove(rightTable);
                linked.add(1, rightTable);


                join.setOnExpression(expression);
                update = true;

                foundImplicit = true;
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

                        if (jString.equals(leftString)) {
                            linked.remove(rightTable);
                            linked.add(linked.indexOf(leftString), rightTable);
                        } else {
                            linked.remove(leftString);
                            linked.add(linked.indexOf(rightTable), leftString);
                        }

                        update = true;
                        foundImplicit = true;
                        break;
                    }
                }
            }
        }


    }

    public Expression getExpression() {
        return expression;
    }

    public List<Join> getJoins() {
        return joins;
    }
}
