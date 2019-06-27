package nl.tudelft.st01.unit.query;

import net.sf.jsqlparser.expression.DoubleValue;
import nl.tudelft.st01.query.NumericDoubleValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests the {@link NumericDoubleValue} class.
 */
class NumericDoubleValueTest {

    private static final String TEST_NUMBER_STR = "2.0";
    private static final double TEST_NUMBER = 2.0;

    /**
     * Test the constructor {@link NumericDoubleValue#NumericDoubleValue(String)} of NumericDoubleValue.
     */
    @Test
    void testConstructor() {
        NumericDoubleValue n = new NumericDoubleValue(TEST_NUMBER_STR);

        assertThat(n).isNotNull().isInstanceOf(DoubleValue.class);
        assertThat(n.getValue()).isEqualTo(TEST_NUMBER);
    }

    /**
     * Tests whether {@link NumericDoubleValue#add(int)} returns a new instance representing the correct value.
     *
     * @param input Number to use in calculation
     */
    @ParameterizedTest(name = "[{index}] number to add: {0}")
    @CsvSource({"1", "-1", "0", "10"})
    void testAdd(int input) {
        NumericDoubleValue oldValue = new NumericDoubleValue(TEST_NUMBER_STR);
        NumericDoubleValue newValue = (NumericDoubleValue) oldValue.add(input);

        assertThat(newValue).isNotSameAs(oldValue);
        assertThat(newValue.getValue()).isEqualTo(TEST_NUMBER + input);
    }
}
