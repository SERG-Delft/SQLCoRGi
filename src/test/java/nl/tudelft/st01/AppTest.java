package nl.tudelft.st01;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous test.
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    /**
     * Test if Mockito can run.
     */
    @Test
    public void mockitoTest() {
        Object mock = Mockito.mock(Object.class);
        assertTrue(Mockito.mockingDetails(mock).isMock());
    }
}
