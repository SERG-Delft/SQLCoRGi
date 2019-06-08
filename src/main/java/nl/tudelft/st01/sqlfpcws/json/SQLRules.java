package nl.tudelft.st01.sqlfpcws.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SQLRules {

    @SerializedName("queryNo")
    @Expose
    private Integer queryNo;

    @SerializedName("pathList")
    @Expose
    private List<String> pathList = new ArrayList<>();

    public SQLRules(Integer queryNo) {
        this.queryNo = queryNo;
    }

    public SQLRules(Integer queryNo, List<String> pathList) {
        this.queryNo = queryNo;
        this.pathList = pathList;
    }

    public void addRule(String rule) {
        pathList.add(rule);
    }
}