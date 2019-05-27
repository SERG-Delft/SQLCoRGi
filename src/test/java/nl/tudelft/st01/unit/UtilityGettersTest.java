package nl.tudelft.st01.unit;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import nl.tudelft.st01.UtilityGetters;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sample nl.tudelft.st01.unit test class.
 */
public class UtilityGettersTest {

    /**
     * Unit test case for UtilityGetters getCountAllColumns function.
     */
    @Test
    public void testGetCountAllColumns() {
        assertThat(UtilityGetters.getCountAllColumns().toString()).isEqualTo("COUNT(*)");
    }

    /**
     * Unit test case for UtilityGetters getGreaterThan1 function.
     */
    @Test
    public void testGetGreaterThan1() {
        Function allColumns = UtilityGetters.getCountAllColumns();

        assertThat(UtilityGetters.getGreaterThan1(allColumns).toString()).isEqualTo("COUNT(*) > 1");
    }

    /**
     * Unit test case for UtilityGetters getCountColumn function.
     */
    @Test
    public void testGetCountColumn() {
        assertThat(
            UtilityGetters.getCountColumn(new Column("director"), false).toString()
        ).isEqualTo("COUNT(director)");
    }

    /**
     * Unit test case for UtilityGetters getCountColumn function with DISTINCT enabled.
     */
    @Test
    public void testGetCountDistinctColumn() {
        assertThat(
            UtilityGetters.getCountColumn(new Column("title"), true).toString()
        ).isEqualTo("COUNT(DISTINCT title)");
    }
}
