package nl.tudelft.st01.unit.query;

import net.sf.jsqlparser.expression.LongValue;
import nl.tudelft.st01.query.NumericLongValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests the {@link NumericLongValue} class.
 */
class NumericLongValueTest {

    private static final String TEST_NUMBER_STR = "1";
    private static final int TEST_NUMBER = 1;

    /**
     * Test the constructor {@link NumericLongValue#NumericLongValue(String)} of NumericLongValue.
     */
    @Test
    void testConstructor() {
        NumericLongValue n = new NumericLongValue(TEST_NUMBER_STR);

        assertThat(n).isNotNull().isInstanceOf(LongValue.class);
        assertThat(n.getValue()).isEqualTo(TEST_NUMBER);
    }

    /**
     * Tests whether {@link NumericLongValue#add(int)} returns a new instance representing the correct value.
     *
     * @param input Number to use in calculation
     */
    @ParameterizedTest(name = "[{index}] number to add: {0}")
    @CsvSource({"1", "-1", "0", "10"})
    void testAdd(int input) {
        NumericLongValue oldValue = new NumericLongValue(TEST_NUMBER_STR);
        NumericLongValue newValue = (NumericLongValue) oldValue.add(input);

        assertThat(newValue).isNotSameAs(oldValue);
        assertThat(newValue.getValue()).isEqualTo(TEST_NUMBER + input);
    }
}
