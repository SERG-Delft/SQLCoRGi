package com.github.sergdelft.sqlcorgi;

import com.github.sergdelft.sqlcorgi.schema.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 *  Entry point for our tool. Use this to interact with the generator.
 */
public final class CoverageRuleGenerator {

    /**
     *  Constructor for the Entry point to our tool. This is not supposed to be instantiated.
     *  Instead, you just call the static method {@code CoverageRuleGenerator.generateRules()} and enjoy the results.
     */
    private CoverageRuleGenerator() {
        throw new UnsupportedOperationException("You are not allowed to instantiate this class.");
    }

    /**
    *  Generates coverage targets based on an input SQL query.
    *
    * @param query SQL query to generate coverage rules for.
    * @param schema The database schema.
    * @return List of MC/DC coverage rules as strings.
    */
    public static List<String> generateRules(String query, Schema schema) {
        return new ArrayList<>(Generator.generateRules(query));
    }

}
