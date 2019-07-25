package com.github.sergdelft.sqlcorgi.schema;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.select.*;

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

    public Deque<Map<String, Table>> getTableStack() {
        return tableStack;
    }

    public Schema getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        return tableStack.toString();
    }

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
     * Removes the most recently added layer of tables from the structure.
     */
    public void removeLayer() {
        tableStack.pop();
    }

    public boolean isNullable(net.sf.jsqlparser.schema.Column column) {

        if (schema != null) {
            return getColumn(column).isNullable();
        }

        return false;
    }

    public boolean isKey(net.sf.jsqlparser.schema.Column column) {

        if (schema != null) {
            return getColumn(column).isKey();
        }

        return false;
    }

    public Column.DataType getDataType(net.sf.jsqlparser.schema.Column column) {

        if (schema != null) {
            return getColumn(column).getDataType();
        }

        return Column.DataType.STRING;
    }

    /**
     * Returns the {@link Column} associated with the {@code column}.
     * @param column a {@link net.sf.jsqlparser.schema.Column} referring to a {@code Column}.
     *
     * @return a {@code Column} if one is found.
     * @throws AmbiguousColumnException if {@code column} references multiple {@code Column}s.
     * @throws UnknownColumnException if no {@code Column} is found.
     */
    public Column getColumn(net.sf.jsqlparser.schema.Column column)
            throws AmbiguousColumnException, UnknownColumnException {

        net.sf.jsqlparser.schema.Table jsqlTable = column.getTable();
        String tableName = jsqlTable == null ? null : jsqlTable.getName();
        String colName = column.getColumnName();

        Column result = null;
        for (Map<String, Table> tableMap : tableStack) {

            if (tableName != null) {
                for (Column temp : tableMap.get(tableName).getColumns()) {
                    if (colName.equals(temp.getName())) {
                        if (result != null) {
                            throw new AmbiguousColumnException(colName);
                        }
                        result = temp;
                    }
                }
            } else {
                Table foundTable = null;
                for (Map.Entry<String, Table> table : tableMap.entrySet()) {

                    Column found = null;
                    for (Column temp : table.getValue().getColumns()) {
                        if (colName.equals(temp.getName())) {
                            if (found != null) {
                                throw new AmbiguousColumnException(colName);
                            }
                            found = temp;
                        }
                    }

                    if (result != null && found != null) {
                        if (foundTable != null) {
                            if (foundTable != table.getValue()) {
                                throw new AmbiguousColumnException(colName);
                            }
                        }
                    }
                    foundTable = table.getValue();
                    result = found;
                }
            }

            if (result != null) {
                return result;
            }
        }

        throw new UnknownColumnException("The following column could not be found: " + column);
    }

    /**
     * Returns the {@link Table} associated with the {@code tableName}.
     * @param tableName a string referring to a {@code Table}.
     *
     * @return a {@code Table} if one is found.
     * @throws UnknownTableException if no {@code Table} is found.
     */
    public Table getTable(String tableName) throws UnknownTableException {

        for (Map<String, Table> tableMap : tableStack) {
            Table table = tableMap.get(tableName);
            if (table != null) {
                return table;
            }
        }

        throw new UnknownTableException("The following table could not be found: " + tableName);
    }

    public Table getFromTable() {

        if (tableStack.isEmpty()) {
            return null;
        }

        return tableStack.peek().get("");
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
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

            if (table == null) {
                throw new UnknownTableException("The following table cannot be found in the schema: " + name);
            }

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
            return deriveSubSelectTable((SubSelect) fromItem, storeTable);
        }

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

    private Table deriveSubSelectTable(SubSelect subSelect, boolean storeTable) {

        Alias alias = subSelect.getAlias();
        if (alias == null) {
            throw new IllegalArgumentException("The following subquery must have an alias: " + subSelect);
        }

        Table derivedTable = null;

        SelectBody selectBody = subSelect.getSelectBody();
        if (selectBody instanceof PlainSelect) {
            derivedTable = deriveSelectTable((PlainSelect) selectBody);

        } else if (selectBody instanceof SetOperationList) {
            List<SelectBody> selects = ((SetOperationList) selectBody).getSelects();
            for (SelectBody select : selects) {
                if (select instanceof PlainSelect) {
                    derivedTable = deriveSelectTable((PlainSelect) select);
                    break;
                }
            }
        }

        if (derivedTable == null) {
            throw new UnsupportedOperationException("Unsupported select body: " + selectBody);
        }

        if (storeTable) {
            Map<String, Table> tables = tableStack.peek();
            assert tables != null;

            tables.put(alias.getName(), derivedTable);
        }

        return derivedTable;
    }

    private Table deriveSelectTable(PlainSelect select) {

        Table derivedTable = new Table(null);

        List<SelectItem> selectItems = select.getSelectItems();
        FromItem fromItem = select.getFromItem();
        List<Join> joins = select.getJoins();

        TableStructure tableStructure = new TableStructure();
        tableStructure.setSchema(schema);
        tableStructure.addLayer(fromItem, joins);
        for (SelectItem selectItem : selectItems) {

            if (selectItem instanceof AllColumns) {
                for (Column column : tableStructure.getFromTable().getColumns()) {
                    derivedTable.addColumn(column);
                }

            } else if (selectItem instanceof AllTableColumns) {
                Table table = tableStructure.getTable(((AllTableColumns) selectItem).getTable().getName());
                for (Column column : table.getColumns()) {
                    derivedTable.addColumn(column);
                }

            } else if (selectItem instanceof SelectExpressionItem) {

                TypeChecker typeChecker = new TypeChecker(tableStructure);
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                selectExpressionItem.getExpression().accept(typeChecker);

                Alias alias = selectExpressionItem.getAlias();
                String columnName = alias != null ? alias.getName() : "";
                derivedTable.addColumn(new Column(columnName, false, false, typeChecker.getType()));
            }
        }
        tableStructure.removeLayer();

        return derivedTable;
    }
}
