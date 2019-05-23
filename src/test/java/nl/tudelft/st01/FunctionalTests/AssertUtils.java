package nl.tudelft.st01.FunctionalTests;

import nl.tudelft.st01.Generator;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

final class AssertUtils {

    static void assertEquals(String query, String... expected) {
        Set<String> result = Generator.generateRules(query);

        Assertions.assertEquals(new TreeSet<>(Arrays.asList(expected)), result);
    }
}
