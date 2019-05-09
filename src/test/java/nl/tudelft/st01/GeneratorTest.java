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

    //    /**
    //     * A test case for a simple query testing for string equality.
    //     */
    //    @Test
    //    public void testEqualToString() {
    //        String query = "SELECT * FROM table WHERE a = 'qwerty'";
    //        Set<String> result = Generator.generateRules(query);
    //
    //        Set<String> expected = new TreeSet<>();
    //        expected.add("SELECT * FROM table WHERE a = 'qwerty'");
    //        expected.add("SELECT * FROM table WHERE NOT(a = 'qwerty')");
    //        expected.add("SELECT * FROM table WHERE a IS NULL");
    //
    //        assertEquals(expected, result);
    //    }

}
