package com.github.sergdelft.sqlcrg.functional;

import com.github.sergdelft.sqlcrg.Generator;
import com.github.sergdelft.sqlcrg.exceptions.CannotBeParsedException;
import com.github.sergdelft.sqlcrg.exceptions.UnsupportedInputException;
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
