package nl.tudelft.st01.unit;

import nl.tudelft.st01.Generator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

/**
 * Unit tests for the {@link Generator}.
 */
public class AggregateFunctionsGeneratorTest {


    /**
     *  Basic test.
     */
    @Test
    public void basicTest() {
        assertThat(true).isEqualTo(true);
    }
}
