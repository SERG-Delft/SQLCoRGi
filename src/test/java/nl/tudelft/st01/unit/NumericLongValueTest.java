package nl.tudelft.st01.unit;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LongValue;
import nl.tudelft.st01.query.NumericLongValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sample nl.tudelft.st01.unit test class.
 */
public class NumericLongValueTest {

    /**
     * Test for NumericLongValue.
     */
    @Test
    public void correctSuperclass() {
        NumericLongValue n = new NumericLongValue("123");

        assertThat(n instanceof LongValue).isTrue();
    }

    /**
     * Test for NumericLongValue.
     */
    @Test
    public void getTest() {
        NumericLongValue n = new NumericLongValue("123");

        assertThat(n.getValue()).isEqualTo(123);
    }

    /**
     * Test for NumericLongValue.
     */
    @ParameterizedTest(name = "[{index}] number to add: {0}")
    @CsvSource({"1", "-1", "0", "10"})
    public void addTest(int input) {
        NumericLongValue oldValue = new NumericLongValue("123");
        NumericLongValue newValue = (NumericLongValue) oldValue.add(input);

        assertThat(newValue.getValue()).isEqualTo(123 + input);
    }
}
