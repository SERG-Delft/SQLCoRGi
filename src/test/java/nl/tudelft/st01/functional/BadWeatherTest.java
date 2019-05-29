package nl.tudelft.st01.functional;

import org.junit.jupiter.api.Test;

import static nl.tudelft.st01.functional.AssertUtils.verify;

/**
 * This class exercises several bad weather scenarios for the {@code Generator}.
 */
public class BadWeatherTest {

    /**
     * A test case to check if an invalid query returns nothing.
     */
    @Test
    public void testInvalidQuery() {
        verify("ELECT * ROM invalid WERE statement = 5");
    }

    /**
     * A test case to check if a non-select query returns nothing.
     */
    @Test
    public void testNonSelectQuery() {
        verify("ALTER TABLE Customers ADD Email varchar(255)");
    }
}
