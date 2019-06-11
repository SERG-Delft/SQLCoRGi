package nl.tudelft.st01.unit.sqlfpcws.json;

import nl.tudelft.st01.sqlfpcws.json.SQLRules;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

public class SQLRulesTest {

    public static final int QUERY_NO = 1;
    public static final String QUERY = "SELECT * FROM Universe WHERE Universe.Truth = 42";


    @Test
    public void testGetQueryNo() {
        SQLRules rules = new SQLRules(QUERY_NO, new ArrayList<>());

        assertThat(rules.getQueryNo()).isEqualTo(1);
    }

    @Test
    public void testGetPathList() {
        List<String> pathList = new ArrayList<>();
        pathList.add(QUERY);

        SQLRules rules = new SQLRules(QUERY_NO, pathList);

        assertThat(rules.getPathList()).isEqualTo(pathList);
    }
}
