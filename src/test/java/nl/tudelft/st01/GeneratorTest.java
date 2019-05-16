package nl.tudelft.st01;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Contains tests for {@link Generator}.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
public class GeneratorTest {

    /**
     * A test case for a simple query containing only one condition with < as operator.
     */
    @Test
    public void testLessThanInteger() {
        String query = "SELECT * FROM table WHERE a < 100";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM table WHERE a = 99");
        expected.add("SELECT * FROM table WHERE a = 100");
        expected.add("SELECT * FROM table WHERE a = 101");
        expected.add("SELECT * FROM table WHERE a IS NULL");

        assertEquals(expected, result);
    }

    /**
     * A test case for a simple query containing only one condition with != as operator.
     */
    @Test
    public void testNotEqualToFloat() {
        String query = "SELECT * FROM table WHERE a <> 0.0";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM table WHERE a = -1.0");
        expected.add("SELECT * FROM table WHERE a = 0.0");
        expected.add("SELECT * FROM table WHERE a = 1.0");
        expected.add("SELECT * FROM table WHERE a IS NULL");

        assertEquals(expected, result);
    }

    /**
     * A test case for a simple query testing for string equality.
     */
    @Test
    public void testEqualToString() {
        String query = "SELECT * FROM table WHERE a = 'qwerty'";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM table WHERE a = 'qwerty'");
        expected.add("SELECT * FROM table WHERE a <> 'qwerty'");
        expected.add("SELECT * FROM table WHERE a IS NULL");

        assertEquals(expected, result);
    }

    /**
     * A test case for a simple query with IS NULL.
     */
    @Test
    public void testIsNull() {
        String query = "SELECT * FROM table WHERE a IS NOT NULL";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM table WHERE a IS NOT NULL");
        expected.add("SELECT * FROM table WHERE a IS NULL");

        assertEquals(expected, result);
    }

    /**
     * A test case with three conditions, combined with AND and OR.
     */
    @Test
    public void testThreeConditionsAndOr() {
        String query = "SELECT * FROM Table1 WHERE a1 = 11 OR a2 = 22 AND a3 = 33";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();
        expected.add("SELECT * FROM Table1 WHERE (a1 = 10) AND NOT (a2 = 22 AND a3 = 33)");
        expected.add("SELECT * FROM Table1 WHERE (a1 = 11) AND NOT (a2 = 22 AND a3 = 33)");
        expected.add("SELECT * FROM Table1 WHERE (a1 = 12) AND NOT (a2 = 22 AND a3 = 33)");
        expected.add("SELECT * FROM Table1 WHERE (a1 IS NULL) AND NOT (a2 = 22 AND a3 = 33)");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 21) AND (a3 = 33))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 33))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 23) AND (a3 = 33))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 IS NULL) AND (a3 = 33))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 32))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 = 34))");
        expected.add("SELECT * FROM Table1 WHERE NOT (a1 = 11) AND ((a2 = 22) AND (a3 IS NULL))");

        assertEquals(expected, result);
    }


    /**
     * A test case with 1 column and 1 aggregator, in this case AVG.
     */
    @Test
    public void testAVGAggregator1column1Aggr() {
        String query = "SELECT Director, AVG(Length) FROM Movies GROUP BY Director";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();

        // Newlines needed to not excess line length
        expected.add("SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Director) > 1");

        expected.add("SELECT Director, AVG(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(*) > 1");

        expected.add("SELECT Director, AVG(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(*) > COUNT(Length) AND COUNT(DISTINCT Length) > 1");

        expected.add("SELECT Director, AVG(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1");

        assertEquals(expected, result);
    }

    /**
     * A test case with 1 column and 2 aggregators, in this case AVG and Sum.
     */
    @Test
    public void testSUMAVGAggregator1column2Aggr() {
        String query = "SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();

        // Newlines needed to not excess line length
        expected.add("SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Director) > 1");
        expected.add("SELECT Director, AVG(Score), SUM(Length) FROM Movies "
                    + "GROUP BY Director HAVING COUNT(*) > 1");

        expected.add("SELECT Director, AVG(Score), SUM(Length) FROM Movies "
                    + "GROUP BY Director HAVING COUNT(*) > COUNT(Score) AND COUNT(DISTINCT Score) > 1");

        expected.add("SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(*) > COUNT(Length) AND COUNT(DISTINCT Length) > 1");

        expected.add("SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(Score) > COUNT(DISTINCT Score) AND COUNT(DISTINCT Score) > 1");

        expected.add("SELECT Director, AVG(Score), SUM(Length) FROM Movies GROUP BY Director "
                    + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1");

        assertEquals(expected, result);
    }

    /**
     * A test case with 2 columns and 1 aggregator, in this case MAX.
     */
    @Test
    public void testMAXAggregator2columns1Aggr() {
        String query = "SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name";
        Set<String> result = Generator.generateRules(query);

        Set<String> expected = new TreeSet<>();

        expected.add("SELECT COUNT(*) FROM Movies HAVING COUNT(DISTINCT Name) > 1");

        expected.add("SELECT Director, Name, MAX(Length) FROM Movies "
                + "GROUP BY Name HAVING COUNT(*) > 1");

        expected.add("SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name "
                + "HAVING COUNT(*) > COUNT(Length) AND COUNT(DISTINCT Length) > 1");

        expected.add("SELECT Director, Name, MAX(Length) FROM Movies GROUP BY Name "
                + "HAVING COUNT(Length) > COUNT(DISTINCT Length) AND COUNT(DISTINCT Length) > 1");

        assertEquals(expected, result);
    }

}
