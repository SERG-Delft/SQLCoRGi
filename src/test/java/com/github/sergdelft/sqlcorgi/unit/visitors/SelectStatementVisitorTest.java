package com.github.sergdelft.sqlcorgi.unit.visitors;

import com.github.sergdelft.sqlcorgi.visitors.SelectStatementVisitor;
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

    /**
     * Assert that the proper exception is thrown when the constructor is provided a null argument.
     */
    @Test
    public void testConstructorWithNullSetThrowsException() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> new SelectStatementVisitor(null, null)
        );
    }

    /**
     * Assert that the proper exception is thrown when the constructor is provided a non-empty set.
     */
    @Test
    public void testConstructorWithNonEmptySetThrowsException() {
        Set<String> nonEmptySet = new HashSet<>();
        nonEmptySet.add("");

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> new SelectStatementVisitor(null, nonEmptySet)
        ).withMessage(EXCEPTION_MESSAGE);
    }
}
