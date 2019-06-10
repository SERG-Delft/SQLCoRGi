package nl.tudelft.st01.unit;

import nl.tudelft.st01.Generator;

import nl.tudelft.st01.util.exceptions.CanNotBeNullException;
import nl.tudelft.st01.util.exceptions.CanNotBeParsedException;
import nl.tudelft.st01.util.exceptions.ShouldNotBeInstantiatedException;
import nl.tudelft.st01.util.exceptions.UnsupportedInputException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

/**
 * Unit tests for the {@link Generator}.
 */
public class GeneratorTest {

    private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();

    private static final String UTF8 = "UTF-8";

    private static final String NULL_ERROR_MESSAGE = "Input cannot be null.";

    private static final String NOT_AN_SQL_QUERY_ERROR_MESSAGE = "Input query could not be parsed.";
    private static final String NOT_A_SELECT_STATEMENT_ERROR_MESSAGE = "Only SELECT statements are supported.";
    private static final String CATCH_EXCEPTION_FAIL_MESSAGE = "UnsupportedEncodingException was thrown";

    /**
     * Trying to invoke the {@link Generator} constructor should throw an {@link UnsupportedOperationException}.
     *
     * Java Reflection is used because the {@link Generator} constructor is private.
     *
     * @throws NoSuchMethodException if the {@link Generator} constructor is not found - this cannot happen.
     */
    @Test
    public void testConstructorThrowsException() throws NoSuchMethodException {
        Constructor<Generator> generatorConstructor = Generator.class.getDeclaredConstructor();
        generatorConstructor.setAccessible(true);

        assertThatThrownBy(
            () -> generatorConstructor.newInstance()).hasRootCauseInstanceOf(ShouldNotBeInstantiatedException.class);
    }

    /**
     * Assert that the right exception is thrown when {@code GenerateRules} is called with a null-query.
     */
    @Test
    public void testGenerateRulesNullShouldPrintErrorMessage() {
        assertThatExceptionOfType(CanNotBeNullException.class).isThrownBy(
                () -> Generator.generateRules(null));
    }

    /**
     * Assert that the right exception is thrown when {@code GenerateRules} is called with a query that is not
     * valid SQL..
     */
    @Test
    public void testGenerateRulesWithInvalidQueryShouldPrintErrorMessage() {

        assertThatExceptionOfType(CanNotBeParsedException.class).isThrownBy(
                () -> Generator.generateRules("This is not a SQL Query"));
    }

    /**
     * Assert that the right exception is thrown when {@code GenerateRules} is called with a non-select statement.
     */
    @Test
    public void testGenerateRulesWithNonSelectQueryShouldPrintErrorMessage() {

        assertThatExceptionOfType(UnsupportedInputException.class).isThrownBy(
                () -> Generator.generateRules("UPDATE Table1 SET column = value WHERE condition IS NOT NULL;"));
    }
}
