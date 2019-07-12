package com.github.sergdelft.sqlcorgi.unit.sqlfpcws.json;

import com.github.sergdelft.sqlcorgi.sqlfpcws.json.JSONEntries;
import com.github.sergdelft.sqlcorgi.sqlfpcws.json.SQLRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class that tests {@link JSONEntries}.
 */
class JSONEntriesTest {

    private JSONEntries jsonEntries;

    private static final int QUERYNO_ONE = 1;
    private static final int QUERYNO_TWO = 2;
    private static final int QUERYNO_THREE = 3;

    /**
     * Sets up a {@link JSONEntries} object.
     */
    @BeforeEach
    void setUpJSONEntries() {
        jsonEntries = new JSONEntries();
    }

    /**
     * Assert that a newly created {@link JSONEntries} object returns an empty {@link List} of type {@link String}
     * when the {@code getEntries} method is called.
     */
    @Test
    void testGetEntriesNewlyInstantiatedJSONEntriesObject() {
        assertThat(jsonEntries.getEntries())
                .hasOnlyElementsOfType(SQLRules.class)
                .isEmpty();
    }

    /**
     * Assert that {@code getEntries} correctly returns the one {@link SQLRules} added to it via the {@code addEntry}
     * method.
     */
    @Test
    void testGetEntriesAfterAddingOneEntry() {
        SQLRules rules = new SQLRules(QUERYNO_ONE, new ArrayList<>());

        jsonEntries.addEntry(rules);

        assertThat(jsonEntries.getEntries()).containsExactly(rules);
    }

    /**
     * Assert that {@code getEntries} correctly returns the multiple {@link SQLRules} added to it via the
     * {@code addEntry} method.
     */
    @Test
    void testGetEntriesAfterAddingMultipleEntries() {
        SQLRules rules1 = new SQLRules(QUERYNO_ONE, new ArrayList<>());
        SQLRules rules2 = new SQLRules(QUERYNO_TWO, new ArrayList<>());
        SQLRules rules3 = new SQLRules(QUERYNO_THREE, new ArrayList<>());

        jsonEntries.addEntry(rules1);
        jsonEntries.addEntry(rules2);
        jsonEntries.addEntry(rules3);

        assertThat(jsonEntries.getEntries()).containsExactly(rules1, rules2, rules3);
    }
}
