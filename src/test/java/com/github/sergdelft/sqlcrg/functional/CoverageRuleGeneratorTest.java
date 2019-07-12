package com.github.sergdelft.sqlcrg.functional;

import com.github.sergdelft.sqlcrg.CoverageRuleGenerator;
import com.github.sergdelft.sqlcrg.exceptions.CannotBeParsedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 *  This test class tests a good flow and a bad flow for the entry point to this tool,
 *  the {@link CoverageRuleGenerator} class.
 */
class CoverageRuleGeneratorTest {

    /**
     * A test case for the happy flow of the class. This shows what happens when
     * the {@code generateRules()} method is given a good input.
     */
    @Test
    void goodFlowTest() {
        List<String> result = CoverageRuleGenerator.generateRules(
                "SELECT COUNT(*) FROM Movies WHERE length_minutes < 100"
        );

        assertThat(result).containsExactlyInAnyOrder(
                "SELECT COUNT(*) FROM Movies WHERE length_minutes = 101",
                "SELECT COUNT(*) FROM Movies WHERE length_minutes = 100",
                "SELECT COUNT(*) FROM Movies WHERE length_minutes = 99",
                "SELECT COUNT(*) FROM Movies WHERE length_minutes IS NULL"
        );

    }

    /**
     * A test case for the bad flow of the class, which shows that exceptions are thrown correctly
     * when the {@code generateRules()} method is given a bad input.
     */
    @Test
    void badFlowTest() {
        assertThatExceptionOfType(CannotBeParsedException.class).isThrownBy(
            () -> CoverageRuleGenerator.generateRules("SELEC * FRO invalid WERE statement = 5")
        );
    }
}
