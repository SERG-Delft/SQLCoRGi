package com.github.sergdelft.sqlcorgi.schema;

import java.util.Map;

/**
 * The schema data structure allows for storing the database schema in a systematic manner.
 */
public class Schema {
    private String name;
    private Map<String, Table> tables;

    /**
     * Creates an instance of a Schema.
     * @param name The schema name.
     * @param tables The tables contained in the schema.
     */
    public Schema(String name, Map<String, Table> tables) {
        this.name = name;
        this.tables = tables;
    }

    public String getName() {
        return name;
    }

    public Map<String, Table> getTables() {
        return tables;
    }
}
