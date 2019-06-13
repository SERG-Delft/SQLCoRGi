package nl.tudelft.st01.unit.visitors.select;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import nl.tudelft.st01.visitors.select.SelectExpressionVisitor;
import nl.tudelft.st01.util.exceptions.CannotBeNullException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@code SelectExpressionVisitorTest}.
 */
public class SelectExpressionVisitorTest {
    private static final String EXCEPTION_MESSAGE = "A SelectExpressionVisitor requires an empty,"
            + " non-null set to which it can write generated expressions.";
    
    /**
     * Test whether initializing a {@code SelectExpressionVisitor} with {@code null} throws the correct exception.
     */
    @Test
    public void constructorNullOutputTest() {
        assertThatThrownBy(() -> {
            new SelectExpressionVisitor(null);
        }).isInstanceOf(CannotBeNullException.class).hasMessageContaining(EXCEPTION_MESSAGE);
    }
    
    /**
     * Test whether initializing a {@code SelectExpressionVisitor}
     * with a non-empty {@code output} throws the correct exception.
     */
    @Test
    public void constructorNonEmptyOutputTest() {
        List<Expression> output = new ArrayList<>();
        output.add(new GreaterThan());
        
        assertThatThrownBy(() -> {
            new SelectExpressionVisitor(output);
        }).isInstanceOf(CannotBeNullException.class).hasMessageContaining(EXCEPTION_MESSAGE);
    }