package com.github.sergdelft.sqlcorgi.schema;

import java.util.List;

/**
 * The table data structure allows for storing a database table in a systematic manner.
 */
public class Table {
    private String name;
    private List<Column> columns;

    /**
     * Constructor to instantiate a table.
     * @param name The table name.
     * @param columns The columns contained in the table.
     */
    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }
}
