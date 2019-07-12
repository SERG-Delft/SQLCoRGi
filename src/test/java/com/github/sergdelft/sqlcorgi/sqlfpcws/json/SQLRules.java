package com.github.sergdelft.sqlcorgi.sqlfpcws.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents the rules in the JSON output of the {@code BulkMainGenerator}.
 *
 * {@link com.google.gson.Gson} is used to enable easy JSON (de)serialization.
 */
public class SQLRules {

    @SerializedName("queryNo")
    @Expose
    private Integer queryNo;

    @SerializedName("pathList")
    @Expose
    private List<String> pathList;

    /**
     * Creates an {@link SQLRules} object.
     *
     * @param queryNo  The index of the query (as related to the order in the SQL input file).
     * @param pathList The list of coverage targets.
     */
    public SQLRules(Integer queryNo, List<String> pathList) {
        this.queryNo = queryNo;
        this.pathList = pathList;
    }

    public Integer getQueryNo() {
        return queryNo;
    }

    public List<String> getPathList() {
        return pathList;
    }
}
