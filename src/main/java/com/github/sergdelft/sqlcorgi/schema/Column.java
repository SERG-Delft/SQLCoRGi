package com.github.sergdelft.sqlcorgi.schema;

public class Column {
    private String name;
    private boolean isNullable;
    private boolean isKey;
    private DataType dataType;

    /**
     * Data type enum.
     * Contains all supported data types.
     */
    enum DataType {
        INTEGER, STRING
    }

    /**
     * Constructor to instantiate a column.
     * @param name Column name.
     * @param isNullable Specifies whether the it is nullable.
     * @param isKey Specifies whether it is a key.
     * @param dataType The data type.
     */
    public Column(String name, boolean isNullable, boolean isKey, DataType dataType) {
        this.name = name;
        this.isNullable = isNullable;
        this.isKey = isKey;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public boolean isKey() {
        return isKey;
    }

    public DataType getDataType() {
        return dataType;
    }
}
