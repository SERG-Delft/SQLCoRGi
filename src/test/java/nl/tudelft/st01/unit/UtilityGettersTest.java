package nl.tudelft.st01.unit;

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
     * Test case to check whether {@code UtilityGetters getCountAllColumns} function returns COUNT(*).
     */
    @Test
    public void testGetCountAllColumns() {
        assertThat(UtilityGetters.getCountAllColumns().toString()).isEqualTo("COUNT(*)");
    }

    /**
     * Test case to check whether {@code UtilityGetters getGreaterThan1} function returns _ > 1.
     */
    @Test
    public void testGetGreaterThan1() {
        Function allColumns = UtilityGetters.getCountAllColumns();

        assertThat(UtilityGetters.getGreaterThan1(allColumns).toString()).isEqualTo("COUNT(*) > 1");
    }

    /**
     * Test case to check whether {@code UtilityGetters getCountColumn} function returns COUNT(column).
     */
    @Test
    public void testGetCountColumn() {
        assertThat(
            UtilityGetters.getCountColumn(new Column("director"), false).toString()
        ).isEqualTo("COUNT(director)");
    }

    /**
     * Test case to check whether {@code UtilityGetters getCountDistinctColumn} function
     * returns COUNT(DISTINCT column).
     */
    @Test
    public void testGetCountDistinctColumn() {
        assertThat(
            UtilityGetters.getCountColumn(new Column("title"), true).toString()
        ).isEqualTo("COUNT(DISTINCT title)");
    }
}
