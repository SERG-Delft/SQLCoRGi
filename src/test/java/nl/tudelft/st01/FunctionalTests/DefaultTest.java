package nl.tudelft.st01.FunctionalTests;

import java.util.Arrays;
import java.util.TreeSet;

public class DefaultTest {

    private String displayName;
    private String query;
    private TreeSet<String> expected;

    public DefaultTest(String displayName, String query, String... expected) {
        this.displayName = displayName;
        this.query = query;
        this.expected = new TreeSet<>(Arrays.asList(expected));
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getQuery() {
        return query;
    }

    public TreeSet<String> getExpected() {
        return expected;
    }
}
