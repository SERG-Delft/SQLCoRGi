package com.github.sergdelft.sqlcorgi.unit.sqlfpcws.json;

import com.github.sergdelft.sqlcorgi.sqlfpcws.json.SQLRules;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Class that tests {@link SQLRules}.
 */
class SQLRulesTest {

    private static final int QUERY_NO = 1;
    private static final String QUERY = "SELECT * FROM Universe WHERE Universe.Truth = 42";

    /**
     * Tests that the getter for the {@code QueryNo} correctly returns the query number.
     */
    @Test
    void testGetQueryNo() {
        SQLRules rules = new SQLRules(QUERY_NO, new ArrayList<>());

        assertThat(rules.getQueryNo()).isEqualTo(1);
    }

    /**
     * Tests that the getter for the {@code pathList} correctly returns the list.
     */
    @Test
    void testGetPathList() {
        List<String> pathList = new ArrayList<>();
        pathList.add(QUERY);

        SQLRules rules = new SQLRules(QUERY_NO, pathList);

        assertThat(rules.getPathList()).isEqualTo(pathList);
    }
}
