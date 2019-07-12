package com.github.sergdelft.sqlcorgi.unit;

import com.github.sergdelft.sqlcorgi.SQLCorgi;
import com.github.sergdelft.sqlcorgi.exceptions.CannotBeNullException;
import com.github.sergdelft.sqlcorgi.exceptions.CannotBeParsedException;
import com.github.sergdelft.sqlcorgi.exceptions.UnsupportedInputException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link SQLCorgi}.
 */
class SQLCorgiTest {

    /**
     * Trying to invoke the {@link SQLCorgi} constructor should throw an {@code UnsupportedOperationException}.
     *
     * Java Reflection is used because the {@code SQLCorgi} constructor is private.
     *
     * @throws NoSuchMethodException if the {@code SQLCorgi} constructor is not found - this cannot happen.
     */
    @Test
    void testConstructorThrowsException() throws NoSuchMethodException {
        Constructor<SQLCorgi> sqlCorgiConstructor = SQLCorgi.class.getDeclaredConstructor();
        sqlCorgiConstructor.setAccessible(true);

        assertThatThrownBy(sqlCorgiConstructor::newInstance)
                .hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Assert that the right exception is thrown when {@link SQLCorgi#generateRules} is called with a null-query.
     */
    @Test
    void testGenerateRulesNullShouldPrintErrorMessage() {
        assertThatExceptionOfType(CannotBeNullException.class).isThrownBy(
            () -> SQLCorgi.generateRules(null, null)
        );
    }

    /**
     * Assert that the right exception is thrown when {link SQLCorgi#generateRules} is called with a query that is not
     * valid SQL..
     */
    @Test
    void testGenerateRulesWithInvalidQueryShouldPrintErrorMessage() {

        assertThatExceptionOfType(CannotBeParsedException.class).isThrownBy(
            () -> SQLCorgi.generateRules("This is not a SQL Query", null)
        );
    }

    /**
     * Assert that the right exception is thrown when {link SQLCorgi#generateRules} is called with a non-select
     * statement.
     */
    @Test
    void testGenerateRulesWithNonSelectQueryShouldPrintErrorMessage() {

        assertThatExceptionOfType(UnsupportedInputException.class).isThrownBy(
            () -> SQLCorgi.generateRules("UPDATE Table1 SET column = value WHERE condition IS NOT NULL;", null)
        );
    }
}
