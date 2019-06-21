package nl.tudelft.st01.unit.visitors.select;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.schema.Column;
import nl.tudelft.st01.exceptions.CannotBeNullException;
import nl.tudelft.st01.visitors.select.SelectValueVisitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Unit tests for the {@link SelectValueVisitor}.
 */
class SelectValueVisitorTest {
    private static final String EXCEPTION_MESSAGE = "A SelectValueVisitor requires an empty,"
            + " non-null set to which it can write generated mutations.";

    /**
     * Assert that the proper exception is thrown when the constructor is provided a null argument.
     */
    @Test
    void testConstructorWithNullSetThrowsException() {
        assertThatExceptionOfType(CannotBeNullException.class).isThrownBy(
            () -> new SelectValueVisitor(new Column("variable"), null)
        ).withMessage(EXCEPTION_MESSAGE);
    }

    /**
     * Assert that the proper exception is thrown when the constructor is provided a non-empty set.
     */
    @Test
    void testConstructorWithNonEmptySetThrowsException() {
        List<Expression> nonEmptySet = new ArrayList<>();
        nonEmptySet.add(new GreaterThan());

        assertThatExceptionOfType(CannotBeNullException.class).isThrownBy(
            () -> new SelectValueVisitor(new Column("column"), nonEmptySet)
        ).withMessage(EXCEPTION_MESSAGE);
    }
}
