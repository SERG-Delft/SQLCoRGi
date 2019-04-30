package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.ValueListExpression;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.SelectUtils;

public class Example {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        try {
            Statement stmt = CCJSqlParserUtil.parse("SELECT Name FROM Employees WHERE Salary < 40000");
            System.out.println("before change: Statement = \t\t\t" + stmt.toString());
            Select select = (Select) stmt;

            // with selectUtils you can add all kinds of things to a query. Try for yourself!
            SelectUtils.addExpression(select, new Column("Address"));

            select.getSelectBody().accept(getSelectVisitor());


            System.out.println("After change: Statement = \t\t\t" + stmt.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static SelectVisitor getSelectVisitor() {
        return new SelectVisitor() {
            @Override
            public void visit(PlainSelect plainSelect) {

                // adding an alias for the table
                System.out.println("Before Alias: \t\t\t\t\t\t" + plainSelect.getFromItem());
                Alias a  = new Alias("Workers", true); // useAs means: put in the word 'AS' in the query or not
                plainSelect.getFromItem().setAlias(a);
                System.out.println("After alias: \t\t\t\t\t\t" + plainSelect.getFromItem());

                // unleashing a visitor that changes the values of the WHERE clause
                Expression where = plainSelect.getWhere();
                where.accept(getExpressionVisitor());
                System.out.println("Where clause after visitor: \t\t" + where.toString());


                // changing the expression from < to >

                // you first have to initialize a new Expression object, then
                //      set the left and right expression of the object, then
                //      update the where attribute of the select object
                MinorThan mt = (MinorThan) where;
                GreaterThan gt = new GreaterThan();

                gt.setLeftExpression(mt.getLeftExpression());
                gt.setRightExpression(mt.getRightExpression());

                plainSelect.setWhere(gt);
                System.out.println("Where after changing conditon: \t\t" + plainSelect.getWhere());
            }

            @Override
            public void visit(SetOperationList setOperationList) {
                System.out.println("setOpList: "+ setOperationList.toString());
            }

            @Override
            public void visit(WithItem withItem) {
                System.out.println("withItem: "+ withItem.toString());
            }

            @Override
            public void visit(ValuesStatement valuesStatement) {
                System.out.println("valStmt: "+ valuesStatement.toString());

            }
        };
    }

    static ExpressionVisitor getExpressionVisitor() {
        return new ExpressionVisitor() {
            @Override
            public void visit(BitwiseRightShift bitwiseRightShift) {

            }

            @Override
            public void visit(BitwiseLeftShift bitwiseLeftShift) {

            }

            @Override
            public void visit(NullValue nullValue) {

            }

            @Override
            public void visit(Function function) {

            }

            @Override
            public void visit(SignedExpression signedExpression) {

            }

            @Override
            public void visit(JdbcParameter jdbcParameter) {

            }

            @Override
            public void visit(JdbcNamedParameter jdbcNamedParameter) {

            }

            @Override
            public void visit(DoubleValue doubleValue) {

            }

            @Override
            public void visit(LongValue longValue) {

            }

            @Override
            public void visit(HexValue hexValue) {

            }

            @Override
            public void visit(DateValue dateValue) {

            }

            @Override
            public void visit(TimeValue timeValue) {

            }

            @Override
            public void visit(TimestampValue timestampValue) {

            }

            @Override
            public void visit(Parenthesis parenthesis) {

            }

            @Override
            public void visit(StringValue stringValue) {

            }

            @Override
            public void visit(Addition addition) {

            }

            @Override
            public void visit(Division division) {

            }

            @Override
            public void visit(Multiplication multiplication) {

            }

            @Override
            public void visit(Subtraction subtraction) {

            }

            @Override
            public void visit(AndExpression andExpression) {

            }

            @Override
            public void visit(OrExpression orExpression) {

            }

            @Override
            public void visit(Between between) {

            }

            @Override
            public void visit(EqualsTo equalsTo) {

            }

            @Override
            public void visit(GreaterThan greaterThan) {

            }

            @Override
            public void visit(GreaterThanEquals greaterThanEquals) {

            }

            @Override
            public void visit(InExpression inExpression) {

            }

            @Override
            public void visit(IsNullExpression isNullExpression) {

            }

            @Override
            public void visit(LikeExpression likeExpression) {

            }

            @Override
            public void visit(MinorThan minorThan) {
                // in the visitor pattern you can define what you want to do for every
                // different instance of an Expression. Here, we edit the data of the salary

                // Note: We CANNOT change the < to a > here. This has to be done
                // on a higher level
                System.out.println("Left expression of MinorThan: \t\t" + minorThan.getLeftExpression());
                System.out.println("String expression of MinorThan: \t" + minorThan.getStringExpression());
                System.out.println("Right expression of MinorThan: \t\t" + minorThan.getRightExpression());
                // you can also toggle the NOT operator
                minorThan.setNot();
                System.out.println("with NOT: \t\t\t\t\t\t\t" + minorThan);
                minorThan.removeNot();
                System.out.println("without NOT: \t\t\t\t\t\t" + minorThan);

                minorThan.setLeftExpression(new Column("DragonBallZScore"));

                minorThan.setRightExpression(new DoubleValue("9000"));
            }

            @Override
            public void visit(MinorThanEquals minorThanEquals) {

            }

            @Override
            public void visit(NotEqualsTo notEqualsTo) {

            }

            @Override
            public void visit(Column column) {

            }

            @Override
            public void visit(SubSelect subSelect) {

            }

            @Override
            public void visit(CaseExpression caseExpression) {

            }

            @Override
            public void visit(WhenClause whenClause) {

            }

            @Override
            public void visit(ExistsExpression existsExpression) {

            }

            @Override
            public void visit(AllComparisonExpression allComparisonExpression) {

            }

            @Override
            public void visit(AnyComparisonExpression anyComparisonExpression) {

            }

            @Override
            public void visit(Concat concat) {

            }

            @Override
            public void visit(Matches matches) {

            }

            @Override
            public void visit(BitwiseAnd bitwiseAnd) {

            }

            @Override
            public void visit(BitwiseOr bitwiseOr) {

            }

            @Override
            public void visit(BitwiseXor bitwiseXor) {

            }

            @Override
            public void visit(CastExpression castExpression) {

            }

            @Override
            public void visit(Modulo modulo) {

            }

            @Override
            public void visit(AnalyticExpression analyticExpression) {

            }

            @Override
            public void visit(ExtractExpression extractExpression) {

            }

            @Override
            public void visit(IntervalExpression intervalExpression) {

            }

            @Override
            public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {

            }

            @Override
            public void visit(RegExpMatchOperator regExpMatchOperator) {

            }

            @Override
            public void visit(JsonExpression jsonExpression) {

            }

            @Override
            public void visit(JsonOperator jsonOperator) {

            }

            @Override
            public void visit(RegExpMySQLOperator regExpMySQLOperator) {

            }

            @Override
            public void visit(UserVariable userVariable) {

            }

            @Override
            public void visit(NumericBind numericBind) {

            }

            @Override
            public void visit(KeepExpression keepExpression) {

            }

            @Override
            public void visit(MySQLGroupConcat mySQLGroupConcat) {

            }

            @Override
            public void visit(ValueListExpression valueListExpression) {

            }

            @Override
            public void visit(RowConstructor rowConstructor) {

            }

            @Override
            public void visit(OracleHint oracleHint) {

            }

            @Override
            public void visit(TimeKeyExpression timeKeyExpression) {

            }

            @Override
            public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {

            }

            @Override
            public void visit(NotExpression notExpression) {

            }
        };
    }
}
