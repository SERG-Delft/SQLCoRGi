package nl.tudelft.st01.functional;

import nl.tudelft.st01.CoverageRulesGenerator;
import nl.tudelft.st01.util.exceptions.CannotBeParsedException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 *  This test class tests a good flow and a bad flow for the entry point to this tool,
 *  the {@link CoverageRulesGenerator} class.
 */
public class CoverageRulesGeneratorTest {

    /**
     * A test case for the happy flow of the class. This shows what happens when
     * the {@code generateRules()} method is given a good input.
     */
    @Test
    public void goodFlowTest() {
        List<String> result = CoverageRulesGenerator.generateRules(
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
    public void badFlowTest() {
        assertThatExceptionOfType(CannotBeParsedException.class).isThrownBy(
            () -> CoverageRulesGenerator.generateRules("SELEC * FRO invalid WERE statement = 5")
        );
    }
}
