package com.github.sergdelft.sqlcorgi.schema;

import net.sf.jsqlparser.statement.select.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO
public class TableSetBuilder implements FromItemVisitor {

    private Schema schema;
    private Map<String, Table> tables = new HashMap<>();

    @Override
    public void visit(net.sf.jsqlparser.schema.Table tableName) {
        List<Table> tables = schema.getTables();
        for (Table table : tables) {
            if (table.getName().equals(tableName.getName())) {
                this.tables.put(table.getName(), table);
                break;
            }
        }
    }

    @Override
    public void visit(SubSelect subSelect) {

    }

    @Override
    public void visit(SubJoin subjoin) {

    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }

    @Override
    public void visit(ValuesList valuesList) {

    }

    @Override
    public void visit(TableFunction tableFunction) {

    }

    @Override
    public void visit(ParenthesisFromItem aThis) {

    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
