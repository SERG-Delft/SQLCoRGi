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
import java.util.Set;


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
    private Set<String> simple;


    /**
     * Constructor.
     *
     * @param join The join that is evaluated.
     * @param fromItem The from item of the from clause.
     * @param joins The list of joins in the from clause.
     * @param linked The list of the current order of joins.
     * @param simple The set of table names of simple joins.
     */
    public ImplicitInnerJoinDeducer(Join join, FromItem fromItem, List<Join> joins,
                                    List<String> linked, Set<String> simple) {
        this.rightTable = join.getRightItem().toString().toLowerCase();
        this.fromItem = fromItem;
        this.join = join;
        this.joins = joins;
        this.simple = simple;
        update = false;
        foundImplicit = false;

        this.linked = linked;
    }

    @Override
    public void visit(AndExpression andExpression) {
        andExpression.getLeftExpression().accept(this);
        updateAndExpression(andExpression);

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
        if (!foundImplicit && equalsTo.getLeftExpression() instanceof Column
                && equalsTo.getRightExpression() instanceof Column) {
            Column left = (Column) equalsTo.getLeftExpression();
            Column right = (Column) equalsTo.getRightExpression();

            checkImplicitJoin(left, right);
        }
    }

    /**
     * Checks whether the given two columns are related to the implicit inner join.
     *
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
                updateSimpleJoin(join, left, right);
                updateLinked(rightTable, 1);

                update = true;
                foundImplicit = true;
            }

            if (!update) {
                for (Join j : joins) {
                    jString = j.getRightItem().toString().toLowerCase();
                    if (j.isSimple() && (leftString.equals(jString) || rightTable.equals(jString))) {
                        updateSimpleJoin(j, left, right);

                        if (jString.equals(leftString)) {
                            updateLinked(rightTable, linked.indexOf(leftString));
                        } else {
                            updateLinked(leftString, linked.indexOf(rightTable));
                        }

                        update = true;
                        foundImplicit = true;
                        break;
                    }
                }
            }
        }


    }

    /**
     * Updates the and expression if an implicit inner join is present in the expression in the where clause.
     *
     * @param andExpression The and expression to be updated.
     */
    private void updateAndExpression(AndExpression andExpression) {
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

    /**
     * When an simple join is to be converted to an inner join, the join is updated and removed from the list
     * of simple joins.
     *
     * @param join The join to be updated.
     * @param left The left column for the on expression.
     * @param right The right column for the on expression.
     */
    private void updateSimpleJoin(Join join, Column left, Column right) {
        join.setInner(true);
        join.setSimple(false);

        EqualsTo expression = new EqualsTo();
        expression.setRightExpression(right);
        expression.setLeftExpression(left);
        join.setOnExpression(expression);

        simple.remove(left.getTable().toString().toLowerCase());
        simple.remove(right.getTable().toString().toLowerCase());
    }

    /**
     * Update the linked list when the order of joins should be changed.
     *
     * @param table The table for which the join should be moved.
     * @param index The index of the new position.
     */
    private void updateLinked(String table, int index) {
        linked.remove(table);
        linked.add(index, table);
    }

    public Expression getExpression() {
        return expression;
    }

    public List<Join> getJoins() {
        return joins;
    }
}
