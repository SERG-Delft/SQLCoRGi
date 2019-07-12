package com.github.sergdelft.sqlcorgi.sqlfpcws.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents the entries in the JSON output of the {@code BulkMainGenerator}.
 *
 * {@link com.google.gson.Gson} is used to enable easy JSON (de)serialization.
 */
public class JSONEntries {

    @SerializedName("entries")
    @Expose
    private List<SQLRules> entries = new ArrayList<>();

    /**
     * Adds the {@link SQLRules} for a query to the the list of entries.
     *
     * @param rules The rules to add.
     */
    public void addEntry(SQLRules rules) {
        entries.add(rules);
    }

    public List<SQLRules> getEntries() {
        return entries;
    }
}
