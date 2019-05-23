package nl.tudelft.st01.FunctionalTests;

import nl.tudelft.st01.Generator;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class FunctionTest {

    protected List<DefaultTest> tests;

    protected abstract void setUpDefaultTests();

    @TestFactory
    Stream<DynamicTest> executeDefaultTests() {
        setUpDefaultTests();

        ThrowingConsumer<DefaultTest> testExecutor = (test) -> {
            Set<String> result = Generator.generateRules(test.getQuery());
            assertEquals(test.getExpected(), result);
        };

        return DynamicTest.stream(tests.iterator(), e -> e.getDisplayName(), testExecutor);
    }

    @Test
    public void standardTest() {
        assertEquals(true, true);
    }

}
