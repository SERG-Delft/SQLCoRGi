package nl.tudelft.st01.unit;

import net.sf.jsqlparser.expression.DoubleValue;
import nl.tudelft.st01.query.NumericDoubleValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sample nl.tudelft.st01.unit test class.
 */
public class NumericValueTest {

    /**
     * Test for NumericDoubleValue.
     */
    @Test
    public void correctSuperclass() {
        NumericDoubleValue n = new NumericDoubleValue("123");

        assertThat(n instanceof DoubleValue).isTrue();
    }

    /**
     * Test for NumericDoubleValue.
     */
    @Test
    public void getTest() {
        NumericDoubleValue n = new NumericDoubleValue("123");

        assertThat(n.getValue()).isEqualTo(123);
    }

    /**
     * Test for NumericDoubleValue.
     */
    @ParameterizedTest(name = "[{index}] number to add: {0}")
    @CsvSource({"1", "-1", "0", "10"})
    public void addTest(int input) {
        NumericDoubleValue oldValue = new NumericDoubleValue("123");
        NumericDoubleValue newValue = (NumericDoubleValue) oldValue.add(input);

        assertThat(newValue.getValue()).isEqualTo(123 + input);
    }
}
