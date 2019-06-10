package nl.tudelft.st01.unit;

import nl.tudelft.st01.Generator;

import nl.tudelft.st01.util.exceptions.CannotBeNullException;
import nl.tudelft.st01.util.exceptions.CannotBeParsedException;
import nl.tudelft.st01.util.exceptions.ShouldNotBeInstantiatedException;
import nl.tudelft.st01.util.exceptions.UnsupportedInputException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link Generator}.
 */
public class GeneratorTest {

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
        assertThatExceptionOfType(CannotBeNullException.class).isThrownBy(
            () -> Generator.generateRules(null));
    }

    /**
     * Assert that the right exception is thrown when {@code GenerateRules} is called with a query that is not
     * valid SQL..
     */
    @Test
    public void testGenerateRulesWithInvalidQueryShouldPrintErrorMessage() {

        assertThatExceptionOfType(CannotBeParsedException.class).isThrownBy(
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
