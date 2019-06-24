package nl.tudelft.st01.unit.sqlfpcws.json;

import nl.tudelft.st01.sqlfpcws.json.SQLRules;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Class that tests {@link SQLRules}.
 */
public class SQLRulesTest {

    public static final int QUERY_NO = 1;
    public static final String QUERY = "SELECT * FROM Universe WHERE Universe.Truth = 42";

    /**
     * Tests that the getter for the {@code QueryNo} correctly returns the query number.
     */
    @Test
    public void testGetQueryNo() {
        SQLRules rules = new SQLRules(QUERY_NO, new ArrayList<>());

        assertThat(rules.getQueryNo()).isEqualTo(1);
    }

    /**
     * Tests that the getter for the {@code pathList} correctly returns the list.
     */
    @Test
    public void testGetPathList() {
        List<String> pathList = new ArrayList<>();
        pathList.add(QUERY);

        SQLRules rules = new SQLRules(QUERY_NO, pathList);

        assertThat(rules.getPathList()).isEqualTo(pathList);
    }
}
