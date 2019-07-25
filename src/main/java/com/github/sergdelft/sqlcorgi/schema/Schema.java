package com.github.sergdelft.sqlcorgi.schema;

import java.util.HashMap;
import java.util.Map;

/**
 * The schema data structure allows for storing the database schema in a systematic manner.
 */
public class Schema {

    private Map<String, Table> tables;

    /**
     * Creates an empty Schema.
     */
    public Schema() {
        this(new HashMap<>());
    }

    /**
     * Creates an instance of a Schema.
     * @param tables The tables contained in the schema.
     */
    public Schema(Map<String, Table> tables) {
        this.tables = tables;
    }

    /**
     * Adds the given Table to this Schema.
     *
     * @param table the Table to add.
     */
    public void addTable(Table table) {
        tables.put(table.getName(), table);
    }

    public Table getTable(String name) {
        return tables.get(name);
    }

    public Map<String, Table> getTables() {
        return tables;
    }
}
