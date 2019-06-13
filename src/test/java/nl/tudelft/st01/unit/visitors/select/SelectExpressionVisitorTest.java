package nl.tudelft.st01.unit.visitors.select;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import nl.tudelft.st01.visitors.select.SelectExpressionVisitor;
import nl.tudelft.st01.util.exceptions.CannotBeNullException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@code SelectExpressionVisitorTest}.
 */
public class SelectExpressionVisitorTest {
    private static final String EXCEPTION_MESSAGE = "A SelectExpressionVisitor requires an empty,"
            + " non-null set to which it can write generated expressions.";

    private List<Expression> output;
    private SelectExpressionVisitor selectExpressionVisitor;

    /**
     * Set-up a {@code SelectExpressionVisitor} with an empty {@code ArrayList}.
     */
    @BeforeEach
    public void setUpSelectExpressionVisitor() {
        output = new ArrayList<>();
        selectExpressionVisitor = new SelectExpressionVisitor(output);
    }

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

    /**
     * Assert that the {@code visit} method for an {@code IsNullExpression} generates the correct output.
     */
    @Test
    public void visitNullExpressionTest() {
        IsNullExpression isNullExpression = new IsNullExpression();
        StringValue stringValue = new StringValue("context");
        isNullExpression.setLeftExpression(stringValue);

        IsNullExpression isNotNullExpression = new IsNullExpression();
        isNotNullExpression.setNot(true);
        isNotNullExpression.setLeftExpression(stringValue);

        selectExpressionVisitor.visit(isNullExpression);

        assertThat(output).containsOnly(
                isNullExpression,
                isNotNullExpression
        );
    }
}