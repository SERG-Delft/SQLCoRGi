package com.github.sergdelft.sqlcorgi.visitors.select;

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;

import java.util.HashSet;
import java.util.Set;

/**
 * This visitor can be used to obtain a set of attributes that are null checked in an expression.
 */
public class NullAttributeFinder extends ExpressionVisitorAdapter {

    private Set<String> columns = new HashSet<>();

    @Override
    public void visit(IsNullExpression isNull) {
        if (!isNull.isNot()) {
            columns.add(isNull.getLeftExpression().toString());
        }
    }

    public Set<String> getColumns() {
        return columns;
    }
}
