package com.github.sergdelft.sqlcorgi.schema;

import com.github.sergdelft.sqlcorgi.schema.Column.DataType;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.statement.select.SubSelect;

import static com.github.sergdelft.sqlcorgi.schema.Column.DataType.*;

/**
 * This class is used to determine whether the types of the attributes and elements of an expression are consistent.
 */
public class TypeChecker implements ExpressionVisitor {

    private DataType type;
    private TableStructure tableStructure;

    public TypeChecker(TableStructure tableStructure) {
        this.tableStructure = tableStructure;
    }

    public DataType getType() {
        return this.type;
    }

    private void setType(DataType dataType) {
        type = dataType;
    }

    @Override
    public void visit(BitwiseRightShift aThis) {
        setType(NUM);
    }

    @Override
    public void visit(BitwiseLeftShift aThis) {
        setType(NUM);
    }

    @Override
    public void visit(NullValue nullValue) {
        setType(NULL);
    }

    @Override
    public void visit(Function function) {

        String name = function.getName();
        switch (name) {
            case "MAX":
            case "MIN":
                Expression expression = function.getParameters().getExpressions().get(0);
                expression.accept(this);
                break;
            case "AVG":
            case "COUNT":
            case "SUM":
                setType(NUM);
                break;
            default:
                setType(STRING);
                break;
        }
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        setType(NUM);
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {

    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {

    }

    @Override
    public void visit(DoubleValue doubleValue) {
        setType(NUM);
    }

    @Override
    public void visit(LongValue longValue) {
        setType(NUM);
    }

    @Override
    public void visit(HexValue hexValue) {
        setType(NUM);
    }

    @Override
    public void visit(DateValue dateValue) {
        setType(STRING);
    }

    @Override
    public void visit(TimeValue timeValue) {
        setType(STRING);
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        setType(STRING);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue stringValue) {
        setType(STRING);
    }

    @Override
    public void visit(Addition addition) {
        setType(NUM);
    }

    @Override
    public void visit(Division division) {
        setType(NUM);
    }

    @Override
    public void visit(Multiplication multiplication) {
        setType(NUM);
    }

    @Override
    public void visit(Subtraction subtraction) {
        setType(NUM);
    }

    @Override
    public void visit(AndExpression andExpression) {
        setType(STRING);
    }

    @Override
    public void visit(OrExpression orExpression) {
        setType(STRING);
    }

    @Override
    public void visit(Between between) {
        setType(STRING);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        setType(STRING);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        setType(STRING);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        setType(STRING);
    }

    @Override
    public void visit(InExpression inExpression) {
        setType(STRING);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        setType(STRING);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        setType(STRING);
    }

    @Override
    public void visit(MinorThan minorThan) {
        setType(STRING);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        setType(STRING);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        setType(STRING);
    }

    @Override
    public void visit(net.sf.jsqlparser.schema.Column column) {
        setType(tableStructure.getDataType(column));
    }

    @Override
    public void visit(SubSelect subSelect) {
        setType(STRING);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        setType(STRING);
    }

    @Override
    public void visit(WhenClause whenClause) {

    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        setType(STRING);
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {

    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {

    }

    @Override
    public void visit(Concat concat) {
        setType(STRING);
    }

    @Override
    public void visit(Matches matches) {
        setType(STRING);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        setType(STRING);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        setType(STRING);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        setType(STRING);
    }

    @Override
    public void visit(CastExpression cast) {

    }

    @Override
    public void visit(Modulo modulo) {
        setType(NUM);
    }

    @Override
    public void visit(AnalyticExpression aexpr) {

    }

    @Override
    public void visit(ExtractExpression eexpr) {

    }

    @Override
    public void visit(IntervalExpression iexpr) {

    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {

    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        setType(STRING);
    }

    @Override
    public void visit(JsonExpression jsonExpr) {

    }

    @Override
    public void visit(JsonOperator jsonExpr) {

    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        setType(STRING);
    }

    @Override
    public void visit(UserVariable var) {

    }

    @Override
    public void visit(NumericBind bind) {

    }

    @Override
    public void visit(KeepExpression aexpr) {

    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {

    }

    @Override
    public void visit(ValueListExpression valueList) {

    }

    @Override
    public void visit(RowConstructor rowConstructor) {

    }

    @Override
    public void visit(OracleHint hint) {

    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        setType(STRING);
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        setType(STRING);
    }

    @Override
    public void visit(NotExpression aThis) {
        setType(STRING);
    }

    @Override
    public void visit(NextValExpression aThis) {

    }

    @Override
    public void visit(CollateExpression aThis) {

    }

    @Override
    public void visit(SimilarToExpression aThis) {
        setType(STRING);
    }

}
