package com.github.sergdelft.sqlcorgi.unit.schema;

import com.github.sergdelft.sqlcorgi.schema.Column;
import com.github.sergdelft.sqlcorgi.schema.Schema;
import com.github.sergdelft.sqlcorgi.schema.Table;
import com.github.sergdelft.sqlcorgi.schema.TableStructure;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TableStructureTest {

    private static Schema makeSchema() {

        List<Column> columns1 = new ArrayList<>();
        columns1.add(new Column("a", true, false, Column.DataType.INTEGER));
        columns1.add(new Column("b", true, false, Column.DataType.INTEGER));
        columns1.add(new Column("c", false, false, Column.DataType.INTEGER));

        List<Column> columns2 = new ArrayList<>();
        columns2.add(new Column("a", true, false, Column.DataType.INTEGER));
        columns2.add(new Column("x", false, false, Column.DataType.INTEGER));
        columns2.add(new Column("y", false, false, Column.DataType.INTEGER));

        Map<String, Table> tables = new HashMap<>();
        tables.put("t1", new Table("t1", columns1));
        tables.put("t2", new Table("t2", columns2));

        return new Schema(tables);
    }

    @Test
    void testSingleTable() throws JSQLParserException {

        String query = "SELECT * FROM t1 FULL JOIN t2";
        PlainSelect plainSelect = (PlainSelect) ((Select) CCJSqlParserUtil.parse(query)).getSelectBody();

        TableStructure tableStructure = new TableStructure();
        tableStructure.setSchema(makeSchema());

        tableStructure.addLayer(plainSelect.getFromItem(), plainSelect.getJoins());

        System.out.println(tableStructure.getTableStack().toString());
    }
}
