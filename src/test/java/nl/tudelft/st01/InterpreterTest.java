package nl.tudelft.st01;

import junit.framework.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class InterpreterTest {

    Interpreter interpreter = new Interpreter();

    /**
     * Rigorous test.
     */
    @Test
    public void returnsEmptyList() {
        Assert.assertEquals(interpreter.interpret(""), new ArrayList<String>());
    }

    /**
     * Rigorous test.
     */
    @Test
    public void oneCondition() {
        String query = "SELECT * FROM Movies WHERE year > 1950";

        List<String> result = Arrays.asList(
                "SELECT * FROM Movies WHERE year = 1951",
                "SELECT * FROM Movies WHERE year = 1950",
                "SELECT * FROM Movies WHERE year = 1949",
                "SELECT * FROM Movies WHERE year IS NULL");

        Collections.sort(result);
        Assert.assertEquals(result, interpreter.interpret(query));
    }



}
