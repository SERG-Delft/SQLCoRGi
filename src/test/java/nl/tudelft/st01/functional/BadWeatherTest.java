package nl.tudelft.st01.functional;

import nl.tudelft.st01.Generator;
import org.junit.jupiter.api.Test;

import static nl.tudelft.st01.functional.AssertUtils.verify;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This class exercises several bad weather scenarios for the {@link Generator}.
 */
public class BadWeatherTest {

    /**
     * A test case to check if an invalid query return nothing.
     */
    @Test
    public void testInvalidQuery() {
        verify("ELECT * ROM invalid WERE statement = 5");
    }

    /**
     * A test case to check if a non-select query throws the proper exception.
     */
    @Test
    public void testNonSelectQuery() {
        String query = "ALTER TABLE Customers ADD Email varchar(255);";
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                Generator.generateRules(query));
    }
}
