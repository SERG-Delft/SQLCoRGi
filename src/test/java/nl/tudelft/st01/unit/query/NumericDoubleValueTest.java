package nl.tudelft.st01.unit.query;

import nl.tudelft.st01.query.NumericDoubleValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests the functions of the {@link NumericDoubleValue} class.
 */
public class NumericDoubleValueTest {
    private static final String TEST_NUMBER = "123";

    private static final int TEST_NUMBER_AS_INT = 123;

    /**
     * Test for NumericDoubleValue.
     */
    @Test
    public void constructorNotNull() {
        NumericDoubleValue n = new NumericDoubleValue(TEST_NUMBER);

        assertThat(n).isNotNull();
    }

    /**
     * Test for NumericDoubleValue.
     */
    @Test
    public void getTest() {
        NumericDoubleValue n = new NumericDoubleValue(TEST_NUMBER);

        assertThat(n.getValue()).isEqualTo(TEST_NUMBER_AS_INT);
    }

    /**
     * Test for NumericDoubleValue.
     *
     * @param input Number to use in calculation
     */
    @ParameterizedTest(name = "[{index}] number to add: {0}")
    @CsvSource({"1", "-1", "0", "10"})
    public void addTest(int input) {
        NumericDoubleValue oldValue = new NumericDoubleValue(TEST_NUMBER);
        NumericDoubleValue newValue = (NumericDoubleValue) oldValue.add(input);

        assertThat(newValue.getValue()).isEqualTo(TEST_NUMBER_AS_INT + input);
    }
}
