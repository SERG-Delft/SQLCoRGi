package com.github.sergdelft.sqlcorgi.unit;

import com.github.sergdelft.sqlcorgi.CoverageRuleGenerator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for {@link CoverageRuleGenerator}.
 */
class CoverageRuleGeneratorTest {

    /**
     * Tests whether {@link CoverageRuleGenerator} cannot be instantiated.
     *
     * @throws NoSuchMethodException if the {@code CoverageRuleGenerator} constructor cannot be found.
     */
    @Test
    void testConstructorThrowsException() throws NoSuchMethodException {

        Constructor<CoverageRuleGenerator> generatorConstructor = CoverageRuleGenerator.class.getDeclaredConstructor();
        generatorConstructor.setAccessible(true);

        assertThatThrownBy(generatorConstructor::newInstance)
                .hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }
}
