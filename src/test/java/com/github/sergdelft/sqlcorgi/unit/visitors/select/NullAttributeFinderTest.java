package com.github.sergdelft.sqlcorgi.unit.visitors.select;

import com.github.sergdelft.sqlcorgi.visitors.select.NullAttributeFinder;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link NullAttributeFinder}.
 */
class NullAttributeFinderTest {

    private static final String COLUMN_NAME = "a";

    private NullAttributeFinder nullAttributeFinder;

    /**
     * Creates a new instance of {@link NullAttributeFinder} for each test case.
     */
    @BeforeEach
    void setUp() {
        nullAttributeFinder = new NullAttributeFinder();
    }

    /**
     * Verifies that visiting an {@code IS NULL} condition adds the corresponding attribute to the result set.
     */
    @Test
    void testVisitIsNull() {
        IsNullExpression isNull = new IsNullExpression();
        isNull.setLeftExpression(new Column(COLUMN_NAME));
        nullAttributeFinder.visit(isNull);

        assertThat(nullAttributeFinder.getColumns()).containsOnly(COLUMN_NAME);
    }

    /**
     * Verifies that visiting an {@code IS NOT NULL} condition does not add its attribute to the result set.
     */
    @Test
    void testVisitIsNotNull() {
        IsNullExpression isNotNull = new IsNullExpression();
        isNotNull.setLeftExpression(new Column(COLUMN_NAME));
        isNotNull.setNot(true);
        nullAttributeFinder.visit(isNotNull);

        assertThat(nullAttributeFinder.getColumns()).isEmpty();
    }

    /**
     * Tests whether {@link NullAttributeFinder#getColumns()} is initially empty.
     */
    @Test
    void testGetColumnsEmpty() {
        Set<String> columns = nullAttributeFinder.getColumns();
        assertThat(columns).isEmpty();
    }
}
