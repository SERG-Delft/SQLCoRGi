package com.github.sergdelft.sqlcorgi.schema;

import java.util.Set;

/**
 * The table data structure allows for storing a database table in a systematic manner.
 */
public class Table {
    private String name;
    private Set<Column> columns;

    /**
     * Constructor to instantiate a table.
     * @param name The table name.
     * @param columns The columns contained in the table.
     */
    public Table(String name, Set<Column> columns) {
        this.name = name;
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public Set<Column> getColumns() {
        return columns;
    }
}
