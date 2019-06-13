package nl.tudelft.st01;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *  Entry point for our tool. Use this to interact with the generator.
 */
public final class CoverageRulesGenerator {

    /**
     *  Constructor for the Entry point to our tool. This is not supposed to be instantiated.
     *  Instead, you just call the static method {@code CoverageRulesGenerator.generateRules()} and enjoy the results.
     */
    private CoverageRulesGenerator() {
        throw new UnsupportedOperationException("You are not allowed to instantiate this class.");
    }

    /**
    *  Generates coverage targets based on an input SQL query.
    *
    * @param query SQL query to generate coverage rules for.
    * @return List of MC/DC coverage rules as strings.
    */
    public static List<String> generateRules(String query) {
        Set<String> result = Generator.generateRules(query);
        List<String> resultAsList = new ArrayList<>();
        resultAsList.addAll(result);

        return resultAsList;
    }

}
