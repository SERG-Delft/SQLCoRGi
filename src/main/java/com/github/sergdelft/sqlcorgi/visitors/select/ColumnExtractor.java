package com.github.sergdelft.sqlcorgi.visitors.select;

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;

import java.util.HashSet;
import java.util.Set;

import static com.github.sergdelft.sqlcorgi.util.cloner.ExpressionCloner.copy;


public class ColumnExtractor extends ExpressionVisitorAdapter {
    private Set<Column> columns;

    public ColumnExtractor() {
        this.columns = new HashSet<>();
    }

    public Set<Column> getColumns() {
        return columns;
    }

    public void reset() {
        columns.clear();
    }

    @Override
    public void visit(Column column) {
        columns.add((Column) copy(column));
    }
}
