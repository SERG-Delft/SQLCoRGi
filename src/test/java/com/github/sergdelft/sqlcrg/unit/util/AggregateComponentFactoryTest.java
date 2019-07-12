package com.github.sergdelft.sqlcrg.unit.util;

import com.github.sergdelft.sqlcrg.util.AggregateComponentFactory;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This class tests the functions of the {@link AggregateComponentFactory} class.
 */
class AggregateComponentFactoryTest {

    /**
     * Trying to invoke the {@link AggregateComponentFactory} constructor should throw an
     * {@link UnsupportedOperationException}.
     *
     * Java Reflection is used because the {@link AggregateComponentFactory} constructor is private.
     *
     * @throws NoSuchMethodException if the {@link AggregateComponentFactory} constructor is not found.
     */
    @Test
    void testConstructorThrowsException() throws NoSuchMethodException {
        Constructor<AggregateComponentFactory> utilityGettersConstructor
                = AggregateComponentFactory.class.getDeclaredConstructor();
        utilityGettersConstructor.setAccessible(true);

        assertThatThrownBy(utilityGettersConstructor::newInstance)
            .hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Test case to check whether {@link AggregateComponentFactory#createCountAllColumns()} function returns COUNT(*).
     */
    @Test
    void testCreateCountAllColumns() {
        assertThat(AggregateComponentFactory.createCountAllColumns().toString()).isEqualTo("COUNT(*)");
    }

    /**
     * Test case to check whether {@link AggregateComponentFactory#createGreaterThanOne(Expression)} returns _ > 1.
     */
    @Test
    void testCreateGreaterThanOne() {
        Function allColumns = AggregateComponentFactory.createCountAllColumns();

        assertThat(AggregateComponentFactory.createGreaterThanOne(allColumns).toString()).isEqualTo("COUNT(*) > 1");
    }

    /**
     * Test case to check whether {@link AggregateComponentFactory#createCountColumn(Expression, boolean)} function
     * returns COUNT(column).
     */
    @Test
    void testCreateCountColumn() {
        assertThat(
            AggregateComponentFactory.createCountColumn(new Column("director"), false).toString()
        ).isEqualTo("COUNT(director)");
    }

    /**
     * Test case to check whether {@link AggregateComponentFactory#createCountColumn(Expression, boolean)} function
     * returns COUNT(DISTINCT column).
     */
    @Test
    void testCreateCountDistinctColumn() {
        assertThat(
            AggregateComponentFactory.createCountColumn(new Column("title"), true).toString()
        ).isEqualTo("COUNT(DISTINCT title)");
    }
}
