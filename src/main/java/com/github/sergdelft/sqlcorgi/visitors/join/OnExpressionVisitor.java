package com.github.sergdelft.sqlcorgi.visitors.join;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This visitor allows for extracting the column used in the given expression.
 */
public class OnExpressionVisitor extends ExpressionVisitorAdapter {

    private Map<String, List<Column>> output;

    /**
     * Creates a new visitor that can extract names of columns used in an expression it visits. They are stored in
     * {@code output}.
     *
     * @param output the map in which extracted columns are to be stored.
     */
    public OnExpressionVisitor(Map<String, List<Column>> output) {
        this.output = output;
    }

    /**
     * Stores each column corresponding to its table.
     *
     * @param column the column that should be added.
     */
    private void updateColumnList(Column column) {
        String table = column.getTable().toString().toLowerCase();

        if (!output.containsKey(table)) {
            List<Column> list = new ArrayList<>();
            list.add(column);
            output.put(table, list);
        } else if (!contains(output.get(table), column)) {
            output.get(table).add(column);
        }
    }

    /**
     * Ensures that the list of columns used in the on expression contains no duplicate column names.
     * NOTE: Suppose the column name "id" in "Movies" and the column name is unique.
     * Yet, the function considers Movies.id to be different from id.
     *
     * @param list the list of columns.
     * @param column the column to check for.
     * @return true if {@code column} is already in {@code list}, false otherwise.
     */
    private static boolean contains(List<Column> list, Column column) {
        if (list == null) {
            return false;
        }
        for (Expression e : list) {
            if (e.toString().toLowerCase().equals(column.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(Column column) {
        updateColumnList(column);
    }
}
