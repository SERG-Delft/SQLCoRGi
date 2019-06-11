package nl.tudelft.st01.unit.visitors.select;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.schema.Column;
import nl.tudelft.st01.visitors.select.NullReducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains tests for {@link NullReducer}.
 */
// Justification: For some methods it's necessary to check the status of multiple subexpressions, and that of the
// NullReducer itself.
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class NullReducerTest {

    private static final String COLUMN_A = "a";

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
     * Tests whether {@code NullReducer#visitBinaryExpression(BinaryExpression)} leaves binary expressions unchanged
     * if it does not contain attributes that need to be removed.
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

}
