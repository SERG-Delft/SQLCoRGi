package nl.tudelft.st01.sqlfpcws.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SQLjson {

    @SerializedName("entries")
    @Expose
    private List<SQLRules> entries = new ArrayList<>();

    public void addEntry(SQLRules rules) {
        entries.add(rules);
    }
}