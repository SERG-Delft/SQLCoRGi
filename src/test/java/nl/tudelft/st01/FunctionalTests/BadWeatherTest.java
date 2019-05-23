package nl.tudelft.st01.FunctionalTests;

import nl.tudelft.st01.Generator;
import org.junit.jupiter.api.Test;

import static nl.tudelft.st01.FunctionalTests.AssertUtils.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BadWeatherTest {

    /**
     * A test case to check if an invalid query return nothing.
     */
    @Test
    public void testInvalidQuery() {
        assertEquals("ELECT * ROM invalid WERE statement = 5");
    }

    /**
     * A test case to check if a non-select query throws the proper exception.
     */
    @Test
    public void testNonSelectQuery() {
        String query = "ALTER TABLE Customers ADD Email varchar(255);";
        assertThrows(IllegalArgumentException.class, () ->
                Generator.generateRules(query)
        );
    }
}
