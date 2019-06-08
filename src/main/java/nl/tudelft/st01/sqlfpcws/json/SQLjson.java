package nl.tudelft.st01.sqlfpcws.json;

import java.util.ArrayList;
import java.util.List;

public class SQLjson {

    private List<SQLRules> entries = new ArrayList<>();

    public void addEntry(SQLRules rules) {
        entries.add(rules);
    }
}