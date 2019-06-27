package nl.tudelft.st01.functional;

import nl.tudelft.st01.Generator;
import nl.tudelft.st01.exceptions.CannotBeParsedException;
import nl.tudelft.st01.exceptions.UnsupportedInputException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This class exercises several bad weather scenarios for the {@code Generator}.
 */
class BadWeatherTest {

    /**
     * A test case to check if an invalid query results in the correct exception.
     *
     */
    @Test
    void testInvalidQuery() {
        assertThatExceptionOfType(CannotBeParsedException.class).isThrownBy(
            () -> Generator.generateRules("ELECT * ROM invalid WERE statement = 5"));
    }

    /**
     * A test case to check if a non-select query results in the coorect exception.
     */
    @Test
    void testNonSelectQuery() {
        assertThatExceptionOfType(UnsupportedInputException.class).isThrownBy(
            () -> Generator.generateRules("ALTER TABLE Customers ADD Email varchar(255)"));

    }
}
