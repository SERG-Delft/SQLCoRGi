package com.github.sergdelft.sqlcorgi.schema;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.select.*;

import javax.xml.crypto.Data;
import java.util.*;

// TODO: Finish this class and write documentation

/**
 * This class represents the collection of tables that can be referenced from within a query. In order to use it,
 * create an instance, provide it with a schema, and add a new layer. Each layer represents the collection of tables
 * that can be referenced in the local FROM clause of a query. Consider a subquery: it can reference tables from its
 * own FROM clause, but also from the FROM clause of the query that contains it. When finished with a query, make sure
 * to remove its layer.
 */
public class TableStructure {

    private Schema schema;
    private Deque<Map<String, Table>> tableStack = new LinkedList<>();

    /**
     * Adds a new layer of tables to the structure, which is derived from the given {@link FromItem} and list of
     * {@link Join}s.
     *
     * @param fromItem the {@code FromItem} of a query.
     * @param joins the accompanying list of {@code Join}s of the query.
     */
    public void addLayer(FromItem fromItem, List<Join> joins) {

        HashMap<String, Table> tables = new HashMap<>();
        tableStack.push(tables);

        Table left = deriveFromItem(fromItem, true);

        if (joins != null) {
            for (Join join : joins) {
                left = deriveJoinTable(left, join, true);
            }
        }

        tables.put("", left);
    }

    /**
     * Returns the {@link Table} derived from the given {@link FromItem}. If the {@code FromItem} has an
     * {@link Alias} and {@code storeTable} is {@code true}, it will be added to the collection of {@code Table}s.
     *
     * @param fromItem the {@code FromItem} to derive the {@code Table} from.
     * @param storeTable determines whether the {@code FromItem} is added to the collection of {@code Table}s, if the
     * {@code FromItem} has an {@code Alias} or is a {@code Table} itself.
     * @return the {@code Table} derived from the given {@code FromItem}.
     */
    private Table deriveFromItem(FromItem fromItem, boolean storeTable) {

        Map<String, Table> tables = tableStack.peek();
        assert tables != null;

        if (fromItem instanceof net.sf.jsqlparser.schema.Table) {
            String name = ((net.sf.jsqlparser.schema.Table) fromItem).getName();
            Table table = schema.getTable(name);

            if (storeTable) {
                Alias alias = fromItem.getAlias();
                if (alias == null) {
                    tables.put(name, table);
                } else {
                    tables.put(alias.getName(), table);
                }
            }

            return table;
        }

        if (fromItem instanceof ParenthesisFromItem) {
            Alias alias = fromItem.getAlias();
            Table table = deriveFromItem(((ParenthesisFromItem) fromItem).getFromItem(), storeTable && alias == null);

            if (storeTable && alias != null) {
                tables.put(alias.getName(), table);
            }
            return table;
        }

        if (fromItem instanceof SubJoin) {
            return deriveSubJoinTable((SubJoin) fromItem, storeTable);
        }

        if (fromItem instanceof SubSelect) {
            return deriveSubTable((SubSelect) fromItem, storeTable);
        }

        // TODO: Lateral subselect, values list, table function

        throw new UnsupportedOperationException("Encountered the following item in the FROM clause: " + fromItem);
    }

    private Table deriveSubJoinTable(SubJoin subJoin, boolean storeTable) {

        Alias alias = subJoin.getAlias();
        Table joinTable = deriveFromItem(subJoin.getLeft(), storeTable && alias == null);

        List<Join> joins = subJoin.getJoinList();
        for (Join join : joins) {
            joinTable = deriveJoinTable(joinTable, join, storeTable && alias == null);
        }

        if (storeTable && alias != null) {
            Map<String, Table> tables = tableStack.peek();
            assert tables != null;

            tables.put(alias.getName(), joinTable);
        }

        return joinTable;
    }

    private Table deriveJoinTable(Table left, Join join, boolean storeTable) {

        Table right = deriveFromItem(join.getRightItem(), storeTable);

        List<Column> leftColumns = left.getColumns();
        List<Column> rightColumns = right.getColumns();

        List<Column> joinColumns = new ArrayList<>(leftColumns.size() + rightColumns.size());
        Table joinTable = new Table(null, joinColumns);

        boolean updateLeft = join.isRight() || join.isFull();
        boolean updateRight = join.isLeft() || join.isFull();

        if (updateLeft) {
            for (Column column : leftColumns) {
                joinColumns.add(new Column(column.getName(), true, false, column.getDataType()));
            }
        } else {
            joinColumns.addAll(leftColumns);
        }

        if (updateRight) {
            for (Column column : rightColumns) {
                joinColumns.add(new Column(column.getName(), true, false, column.getDataType()));
            }
        } else {
            joinColumns.addAll(rightColumns);
        }

        return joinTable;
    }

    private Table deriveSubTable(SubSelect subSelect, boolean storeTable) {

        // TODO: Derive table

        Alias alias = subSelect.getAlias();
        //if (alias != null) {
        //    // TODO: Add table
        //}

        throw new UnsupportedOperationException("To be implemented");
    }

    /**
     * Removes the most recently added layer of tables from the structure.
     */
    public void removeLayer() {
        tableStack.pop();
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }


    /*


    TODO: Update this dummy method.

     */
    public Column.DataType getDataType(net.sf.jsqlparser.schema.Column column) {
        return Column.DataType.NUM;
    }
}
