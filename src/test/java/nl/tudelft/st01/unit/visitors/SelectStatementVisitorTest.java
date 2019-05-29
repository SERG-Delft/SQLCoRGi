package nl.tudelft.st01.unit.visitors;

import nl.tudelft.st01.visitors.SelectStatementVisitor;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Unit tests for the {@code SelectStatementVisitorTest}.
 */
public class SelectStatementVisitorTest {

    private static final String EXCEPTION_MESSAGE = "A SelectStatementVisitor requires an empty, non-null set to which "
            + "it can output generated rules.";

    @Test
    public void testConstructorWithNullSetThrowsException() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new SelectStatementVisitor(null)
        );
    }

    @Test
    public void testConstructorWithNonEmptySetThrowsException() {
        Set<String> nonEmptySet = new HashSet<>();
        nonEmptySet.add("");

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new SelectStatementVisitor(nonEmptySet)
        ).withMessage(EXCEPTION_MESSAGE);
    }
}
