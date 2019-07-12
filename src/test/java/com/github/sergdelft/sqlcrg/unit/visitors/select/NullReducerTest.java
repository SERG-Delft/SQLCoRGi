package com.github.sergdelft.sqlcrg.unit.visitors.select;

import com.github.sergdelft.sqlcrg.visitors.select.NullReducer;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Contains tests for {@link NullReducer}.
 */
// Justification: For some methods it's necessary to check the status of multiple subexpressions, and that of the
// NullReducer itself.
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class NullReducerTest {

    private static final String COLUMN_A = "a";
    private static final String COLUMN_B = "b";

    private Set<String> nulls;
    private NullReducer nullReducer;

    /**
     * Creates a {@link NullReducer} using a new {@link HashSet}.
     */
    @BeforeEach
    void setUp() {
        nulls = new HashSet<>();
        nullReducer = new NullReducer(nulls);
    }

    /**
     * Tests whether the constructor correctly initializes a {@link NullReducer}.
     */
    @Test
    void testConstructor() {

        nulls.add(COLUMN_A);

        assertThat(nullReducer.getNulls()).isSameAs(nulls);
        assertThat(nullReducer.getChild()).isNull();
        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@link NullReducer#getRoot(Expression)} returns the root of the original expression if it does
     * not need to be replaced.
     */
    @Test
    void testGetRootSame() {

        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(COLUMN_A));
        equalsTo.setRightExpression(new NullValue());

        nullReducer.visit(equalsTo);

        assertThat(nullReducer.getRoot(equalsTo)).isSameAs(equalsTo);
    }

    /**
     * Tests whether {@link NullReducer#getRoot(Expression)} returns a subexpression of the original expression if
     * the root needs to be replaced.
     */
    @Test
    void testGetRootDifferent() {

        nulls.add(COLUMN_A);

        NullValue nullValue = new NullValue();
        AndExpression andExpression = new AndExpression(new Column(COLUMN_A), nullValue);

        nullReducer.visit(andExpression);

        assertThat(nullReducer.getRoot(andExpression)).isSameAs(nullValue);
    }

    /**
     * Tests whether {@code NullReducer#visitLogicalOperator(BinaryExpression)} leaves a logical operator unchanged
     * if neither its left nor right subexpressions needed to be changed.
     */
    @Test
    void testVisitLogicalOperator() {

        Column column = new Column(COLUMN_A);
        NullValue nullValue = new NullValue();

        OrExpression orExpression = new OrExpression(column, nullValue);

        nullReducer.visit(orExpression);

        assertThat(orExpression.getLeftExpression()).isSameAs(column);
        assertThat(orExpression.getRightExpression()).isSameAs(nullValue);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@code NullReducer#visitLogicalOperator(BinaryExpression)} updates the left subexpression of a
     * logical operator when instructed by the update child signal.
     */
    @Test
    void testVisitLogicalOperatorUpdateLeft() {

        nulls.add(COLUMN_A);

        Column column = new Column(COLUMN_A);
        NullValue nullValue = new NullValue();

        OrExpression orExpression = new OrExpression(column, nullValue);

        LongValue longValue = new LongValue(1);
        AndExpression andExpression = new AndExpression(orExpression, longValue);

        nullReducer.visit(andExpression);

        assertThat(andExpression.getLeftExpression()).isSameAs(nullValue);
        assertThat(andExpression.getRightExpression()).isSameAs(longValue);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@code NullReducer#visitLogicalOperator(BinaryExpression)} updates the right subexpression of a
     * logical operator when instructed by the update child signal.
     */
    @Test
    void testVisitLogicalOperatorUpdateRight() {

        nulls.add(COLUMN_A);

        Column column = new Column(COLUMN_A);
        NullValue nullValue = new NullValue();
        StringValue stringValue = new StringValue(COLUMN_A);

        OrExpression orExpression = new OrExpression(column, stringValue);

        OrExpression root = new OrExpression(nullValue, orExpression);

        nullReducer.visit(root);

        assertThat(root.getLeftExpression()).isSameAs(nullValue);
        assertThat(root.getRightExpression()).isSameAs(stringValue);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@code NullReducer#visitLogicalOperator(BinaryExpression)} updates both subexpressions of a
     * logical operator when instructed by the update child signal.
     */
    @Test
    void testVisitLogicalOperatorUpdateBoth() {

        nulls.add(COLUMN_A);
        nulls.add(COLUMN_B);

        Column columnA = new Column(COLUMN_A);
        LongValue leftLong = new LongValue(0);
        OrExpression left = new OrExpression(columnA, leftLong);

        Column columnB = new Column(COLUMN_B);
        LongValue rightLong = new LongValue(1);
        OrExpression right = new OrExpression(rightLong, columnB);

        AndExpression andExpression = new AndExpression(left, right);

        nullReducer.visit(andExpression);

        assertThat(andExpression.getLeftExpression()).isSameAs(leftLong);
        assertThat(andExpression.getRightExpression()).isSameAs(rightLong);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@code NullReducer#visitLogicalOperator(BinaryExpression)} tells the parent of the logical
     * operator to replace it with its right child it if its left child has to be removed.
     */
    @Test
    void testVisitLogicalOperatorRemoveLeft() {

        nulls.add(COLUMN_A);

        Column column = new Column(COLUMN_A);
        NullValue nullValue = new NullValue();

        AndExpression andExpression = new AndExpression(column, nullValue);

        nullReducer.visit(andExpression);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isSameAs(nullValue);
    }

    /**
     * Tests whether {@code NullReducer#visitLogicalOperator(BinaryExpression)} tells the parent of the logical
     * operator to replace it with its left child it if its right child has to be removed.
     */
    @Test
    void testVisitLogicalOperatorRemoveRight() {

        nulls.add(COLUMN_B);

        Column column = new Column(COLUMN_B);
        NullValue nullValue = new NullValue();

        OrExpression orExpression = new OrExpression(nullValue, column);

        nullReducer.visit(orExpression);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isSameAs(nullValue);
    }

    /**
     * Tests whether {@code NullReducer#visitLogicalOperator(BinaryExpression)} tells the parent of a logical
     * expression whose subexpressions need to be removed to remove the logical operator.
     */
    @Test
    void testVisitLogicalOperatorRemoveBoth() {

        nulls.add(COLUMN_A);
        nulls.add(COLUMN_B);

        Column columnA = new Column(COLUMN_A);
        Column columnB = new Column(COLUMN_B);
        OrExpression orExpression = new OrExpression(columnA, columnB);

        nullReducer.visit(orExpression);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isNull();
    }

    /**
     * Tests whether {@code NullReducer#visitBinaryExpression(BinaryExpression)} leaves binary expressions unchanged
     * if they do not contain attributes that need to be removed.
     */
    @Test
    void testVisitBinaryNoChange() {

        Column column = new Column(COLUMN_A);
        LongValue longValue = new LongValue(1);

        MinorThan minorThan = new MinorThan();
        minorThan.setLeftExpression(column);
        minorThan.setRightExpression(longValue);

        nullReducer.visit(minorThan);

        assertThat(minorThan.getLeftExpression()).isSameAs(column);
        assertThat(minorThan.getRightExpression()).isSameAs(longValue);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@code NullReducer#visitBinaryExpression(BinaryExpression)} signals the parent of the binary
     * expression to remove it if its left subexpression needs to be removed.
     */
    @Test
    void testVisitBinaryRemoveLeft() {

        nulls.add(COLUMN_A);

        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(new Column(COLUMN_A));
        greaterThan.setRightExpression(new StringValue(COLUMN_A));

        nullReducer.visit(greaterThan);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isNull();
    }

    /**
     * Tests whether {@code NullReducer#visitBinaryExpression(BinaryExpression)} signals the parent of the binary
     * expression to remove it if its right subexpression needs to be removed.
     */
    @Test
    void testVisitBinaryRemoveRight() {

        nulls.add(COLUMN_A);

        Addition addition = new Addition();
        addition.setLeftExpression(new DoubleValue("1.0"));
        addition.setRightExpression(new Column(COLUMN_A));

        nullReducer.visit(addition);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isNull();
    }

    /**
     * Tests whether {@link NullReducer#visit(NotExpression)} leaves the NOT expression unchanged, and does not
     * signal its parent to remove it.
     */
    @Test
    void testVisitNotUnchanged() {

        DateValue dateValue = new DateValue("'2000-01-01'");
        NotExpression notExpression = new NotExpression(dateValue);

        nullReducer.visit(notExpression);

        assertThat(notExpression.getExpression()).isSameAs(dateValue);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@link NullReducer#visit(NotExpression)} signals the parent of the NOT expression to remove it
     * if its subexpression needs to be removed.
     */
    @Test
    void testVisitNotRemoveSub() {

        nulls.add(COLUMN_A);

        Column column = new Column(COLUMN_A);
        NotExpression notExpression = new NotExpression(column);

        nullReducer.visit(notExpression);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isNull();
    }

    /**
     * Tests whether {@link NullReducer#visit(NotExpression)} does not signal the parent of the NOT expression if its
     * subexpression needs to be updated.
     */
    @Test
    void testVisitNotUpdateSub() {

        nulls.add(COLUMN_A);

        NumericBind numericBind = new NumericBind();
        AndExpression andExpression = new AndExpression(new Column(COLUMN_A), numericBind);
        NotExpression notExpression = new NotExpression(andExpression);

        nullReducer.visit(notExpression);

        assertThat(notExpression.getExpression()).isSameAs(numericBind);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@link NullReducer#visit(IsNullExpression)} does not signal the parent of an IS NULL expression
     * to remove it.
     */
    @Test
    void testVisitIsNull() {

        nulls.add(COLUMN_A);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(new Column(COLUMN_A));

        nullReducer.visit(isNullExpression);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@link NullReducer#visit(IsNullExpression)} signals the parent of an IS NOT NULL expression
     * to remove it if its column is in {@code nulls}.
     */
    @Test
    void testVisitIsNotNullRemove() {

        nulls.add(COLUMN_A);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setNot(true);
        isNullExpression.setLeftExpression(new Column(COLUMN_A));

        nullReducer.visit(isNullExpression);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isNull();
    }

    /**
     * Tests whether {@link NullReducer#visit(SubSelect)} is treated as a leaf.
     */
    @Test
    void testVisitSubSelect() {

        SubSelect subSelect = mock(SubSelect.class);

        nullReducer.visit(subSelect);

        verifyNoMoreInteractions(subSelect);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@link NullReducer#visit(CaseExpression)} is left intact if its subexpressions are unchanged.
     */
    @Test
    void testVisitCaseExpression() {

        CaseExpression caseExpression = new CaseExpression();

        NullValue switchExpression = new NullValue();
        caseExpression.setSwitchExpression(switchExpression);

        WhenClause whenClause = mock(WhenClause.class);
        List<WhenClause> whenClauses = new ArrayList<>(1);
        whenClauses.add(whenClause);
        caseExpression.setWhenClauses(whenClauses);

        NullValue elseExpression = new NullValue();
        caseExpression.setElseExpression(elseExpression);

        nullReducer.visit(caseExpression);

        assertThat(nullReducer.isUpdateChild()).isFalse();

        assertThat(caseExpression.getSwitchExpression()).isSameAs(switchExpression);
        assertThat(caseExpression.getWhenClauses()).isSameAs(whenClauses);
        assertThat(caseExpression.getWhenClauses().size()).isEqualTo(whenClauses.size());
        assertThat(caseExpression.getElseExpression()).isSameAs(elseExpression);
    }

    /**
     * Tests whether {@link NullReducer#visit(CaseExpression)} signals the removal of a CASE expression to its parent
     * if there are no WHEN clauses left.
     */
    @Test
    void testVisitCaseExpressionRemoveWhenClause() {

        nulls.add(COLUMN_A);

        CaseExpression caseExpression = new CaseExpression();

        WhenClause whenClause = new WhenClause();
        whenClause.setWhenExpression(new Column(COLUMN_A));
        whenClause.setThenExpression(new NullValue());

        List<WhenClause> whenClauses = new ArrayList<>(1);
        whenClauses.add(whenClause);

        caseExpression.setWhenClauses(whenClauses);

        nullReducer.visit(caseExpression);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isNull();
    }

    /**
     * Tests whether {@link NullReducer#visit(CaseExpression)} signals the removal of a CASE expression to its parent
     * if its switch is invalidated.
     */
    @Test
    void testVisitCaseExpressionRemoveSwitch() {

        nulls.add(COLUMN_A);

        CaseExpression caseExpression = new CaseExpression();

        WhenClause whenClause = mock(WhenClause.class);
        List<WhenClause> whenClauses = new ArrayList<>(1);
        whenClauses.add(whenClause);
        caseExpression.setWhenClauses(whenClauses);

        caseExpression.setSwitchExpression(new Column(COLUMN_A));

        nullReducer.visit(caseExpression);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isNull();
    }

    /**
     * Tests whether {@link NullReducer#visit(CaseExpression)} updates the ELSE of a CASE expression if the
     * after visiting it the update signal is set.
     */
    @Test
    void testVisitCaseExpressionRemoveElse() {

        nulls.add(COLUMN_A);

        CaseExpression caseExpression = new CaseExpression();

        WhenClause whenClause = mock(WhenClause.class);
        List<WhenClause> whenClauses = new ArrayList<>(1);
        whenClauses.add(whenClause);
        caseExpression.setWhenClauses(whenClauses);

        caseExpression.setElseExpression(new Column(COLUMN_A));

        nullReducer.visit(caseExpression);

        assertThat(caseExpression.getElseExpression()).isNull();

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@link NullReducer#visit(WhenClause)} leaves a WHEN case of a CASE expression intact if it is
     * not modified.
     */
    @Test
    void testVisitWhenClause() {

        WhenClause whenClause = new WhenClause();

        Column whenExpression = new Column(COLUMN_A);
        whenClause.setWhenExpression(whenExpression);

        NullValue thenExpression = new NullValue();
        whenClause.setThenExpression(thenExpression);

        nullReducer.visit(whenClause);

        assertThat(whenClause.getWhenExpression()).isSameAs(whenExpression);
        assertThat(whenClause.getThenExpression()).isSameAs(thenExpression);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@link NullReducer#visit(WhenClause)} replaces the WHEN clause of a case from a CASE expression
     * if its subexpression signals that it must be updated.
     */
    @Test
    void testVisitWhenClauseUpdateWhen() {

        nulls.add(COLUMN_A);

        WhenClause whenClause = new WhenClause();

        Column column = new Column(COLUMN_A);
        LongValue longValue = new LongValue(0);
        AndExpression andExpression = new AndExpression(column, longValue);

        whenClause.setWhenExpression(andExpression);
        whenClause.setThenExpression(new NullValue());

        nullReducer.visit(whenClause);

        assertThat(whenClause.getWhenExpression()).isSameAs(longValue);

        assertThat(nullReducer.isUpdateChild()).isFalse();
    }

    /**
     * Tests whether {@link NullReducer#visit(WhenClause)} signals the CASE expression to which the visited WHEN
     * case belongs that it should be removed if the WHEN clause becomes invalid.
     */
    @Test
    void testVisitWhenClauseRemoveWhen() {

        nulls.add(COLUMN_A);

        WhenClause whenClause = new WhenClause();

        whenClause.setWhenExpression(new Column(COLUMN_A));
        whenClause.setThenExpression(new NullValue());

        nullReducer.visit(whenClause);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isNull();
    }

    /**
     * Tests whether {@link NullReducer#visit(WhenClause)} signals the CASE expression to which the visited WHEN
     * case belongs that it should be removed if the THEN clause becomes invalid.
     */
    @Test
    void testVisitWhenClauseRemoveThen() {

        nulls.add(COLUMN_A);

        WhenClause whenClause = new WhenClause();

        whenClause.setWhenExpression(new NullValue());
        whenClause.setThenExpression(new Column(COLUMN_A));

        nullReducer.visit(whenClause);

        assertThat(nullReducer.isUpdateChild()).isTrue();
        assertThat(nullReducer.getChild()).isNull();
    }

}
