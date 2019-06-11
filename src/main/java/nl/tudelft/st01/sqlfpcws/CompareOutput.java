package nl.tudelft.st01.sqlfpcws;

import com.google.gson.Gson;

import nl.tudelft.st01.Generator;
import nl.tudelft.st01.sqlfpcws.json.JSONEntries;
import nl.tudelft.st01.sqlfpcws.json.SQLRules;

import java.io.BufferedReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.List;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Checks output of our tool against predefined output from another tool.
 */
public final class CompareOutput {

    private static final String RESOURCE_BASE_PATH = ".\\src\\main\\resources\\database";

    private static final String SUITECRM_INPUT_PATH = RESOURCE_BASE_PATH + "\\input_queries\\suitecrm.sql";
    private static final String ESPOCRM_INPUT_PATH = RESOURCE_BASE_PATH + "\\input_queries\\espocrm.sql";
    private static final String ERPNEXT_INPUT_PATH = RESOURCE_BASE_PATH + "\\input_queries\\erpnext.sql";

    private static final String SUITECRM_OUTPUT_PATH = RESOURCE_BASE_PATH + "\\output_json\\suitecrm.json";
    private static final String ESPOCRM_OUTPUT_PATH = RESOURCE_BASE_PATH + "\\output_json\\espocrm.json";
    private static final String ERPNEXT_OUTPUT_PATH = RESOURCE_BASE_PATH + "\\output_json\\erpnext.json";

    private static final String DOUBLE_NEWLINE = "\n\n";
    private static final int AMOUNT_OF_QUERY_SETS = 3;

    /**
     * No instance of this class should be created.
     */
    private CompareOutput() {
        throw new UnsupportedOperationException();
    }

    /**
     * Runs the comparison between the JSON file and our output.
     *
     * @param args - command line arguments - are ignored in the code.
     */
    // Suppress IllegalCatch is there to make sure we can catch all exceptions and still continue comparing the rest of
    // the queries.
    @SuppressWarnings("checkstyle:IllegalCatch")
    public static void main(String[] args) {
        String[] input = {ERPNEXT_INPUT_PATH, SUITECRM_INPUT_PATH, ESPOCRM_INPUT_PATH};
        String[] output = {ERPNEXT_OUTPUT_PATH, SUITECRM_OUTPUT_PATH, ESPOCRM_OUTPUT_PATH};

        Gson gson = new Gson();

        int wrongCounter = 0;
        int totalCounter = 0;

        for (int i = 0; i < AMOUNT_OF_QUERY_SETS; i++) {
            List<String> queries;
            try (Stream<String> stream = Files.lines(Paths.get(input[i]))) {
                queries = stream
                        .filter(query -> !query.isEmpty())
                        .collect(Collectors.toList());
            } catch (IOException e) {
                System.err.println("SQL queries could not be read in correctly: " + e.getMessage());
                queries = new ArrayList<>();
            }

            JSONEntries jsonEntries;
            List<SQLRules> entries;
            try (BufferedReader jsonReader = Files.newBufferedReader(Paths.get(output[i]), StandardCharsets.UTF_8)) {
                jsonEntries = gson.fromJson(jsonReader, JSONEntries.class);
                entries = jsonEntries.getEntries();
            } catch (IOException e) {
                System.err.println("JSON file could not be read in correctly: " + e.getMessage());
                entries = new ArrayList<>();
            }

            int jobSize = Math.min(queries.size(), entries.size());
            for (int query = 0; query < jobSize; query++) {
                String currentQuery = queries.get(query);
                List<String> expectedTargets = entries.get(query).getPathList();
                totalCounter++;

                Set<String> ourTargets;
                try {
                    ourTargets = Generator.generateRules(currentQuery);
                } catch (Exception e) {
                    System.out.println(currentQuery);
                    System.out.println("The query on the previous line caused the following exception: "
                            + e.getMessage() + DOUBLE_NEWLINE);
                    continue;
                }

                int ourTargetSize = ourTargets.size();
                int expectedTargetSize = expectedTargets.size();

                if (ourTargetSize != expectedTargetSize) {
                    System.out.println(wrongCounter + ") The following query is not yet handled correctly: ");
                    System.out.println(currentQuery);
                    System.out.printf("Expected %d rules, got %d%n", expectedTargetSize, ourTargetSize);
                    System.out.println("These queries were expected:");
                    for (String expectedQuery : expectedTargets) {
                        System.out.println(expectedQuery);
                    }
                    System.out.println(DOUBLE_NEWLINE);
                    wrongCounter++;
                }
            }
        }

        System.out.println("Comparison complete! " + (totalCounter - wrongCounter) + " queries out of " + totalCounter
                + " generated the same number of targets as SQLFpc!");
    }
}
