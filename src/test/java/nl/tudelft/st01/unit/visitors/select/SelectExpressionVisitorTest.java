package nl.tudelft.st01.unit.visitors.select;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.*;
import nl.tudelft.st01.visitors.select.SelectExpressionVisitor;
import nl.tudelft.st01.util.exceptions.CannotBeNullException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

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

        assertThat(output.get(0)).isEqualToComparingFieldByFieldRecursively(isNullExpression);
        assertThat(output.get(1)).isEqualToComparingFieldByFieldRecursively(isNotNullExpression);
    }

    /**
     * Assert that the {@code visit} method for an {@code InExpression} generates the correct output.
     */
    @Test
    public void visitInExpressionTest() {
        InExpression inExpression = new InExpression();
        StringValue left = new StringValue("x");
        ExpressionList right = new ExpressionList();
        List<Expression> itemList = new ArrayList<>();
        DoubleValue item = new DoubleValue("28");

        itemList.add(item);
        right.setExpressions(itemList);
        inExpression.setLeftExpression(left);
        inExpression.setRightItemsList(right);

        InExpression notInExpression = new InExpression();
        notInExpression.setNot(true);
        notInExpression.setLeftExpression(left);
        notInExpression.setRightItemsList(right);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(left);

        selectExpressionVisitor.visit(inExpression);

        assertThat(output.get(0)).isEqualToComparingFieldByFieldRecursively(inExpression);
        assertThat(output.get(1)).isEqualToComparingFieldByFieldRecursively(notInExpression);
        assertThat(output.get(2)).isEqualToComparingFieldByFieldRecursively(isNullExpression);
    }

    /**
     * Assert that the {@code visit} method for an {@code LikeExpression} generates the correct output.
     */
    @Test
    public void visitLikeExpressionTest() {
        LikeExpression likeExpression = new LikeExpression();
        StringValue leftValue = new StringValue("x");
        StringValue rightValue = new StringValue("project");
        likeExpression.setLeftExpression(leftValue);
        likeExpression.setRightExpression(rightValue);

        LikeExpression notLikeExpression = new LikeExpression();
        notLikeExpression.setNot();
        notLikeExpression.setLeftExpression(leftValue);
        notLikeExpression.setRightExpression(rightValue);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(leftValue);

        selectExpressionVisitor.visit(likeExpression);

        assertThat(output.get(0)).isEqualToComparingFieldByFieldRecursively(likeExpression);
        assertThat(output.get(1)).isEqualToComparingFieldByFieldRecursively(notLikeExpression);
        assertThat(output.get(2)).isEqualToComparingFieldByFieldRecursively(isNullExpression);
    }
}