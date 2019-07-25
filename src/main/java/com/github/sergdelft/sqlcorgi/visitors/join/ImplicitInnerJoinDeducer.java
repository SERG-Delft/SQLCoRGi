package com.github.sergdelft.sqlcorgi.visitors.join;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;

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

    /**
     * Checks whether the given two columns are related to an implicit inner join.
     *
     * @param left The right column of the expression.
     * @param right The left column in the expression.
     */
    private void checkImplicitJoin(Column left, Column right) {
        Table t1;
        Table t2;

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
        andExpression.getLeftExpression().accept(this);
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

    @Override
    public void visit(AndExpression andExpression) {
        updateAndExpression(andExpression);
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

    @Override
    public void visit(GreaterThan gt) {
        expression = gt;
    }

    @Override
    public void visit(OrExpression orExpression) {
        expression = orExpression;
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        expression = greaterThanEquals;
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        expression = isNullExpression;
    }

    @Override
    public void visit(MinorThan minorThan) {
        expression = minorThan;
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        expression = minorThanEquals;
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        expression = notEqualsTo;
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
        expression = column;
    }

    @Override
    public void visit(Between between) {
        expression = between;
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        expression = likeExpression;
    }

    @Override
    public void visit(InExpression inExpression) {
        expression = inExpression;
    }

    @Override
    public void visit(StringValue value) {
        expression = value;
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        expression = parenthesis;
    }
}
