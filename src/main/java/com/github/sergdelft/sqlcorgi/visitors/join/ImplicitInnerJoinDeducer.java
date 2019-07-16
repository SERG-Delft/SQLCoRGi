package com.github.sergdelft.sqlcorgi.visitors.join;

import com.github.sergdelft.sqlcorgi.JoinRulesGenerator;
import com.github.sergdelft.sqlcorgi.query.JoinWhereItem;
import net.sf.jsqlparser.expression.BinaryExpression;
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

public class ImplicitInnerJoinDeducer extends ExpressionVisitorAdapter {
    private String rightTable;
    //private String leftTable;
    private JoinWhereItem output;
    private boolean update;
    private FromItem fromItem;
    private Join join;
    private List<Join> joins;

    public ImplicitInnerJoinDeducer(Join join, FromItem fromItem, List<Join> joins, JoinWhereItem output) {
        this.rightTable = join.getRightItem().toString().toLowerCase();
        this.fromItem = fromItem;
        this.join = join;
        this.output = output;
        this.joins = joins;
        update = false;
    }

    @Override
    public void visit(AndExpression andExpression) {
        handleExpressionLogical(andExpression);
        if (update) {

        }
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
        EqualsTo eq = new EqualsTo();
        if (equalsTo.getLeftExpression() instanceof Column && equalsTo.getRightExpression() instanceof Column) {
            Column left = (Column) equalsTo.getLeftExpression();
            Column right = (Column) equalsTo.getRightExpression();

            checkImplicitJoin(left, right);
            System.out.println(joins);
        }
    }

    private void checkImplicitJoin(Column left, Column right) {
        Table t1;
        Table t2;

//        if (left.getTable() == null) {
//            t1 = lookupTableByColumn(left);
//        } else {
            t1 = left.getTable();
       // }

//        if (right.getTable() == null) {
//            t2 = lookupTableByColumn(right);
//        } else {
            t2 = right.getTable();
      //  }
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
            for (Join j : joins) {
                jString = j.getRightItem().toString().toLowerCase();
                if ((leftString.equals(jString) && !rightTable.equals(jString))) {
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

            if (!update) {
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
            }
        }



        if (update) {

        }
    }


    private void handleExpressionLogical(BinaryExpression expression) {
        expression.getRightExpression().accept(this);
        if (update) {
            update = false;
            expression.setRightExpression(null);
        } else {
            expression.getLeftExpression().accept(this);
            if (update) {
                update = false;
                expression.setLeftExpression(null);
            }
        }
    }

}
