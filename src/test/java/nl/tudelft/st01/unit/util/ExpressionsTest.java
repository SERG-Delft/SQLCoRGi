package nl.tudelft.st01.unit.util;

import nl.tudelft.st01.util.Expressions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests the {@link Expressions} utility class.
 */
public class ExpressionsTest {

    /**
     * Verifies that {@link Expressions} cannot be instantiated.
     *
     * @throws NoSuchMethodException should not happen.
     */
    @Test
    public void testInstantiationForbidden() throws NoSuchMethodException {

        Constructor<Expressions> constructor = Expressions.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Throwable thrown = catchThrowable(constructor::newInstance);

        assertThat(thrown).hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }

}
