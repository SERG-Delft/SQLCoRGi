package nl.tudelft.st01.unit;

import net.sf.jsqlparser.expression.DoubleValue;
import nl.tudelft.st01.query.NumericDoubleValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sample nl.tudelft.st01.unit test class.
 */
public class NumericValueTest {

    /**
     * Sample test with AssertJ.
     */
    @Test
    public void correctSuperclass() {
        NumericDoubleValue n = new NumericDoubleValue("123");

        assertThat(n instanceof DoubleValue).isTrue();
    }
}
