package nl.tudelft.st01.unit;

import nl.tudelft.st01.SubqueryGenerator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contains tests for {@link SubqueryGenerator}.
 */
class SubqueryGeneratorTest {

    /**
     * Tests whether {@link SubqueryGenerator}s cannot be instantiated.
     *
     * @throws NoSuchMethodException if the {@code SubqueryGenerator} constructor cannot be found.
     */
    @Test
    void testConstructorThrowsException() throws NoSuchMethodException {

        Constructor<SubqueryGenerator> generatorConstructor = SubqueryGenerator.class.getDeclaredConstructor();
        generatorConstructor.setAccessible(true);

        assertThatThrownBy(generatorConstructor::newInstance)
                .hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }
}
