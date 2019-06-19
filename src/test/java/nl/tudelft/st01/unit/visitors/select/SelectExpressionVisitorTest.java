package nl.tudelft.st01.unit.visitors.select;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.*;
import nl.tudelft.st01.query.NumericDoubleValue;
import nl.tudelft.st01.query.NumericLongValue;
import nl.tudelft.st01.visitors.select.SelectExpressionVisitor;
import nl.tudelft.st01.util.exceptions.CannotBeNullException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static nl.tudelft.st01.AssertUtils.compareFieldByField;

/**
 * Unit tests for the {@code SelectExpressionVisitorTest}.
 */
class SelectExpressionVisitorTest {
    private static final String EXCEPTION_MESSAGE = "A SelectExpressionVisitor requires an empty,"
            + " non-null set to which it can write generated expressions.";

    private List<Expression> output;
    private SelectExpressionVisitor selectExpressionVisitor;

    /**
     * Set-up a {@code SelectExpressionVisitor} with an empty {@code ArrayList}.
     */
    @BeforeEach
    void setUpSelectExpressionVisitor() {
        output = new ArrayList<>();
        selectExpressionVisitor = new SelectExpressionVisitor(output);
    }

    /**
     * Test whether initializing a {@code SelectExpressionVisitor} with {@code null} throws the correct exception.
     */
    @Test
    void constructorNullOutputTest() {
        assertThatThrownBy(() -> {
            new SelectExpressionVisitor(null);
        }).isInstanceOf(CannotBeNullException.class).hasMessageContaining(EXCEPTION_MESSAGE);
    }

    /**
     * Test whether initializing a {@code SelectExpressionVisitor}
     * with a non-empty {@code output} throws the correct exception.
     */
    @Test
    void constructorNonEmptyOutputTest() {
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
    void visitNullExpressionTest() {
        IsNullExpression isNullExpression = new IsNullExpression();
        StringValue stringValue = new StringValue("context");
        isNullExpression.setLeftExpression(stringValue);

        IsNullExpression isNotNullExpression = new IsNullExpression();
        isNotNullExpression.setNot(true);
        isNotNullExpression.setLeftExpression(stringValue);

        selectExpressionVisitor.visit(isNullExpression);

        compareFieldByField(output, isNullExpression, isNotNullExpression);
    }

    /**
     * Assert that the {@code visit} method for an
     * {@code BetweenExpression} with Double values generates the correct output.
     */
    @Test
    void visitBetweenWithDoubleValueTest() {
        Between between = new Between();
        StringValue left = new StringValue("double");
        NumericDoubleValue start = new NumericDoubleValue("2");
        NumericDoubleValue end = new NumericDoubleValue("24");
        between.setLeftExpression(left);
        between.setBetweenExpressionStart(start);
        between.setBetweenExpressionEnd(end);

        Between notBetween = new Between();
        notBetween.setNot(true);
        notBetween.setLeftExpression(left);
        notBetween.setBetweenExpressionStart(start);
        notBetween.setBetweenExpressionEnd(end);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(left);

        EqualsTo equalsToStart = new EqualsTo();
        equalsToStart.setLeftExpression(left);
        equalsToStart.setRightExpression(start);

        EqualsTo equalsToEnd = new EqualsTo();
        equalsToEnd.setLeftExpression(left);
        equalsToEnd.setRightExpression(end);

        EqualsTo startMinusOne = new EqualsTo();
        startMinusOne.setLeftExpression(left);
        startMinusOne.setRightExpression(start.add(-1));

        EqualsTo endPlusOne = new EqualsTo();
        endPlusOne.setLeftExpression(left);
        endPlusOne.setRightExpression(end.add(1));

        selectExpressionVisitor.visit(between);

        compareFieldByField(output,
                equalsToStart,
                equalsToEnd,
                startMinusOne,
                endPlusOne,
                between,
                notBetween,
                isNullExpression
        );
    }

    /**
     * Assert that the {@code visit} method for an
     * {@code BetweenExpression} with Long values generates the correct output.
     */
    @Test
    void visitBetweenWithLongValueTest() {
        Between between = new Between();
        StringValue left = new StringValue("long");
        NumericLongValue start = new NumericLongValue("1");
        NumericLongValue end = new NumericLongValue("12");
        between.setLeftExpression(left);
        between.setBetweenExpressionStart(start);
        between.setBetweenExpressionEnd(end);

        Between notBetween = new Between();
        notBetween.setNot(true);
        notBetween.setLeftExpression(left);
        notBetween.setBetweenExpressionStart(start);
        notBetween.setBetweenExpressionEnd(end);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(left);

        EqualsTo equalsToStart = new EqualsTo();
        equalsToStart.setLeftExpression(left);
        equalsToStart.setRightExpression(start);

        EqualsTo equalsToEnd = new EqualsTo();
        equalsToEnd.setLeftExpression(left);
        equalsToEnd.setRightExpression(end);

        EqualsTo startMinusOne = new EqualsTo();
        startMinusOne.setLeftExpression(left);
        startMinusOne.setRightExpression(start.add(-1));

        EqualsTo endPlusOne = new EqualsTo();
        endPlusOne.setLeftExpression(left);
        endPlusOne.setRightExpression(end.add(1));

        selectExpressionVisitor.visit(between);

        compareFieldByField(output,
                equalsToStart,
                equalsToEnd,
                startMinusOne,
                endPlusOne,
                between,
                notBetween,
                isNullExpression
        );
    }

    /**
     * Assert that the {@code visit} method for an
     * {@code BetweenExpression} with String values generates the correct output.
     */
    @Test
    void visitBetweenWithStringValueTest() {
        Between between = new Between();
        StringValue left = new StringValue("string");
        StringValue start = new StringValue("aaa");
        StringValue end = new StringValue("azz");
        between.setLeftExpression(left);
        between.setBetweenExpressionStart(start);
        between.setBetweenExpressionEnd(end);

        Between notBetween = new Between();
        notBetween.setNot(true);
        notBetween.setLeftExpression(left);
        notBetween.setBetweenExpressionStart(start);
        notBetween.setBetweenExpressionEnd(end);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(left);

        EqualsTo equalsToStart = new EqualsTo();
        equalsToStart.setLeftExpression(left);
        equalsToStart.setRightExpression(start);

        EqualsTo equalsToEnd = new EqualsTo();
        equalsToEnd.setLeftExpression(left);
        equalsToEnd.setRightExpression(end);

        selectExpressionVisitor.visit(between);

        compareFieldByField(output,
                equalsToStart,
                equalsToEnd,
                between,
                notBetween,
                isNullExpression
        );
    }

    /**
     * Assert that the {@code visit} method for an {@code InExpression} generates the correct output.
     */
    @Test
    void visitInExpressionTest() {
        InExpression inExpression = new InExpression();
        StringValue left = new StringValue("number");
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

        compareFieldByField(output, inExpression, notInExpression, isNullExpression);
    }

    /**
     * Assert that the {@code visit} method for an {@code LikeExpression} generates the correct output.
     */
    @Test
    void visitLikeExpressionTest() {
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

        compareFieldByField(output, likeExpression, notLikeExpression, isNullExpression);
    }
}
