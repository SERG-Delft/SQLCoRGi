package nl.tudelft.st01.unit.util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import nl.tudelft.st01.util.UtilityGetters;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This class tests the functions of the {@link UtilityGetters} class.
 */
public class UtilityGettersTest {

    /**
     * Trying to invoke the {@link UtilityGetters} constructor should throw an {@link UnsupportedOperationException}.
     *
     * Java Reflection is used because the {@link UtilityGetters} constructor is private.
     *
     * @throws NoSuchMethodException if the {@link UtilityGetters} constructor is not found - this cannot happen.
     */
    @Test
    public void testConstructorThrowsException() throws NoSuchMethodException {
        Constructor<UtilityGetters> utilityGettersConstructor = UtilityGetters.class.getDeclaredConstructor();
        utilityGettersConstructor.setAccessible(true);

        assertThatThrownBy(
            () -> utilityGettersConstructor.newInstance()
        ).hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Test case to check whether {@link UtilityGetters#createCountAllColumns()} function returns COUNT(*).
     */
    @Test
    public void testCreateCountAllColumns() {
        assertThat(UtilityGetters.createCountAllColumns().toString()).isEqualTo("COUNT(*)");
    }

    /**
     * Test case to check whether {@link UtilityGetters#createGreaterThanOne(Expression)} function returns _ > 1.
     */
    @Test
    public void testCreateGreaterThanOne() {
        Function allColumns = UtilityGetters.createCountAllColumns();

        assertThat(UtilityGetters.createGreaterThanOne(allColumns).toString()).isEqualTo("COUNT(*) > 1");
    }

    /**
     * Test case to check whether {@link UtilityGetters#createCountColumn(Expression, boolean)} function
     * returns COUNT(column).
     */
    @Test
    public void testCreateCountColumn() {
        assertThat(
            UtilityGetters.createCountColumn(new Column("director"), false).toString()
        ).isEqualTo("COUNT(director)");
    }

    /**
     * Test case to check whether {@link UtilityGetters#createCountColumn(Expression, boolean)} function
     * returns COUNT(DISTINCT column).
     */
    @Test
    public void testCreateCountDistinctColumn() {
        assertThat(
            UtilityGetters.createCountColumn(new Column("title"), true).toString()
        ).isEqualTo("COUNT(DISTINCT title)");
    }
}
