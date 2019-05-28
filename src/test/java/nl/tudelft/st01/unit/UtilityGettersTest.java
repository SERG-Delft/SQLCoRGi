package nl.tudelft.st01.unit;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import nl.tudelft.st01.UtilityGetters;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests if the functions of the {@code UtilityGetters} class exercise correct behaviour
 * by means of unit tests.
 */
public class UtilityGettersTest {

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
