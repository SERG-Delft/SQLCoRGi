package nl.tudelft.st01.unit;

import nl.tudelft.st01.query.NumericLongValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sample nl.tudelft.st01.unit test class.
 */
public class NumericLongValueTest {
    private static final String TEST_NUMBER = "123";

    private static final int TEST_NUMBER_AS_INT = 123;

    /**
     * Test for NumericLongValue.
     */
    @Test
    public void correctSuperclass() {
        NumericLongValue n = new NumericLongValue(TEST_NUMBER);

        assertThat(n).isNotNull();
    }

    /**
     * Test for NumericLongValue.
     */
    @Test
    public void getTest() {
        NumericLongValue n = new NumericLongValue(TEST_NUMBER);

        assertThat(n.getValue()).isEqualTo(TEST_NUMBER_AS_INT);
    }

    /**
     * Test for NumericLongValue.
     *
     * @param input Number to use in calculation
     */
    @ParameterizedTest(name = "[{index}] number to add: {0}")
    @CsvSource({"1", "-1", "0", "10"})
    public void addTest(int input) {
        NumericLongValue oldValue = new NumericLongValue(TEST_NUMBER);
        NumericLongValue newValue = (NumericLongValue) oldValue.add(input);

        assertThat(newValue.getValue()).isEqualTo(TEST_NUMBER_AS_INT + input);
    }
}
