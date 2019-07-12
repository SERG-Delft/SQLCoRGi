package com.github.sergdelft.sqlcorgi.functional;

import com.github.sergdelft.sqlcorgi.SQLCorgi;
import com.github.sergdelft.sqlcorgi.exceptions.CannotBeParsedException;
import com.github.sergdelft.sqlcorgi.exceptions.UnsupportedInputException;
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
            () -> SQLCorgi.generateRules("ELECT * ROM invalid WERE statement = 5", null));
    }

    /**
     * A test case to check if a non-select query results in the coorect exception.
     */
    @Test
    void testNonSelectQuery() {
        assertThatExceptionOfType(UnsupportedInputException.class).isThrownBy(
            () -> SQLCorgi.generateRules("ALTER TABLE Customers ADD Email varchar(255)", null));

    }
}
