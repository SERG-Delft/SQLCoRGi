package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class SelectVisitors {
    public static SelectVisitor getSelectPlusVisitor() {
        return new SelectVisitor() {

            public void visit(PlainSelect plainSelect) {
                System.out.println("plainSelect before: \t\t" + plainSelect);

                Expression where = plainSelect.getWhere();

                if(where instanceof BinaryExpression) {
                    // for now, just change it to equals, and one lower
                    EqualsTo eq = new EqualsTo();
                    BinaryExpression bin = (BinaryExpression) where;

                    // left
                    bin.getLeftExpression().accept(ExpressionVisitors.getPlusOneVisitor());
                    eq.setLeftExpression(bin.getLeftExpression());

                    // right
                    bin.getRightExpression().accept(ExpressionVisitors.getPlusOneVisitor());
                    eq.setRightExpression(bin.getRightExpression());

                    plainSelect.setWhere(eq);
                }

                System.out.println("plainSelect after: \t\t\t" + plainSelect + "\n");
            }

            @Override
            public void visit(SetOperationList setOperationList) {
                System.out.println("setOpList: " + setOperationList.toString());
            }

            @Override
            public void visit(WithItem withItem) {
                System.out.println("withItem: " + withItem.toString());
            }

            @Override
            public void visit(ValuesStatement valuesStatement) {
                System.out.println("valStmt: " + valuesStatement.toString());

            }
        };
    }
    public static SelectVisitor getSelectMinusVisitor() {
        return new SelectVisitor() {

            public void visit(PlainSelect plainSelect) {
                System.out.println("plainSelect before: \t\t" + plainSelect);
                Expression where = plainSelect.getWhere();

                if(where instanceof BinaryExpression) {
                    // for now, just change it to equals, and one lower
                    EqualsTo eq = new EqualsTo();
                    BinaryExpression bin = (BinaryExpression) where;

                    // left
                    bin.getLeftExpression().accept(ExpressionVisitors.getMinusOneVisitor());
                    eq.setLeftExpression(bin.getLeftExpression());

                    // right
                    bin.getRightExpression().accept(ExpressionVisitors.getMinusOneVisitor());
                    eq.setRightExpression(bin.getRightExpression());

                    plainSelect.setWhere(eq);
                }

                System.out.println("plainSelect after: \t\t\t" + plainSelect + "\n");
            }

            @Override
            public void visit(SetOperationList setOperationList) {
                System.out.println("setOpList: " + setOperationList.toString());
            }

            @Override
            public void visit(WithItem withItem) {
                System.out.println("withItem: " + withItem.toString());
            }

            @Override
            public void visit(ValuesStatement valuesStatement) {
                System.out.println("valStmt: " + valuesStatement.toString());

            }
        };
    }
    public static SelectVisitor getSelectNeutralVisitor() {
        return new SelectVisitor() {

            public void visit(PlainSelect plainSelect) {
                System.out.println("plainSelect before: \t\t" + plainSelect);
                Expression where = plainSelect.getWhere();

                if(where instanceof BinaryExpression) {
                    // for now, just change it to equals, and one lower
                    EqualsTo eq = new EqualsTo();
                    BinaryExpression bin = (BinaryExpression) where;

                    // left
                    eq.setLeftExpression(bin.getLeftExpression());

                    // right
                    eq.setRightExpression(bin.getRightExpression());

                    plainSelect.setWhere(eq);
                }

                System.out.println("plainSelect after: \t\t\t" + plainSelect + "\n");
            }

            @Override
            public void visit(SetOperationList setOperationList) {
                System.out.println("setOpList: " + setOperationList.toString());
            }

            @Override
            public void visit(WithItem withItem) {
                System.out.println("withItem: " + withItem.toString());
            }

            @Override
            public void visit(ValuesStatement valuesStatement) {
                System.out.println("valStmt: " + valuesStatement.toString());

            }
        };
    }
    public static SelectVisitor getSelectNullVisitor() {
        return new SelectVisitor() {

            public void visit(PlainSelect plainSelect) {

                System.out.println("plainSelect before: \t\t" + plainSelect);
                Expression where = plainSelect.getWhere();

                if(where instanceof BinaryExpression) {
                    // for now, just change it to equals, and one lower
                    IsNullExpression isNull = new IsNullExpression();
                    BinaryExpression bin = (BinaryExpression) where;

                    // left
                    isNull.setLeftExpression(bin.getLeftExpression());

                    plainSelect.setWhere(isNull);
                }
                System.out.println("plainSelect after: \t\t\t" + plainSelect + "\n");
            }

            @Override
            public void visit(SetOperationList setOperationList) {
                System.out.println("setOpList: " + setOperationList.toString());
            }

            @Override
            public void visit(WithItem withItem) {
                System.out.println("withItem: " + withItem.toString());
            }

            @Override
            public void visit(ValuesStatement valuesStatement) {
                System.out.println("valStmt: " + valuesStatement.toString());

            }
        };
    }
}
