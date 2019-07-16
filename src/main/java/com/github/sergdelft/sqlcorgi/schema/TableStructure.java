package com.github.sergdelft.sqlcorgi.schema;

import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;

import java.util.*;

// TODO
public class TableStructure {

    private Schema schema;
    private Deque<Map<String, Table>> tableStack = new LinkedList<>();

    public void addLayer(FromItem fromItem, List<Join> joins) {
        Map<String, Table> tables = new HashMap<>();

        if (fromItem instanceof net.sf.jsqlparser.schema.Table) {
            net.sf.jsqlparser.schema.Table fromItem1 = (net.sf.jsqlparser.schema.Table) fromItem;
        }
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
