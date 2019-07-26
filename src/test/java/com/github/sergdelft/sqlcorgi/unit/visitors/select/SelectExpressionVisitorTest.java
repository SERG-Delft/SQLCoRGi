package com.github.sergdelft.sqlcorgi.unit.visitors.select;

import com.github.sergdelft.sqlcorgi.AssertUtils;
import com.github.sergdelft.sqlcorgi.query.NumericDoubleValue;
import com.github.sergdelft.sqlcorgi.query.NumericLongValue;
import com.github.sergdelft.sqlcorgi.query.NumericValue;
import com.github.sergdelft.sqlcorgi.schema.TableStructure;
import com.github.sergdelft.sqlcorgi.visitors.select.SelectExpressionVisitor;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link SelectExpressionVisitor}.
 */
class SelectExpressionVisitorTest {
    private static final String EXCEPTION_MESSAGE = "A SelectExpressionVisitor requires an empty,"
            + " non-null set to which it can write generated expressions.";

    private List<Expression> output;
    private SelectExpressionVisitor selectExpressionVisitor;
    private TableStructure tableStructure;

    /**
     * Set-up a {@link SelectExpressionVisitor} with an empty {@link ArrayList}.
     */
    @BeforeEach
    void setUpSelectExpressionVisitor() {
        output = new ArrayList<>();
        tableStructure = mock(TableStructure.class);
        selectExpressionVisitor = new SelectExpressionVisitor(output, tableStructure);
    }

    /**
     * Test whether initializing a {@link SelectExpressionVisitor} with {@code null} throws the correct exception.
     */
    @Test
    void constructorNullOutputTest() {
        assertThatThrownBy(() -> {
            new SelectExpressionVisitor(null, null);
        }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(EXCEPTION_MESSAGE);
    }

    /**
     * Test whether initializing a {@link SelectExpressionVisitor}
     * with a non-empty {@code output} throws the correct exception.
     */
    @Test
    void constructorNonEmptyOutputTest() {
        List<Expression> output = new ArrayList<>();
        output.add(new GreaterThan());

        assertThatThrownBy(() -> {
            new SelectExpressionVisitor(output, new TableStructure());
        }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(EXCEPTION_MESSAGE);
    }

    /**
     * Assert that the {@code visit} method for an {@link IsNullExpression} generates the correct output.
     */
    @Test
    void visitNullExpressionTest() {
        IsNullExpression isNullExpression = new IsNullExpression();
        Column stringValue = new Column("context");
        isNullExpression.setLeftExpression(stringValue);

        IsNullExpression isNotNullExpression = new IsNullExpression();
        isNotNullExpression.setNot(true);
        isNotNullExpression.setLeftExpression(stringValue);

        selectExpressionVisitor.visit(isNullExpression);

        AssertUtils.compareFieldByField(output, isNullExpression, isNotNullExpression);
    }

    /**
     * Assert that the {@code visit} method for an
     * {@link Between} with Double values generates the correct output.
     */
    @Test
    void visitBetweenWithDoubleValueTest() {
        Column left = new Column("double");
        NumericDoubleValue start = new NumericDoubleValue("2");
        NumericDoubleValue end = new NumericDoubleValue("24");

        when(tableStructure.isNullable(any())).thenReturn(true);
        betweenAssert(left, start, end, true);
    }

    /**
     * Assert that the {@code visit} method for an
     * {@link Between} with Long values generates the correct output.
     */
    @Test
    void visitBetweenWithLongValueTest() {
        Column left = new Column("long");
        NumericLongValue start = new NumericLongValue("1");
        NumericLongValue end = new NumericLongValue("12");

        when(tableStructure.isNullable(any())).thenReturn(true);
        betweenAssert(left, start, end, true);
    }

    /**
     * Assert that the {@code visit} method for an
     * {@link Between} with String values generates the correct output.
     */
    @Test
    void visitBetweenWithStringValueTest() {
        Column left = new Column("string");
        StringValue start = new StringValue("aaa");
        StringValue end = new StringValue("azz");

        when(tableStructure.isNullable(any())).thenReturn(true);
        betweenAssert(left, start, end, false);
    }

    /**
     * Asserts the different tests or BETWEEN expressions.
     * @param left The expression left of the BETWEEN operator
     * @param start The start of the BETWEEN expression
     * @param end The end of the BETWEEN expression
     * @param isNumeric Flag to check whether the BETWEEN expression is numeric
     */
    private void betweenAssert(Expression left, Expression start, Expression end, boolean isNumeric) {
        Between between = new Between();
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

        if (isNumeric) {
            EqualsTo startMinusOne = new EqualsTo();
            startMinusOne.setLeftExpression(left);
            startMinusOne.setRightExpression(((NumericValue) start).add(-1));

            EqualsTo endPlusOne = new EqualsTo();
            endPlusOne.setLeftExpression(left);
            endPlusOne.setRightExpression(((NumericValue) end).add(1));

            selectExpressionVisitor.visit(between);

            AssertUtils.compareFieldByField(output,
                    equalsToStart,
                    equalsToEnd,
                    startMinusOne,
                    endPlusOne,
                    between,
                    notBetween,
                    isNullExpression
            );
        } else {
            selectExpressionVisitor.visit(between);

            AssertUtils.compareFieldByField(output,
                    equalsToStart,
                    equalsToEnd,
                    between,
                    notBetween,
                    isNullExpression
            );
        }
    }

    /**
     * Assert that the {@code visit} method for an {@link InExpression} generates the correct output.
     */
    @Test
    void visitInExpressionTest() {
        InExpression inExpression = new InExpression();
        Column left = new Column("number");
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

        when(tableStructure.isNullable(any())).thenReturn(true);
        selectExpressionVisitor.visit(inExpression);

        AssertUtils.compareFieldByField(output, inExpression, notInExpression, isNullExpression);
    }

    /**
     * Assert that the {@code visit} method for a case-insensitive {@link LikeExpression} generates the correct output.
     */
    @Test
    void visitLikeExpressionTest() {
        LikeExpression likeExpression = new LikeExpression();
        Column leftValue = new Column("x");
        StringValue rightValue = new StringValue("project");
        likeExpression.setLeftExpression(leftValue);
        likeExpression.setRightExpression(rightValue);

        LikeExpression notLikeExpression = new LikeExpression();
        notLikeExpression.setNot(true);
        notLikeExpression.setLeftExpression(leftValue);
        notLikeExpression.setRightExpression(rightValue);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(leftValue);

        when(tableStructure.isNullable(any())).thenReturn(true);
        selectExpressionVisitor.visit(likeExpression);

        AssertUtils.compareFieldByField(output, likeExpression, notLikeExpression, isNullExpression);
    }

    /**
     * Assert that the {@code visit} method for an {@link LikeExpression} generates the correct output.
     */
    @Test
    void visitILikeExpressionTest() {
        LikeExpression likeExpression = new LikeExpression();
        StringValue leftValue = new StringValue("y");
        StringValue rightValue = new StringValue("coverage");
        likeExpression.setLeftExpression(leftValue);
        likeExpression.setRightExpression(rightValue);
        likeExpression.setCaseInsensitive(true);

        LikeExpression notLikeExpression = new LikeExpression();
        notLikeExpression.setNot(true);
        notLikeExpression.setLeftExpression(leftValue);
        notLikeExpression.setRightExpression(rightValue);
        notLikeExpression.setCaseInsensitive(true);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(leftValue);

        when(tableStructure.isNullable(any())).thenReturn(false);
        selectExpressionVisitor.visit(likeExpression);

        AssertUtils.compareFieldByField(output, likeExpression, notLikeExpression);
    }
}
