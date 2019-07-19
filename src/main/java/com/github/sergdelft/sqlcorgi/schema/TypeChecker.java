package com.github.sergdelft.sqlcorgi.schema;

import com.github.sergdelft.sqlcorgi.exceptions.TypeCheckerException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;

public class TypeChecker extends ExpressionVisitorAdapter {

    private Column.DataType type;
    private TableStructure tableStructure;

    public TypeChecker(TableStructure tableStructure) {
        this.tableStructure = tableStructure;
    }

    public Column.DataType getType() {
        return this.type;
    }

    private void setType(Column.DataType dataType) {
        if (type != null && type != dataType) {
            throw new TypeCheckerException("All types in the expression must be equal.");
        } else {
            type = dataType;
        }
    }

    @Override
    public void visit(net.sf.jsqlparser.schema.Column column) {
        Column.DataType type = tableStructure.getDataType(column);
        setType(type);
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        setType(Column.DataType.NUM);
    }

    @Override
    public void visit(LongValue longValue) {
        setType(Column.DataType.NUM);
    }

    @Override
    public void visit(StringValue stringValue) {
        setType(Column.DataType.STRING);
    }

//    @Override
//    public void visit(Addition expr) {
//        type = Column.DataType.NUM;
//    }
//
//    @Override
//    public void visit(Division expr) {
//        this.visitBinaryExpression(expr);
//    }
//
//    @Override
//    public void visit(Multiplication expr) {
//        this.visitBinaryExpression(expr);
//    }
//
//    @Override
//    public void visit(Subtraction expr) {
//        this.visitBinaryExpression(expr);
//    }
}
