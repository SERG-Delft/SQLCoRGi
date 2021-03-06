package com.github.sergdelft.sqlcorgi.visitors.select;

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;

import java.util.HashSet;
import java.util.Set;

import static com.github.sergdelft.sqlcorgi.util.cloner.ExpressionCloner.copy;

/**
 * This class is used to extract all columns used in a given expression.
 */
public class ColumnExtractor extends ExpressionVisitorAdapter {
    private Set<Column> columns;

    public ColumnExtractor() {
        this.columns = new HashSet<>();
    }

    public Set<Column> getColumns() {
        return columns;
    }

    @Override
    public void visit(Column column) {
        columns.add((Column) copy(column));
    }
}
