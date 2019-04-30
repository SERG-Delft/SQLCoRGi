package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.SelectUtils;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        try {
            Statement stmt = CCJSqlParserUtil.parse("SELECT someColumn FROM tab1 WHERE a < 10");
            System.out.println("before change: Statement = " + stmt.toString());
            Select select = (Select) stmt;
            SelectUtils.addExpression(select, new Column("otherColumn"));

            MinorThan mt = new MinorThan();
            mt.setLeftExpression(new Column("yetAnotherColumn"));
            mt.setRightExpression(new DoubleValue("727"));
            SelectUtils.addExpression(select, mt);

            stmt = changeCondition(stmt, VisitorFactory.getSelectVisitor());
            System.out.println("before change: Statement = " + stmt.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * The idea of this method is to change the '>' to a '<'
     * I did not yet get it working, but this beginning may help to get things going in the right direction
     *
     * @param st
     * @param visitor
     * @return
     */
    public static Statement changeCondition(Statement st, SelectVisitor visitor) {
        Select select = (Select) st;
        select.getSelectBody().accept(visitor);
        return st;
    }
}