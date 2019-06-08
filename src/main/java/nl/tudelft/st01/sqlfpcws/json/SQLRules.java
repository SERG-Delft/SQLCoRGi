package nl.tudelft.st01.sqlfpcws.json;

import java.util.ArrayList;
import java.util.List;

public class SQLRules {

    private Integer queryNo;

    private List<String> pathList = new ArrayList<>();

    public SQLRules(Integer queryNo) {
        this.queryNo = queryNo;
    }

    public void addRule(String rule) {
        pathList.add(rule);
    }
}