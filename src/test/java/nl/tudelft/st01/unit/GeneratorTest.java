package nl.tudelft.st01.unit;

import nl.tudelft.st01.Generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link Generator}.
 */
public class GeneratorTest {
    private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();

    private static final String NULL_ERROR_MESSAGE = "Input cannot be null.";
    private static final String NOT_AN_SQL_QUERY_ERROR_MESSAGE = "Input query could not be parsed.";
    private static final String NOT_A_SELECT_STATEMENT_ERROR_MESSAGE = "Only SELECT statements are supported.";

    @BeforeEach
    public void setUpStream() {
        System.setErr(new PrintStream(errStream));
    }

    /**
     * Trying to invoke the {@link Generator} constructor should throw an {@link UnsupportedOperationException}.
     *
     * Java Reflection is used because the {@link Generator} constructor is private.
     */
    @Test
    public void testConstructorThrowsException() throws NoSuchMethodException {
        Constructor<Generator> generatorConstructor = Generator.class.getDeclaredConstructor();
        generatorConstructor.setAccessible(true);

        assertThatThrownBy(() -> generatorConstructor.newInstance())
                .hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Assert that the right error message is printed when {@code GenerateRules} is called with a null-query.
     */
    @Test
    public void testGenerateRulesNullShouldPrintErrorMessage(){
        Generator.generateRules(null);

        assertThat(errStream.toString().trim()).isEqualTo(NULL_ERROR_MESSAGE);
    }

    /**
     * Assert that the right error message is printed when {@code GenerateRules} is called with a query that is not
     * valid SQL.
     */
    @Test
    public void testGenerateRulesWithInvalidQueryShouldPrintErrorMessage(){
        Generator.generateRules("This is not a SQL Query");

        assertThat(errStream.toString().trim()).isEqualTo(NOT_AN_SQL_QUERY_ERROR_MESSAGE);
    }

    /**
     * Assert that the right error message is printed when {@code GenerateRules} is called with a non-select statement.
     */
    @Test
    public void testGenerateRulesWithNonSelectQueryShouldPrintErrorMessage() {
        Generator.generateRules("UPDATE Table1 SET column = value WHERE condition IS NOT NULL;");

        assertThat(errStream.toString().trim()).isEqualTo(NOT_A_SELECT_STATEMENT_ERROR_MESSAGE);
    }

}
