package nl.tudelft.st01.unit.visitors.select;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import nl.tudelft.st01.visitors.select.NullReducer;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains tests for {@link NullReducer}.
 */
class NullReducerTest {

    private static final String COLUMN_A = "a";

    /**
     * Tests whether the constructor correctly initializes a {@link NullReducer}.
     */
    @Test
    void testConstructor() {
        HashSet<String> nulls = new HashSet<>();
        nulls.add(COLUMN_A);

        NullReducer nullReducer = new NullReducer(nulls);

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

        NullReducer nullReducer = new NullReducer(new HashSet<>());

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

        HashSet<String> nulls = new HashSet<>();
        nulls.add(COLUMN_A);
        NullReducer nullReducer = new NullReducer(nulls);

        NullValue nullValue = new NullValue();
        AndExpression andExpression = new AndExpression(new Column(COLUMN_A), nullValue);

        nullReducer.visit(andExpression);

        assertThat(nullReducer.getRoot(andExpression)).isSameAs(nullValue);
    }

}
