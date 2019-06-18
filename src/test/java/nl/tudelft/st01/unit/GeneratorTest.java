package nl.tudelft.st01.unit;

import nl.tudelft.st01.Generator;
import nl.tudelft.st01.util.exceptions.CannotBeNullException;
import nl.tudelft.st01.util.exceptions.CannotBeParsedException;
import nl.tudelft.st01.util.exceptions.UnsupportedInputException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link Generator}.
 */
class GeneratorTest {

    /**
     * Trying to invoke the {@link Generator} constructor should throw an {@code UnsupportedOperationException}.
     *
     * Java Reflection is used because the {@code Generator} constructor is private.
     *
     * @throws NoSuchMethodException if the {@code Generator} constructor is not found - this cannot happen.
     */
    @Test
    void testConstructorThrowsException() throws NoSuchMethodException {
        Constructor<Generator> generatorConstructor = Generator.class.getDeclaredConstructor();
        generatorConstructor.setAccessible(true);

        assertThatThrownBy(generatorConstructor::newInstance)
                .hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Assert that the right exception is thrown when {@link Generator#generateRules} is called with a null-query.
     */
    @Test
    void testGenerateRulesNullShouldPrintErrorMessage() {
        assertThatExceptionOfType(CannotBeNullException.class).isThrownBy(
            () -> Generator.generateRules(null)
        );
    }

    /**
     * Assert that the right exception is thrown when {link Generator#generateRules} is called with a query that is not
     * valid SQL..
     */
    @Test
    void testGenerateRulesWithInvalidQueryShouldPrintErrorMessage() {

        assertThatExceptionOfType(CannotBeParsedException.class).isThrownBy(
            () -> Generator.generateRules("This is not a SQL Query")
        );
    }

    /**
     * Assert that the right exception is thrown when {link Generator#generateRules} is called with a non-select
     * statement.
     */
    @Test
    void testGenerateRulesWithNonSelectQueryShouldPrintErrorMessage() {

        assertThatExceptionOfType(UnsupportedInputException.class).isThrownBy(
            () -> Generator.generateRules("UPDATE Table1 SET column = value WHERE condition IS NOT NULL;")
        );
    }
}
