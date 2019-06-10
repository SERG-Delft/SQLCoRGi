package nl.tudelft.st01.sqlfpcws;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.tudelft.st01.Generator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Checks output of our tool against predefined output from another tool.
 */
public final class CompareOutput {

    private static final String RESOURCE_BASE_PATH = ".\\src\\main\\resources\\database";

    private static final String SUITECRM_INPUT_PATH = RESOURCE_BASE_PATH + "\\input_queries\\suitecrm.sql";
    private static final String ESPOCRM_INPUT_PATH = RESOURCE_BASE_PATH + "\\input_queries\\espocrm.sql";
    private static final String ERPNEXT_INPUT_PATH = RESOURCE_BASE_PATH + "\\input_queries\\erpnext.sql";

    private static final String SUITECRM_OUTPUT_PATH = RESOURCE_BASE_PATH + "\\output_json\\suitecrm.sql";
    private static final String ESPOCRM_OUTPUT_PATH = RESOURCE_BASE_PATH + "\\output_json\\espocrm.sql";
    private static final String ERPNEXT_OUTPUT_PATH = RESOURCE_BASE_PATH + "\\output_json\\erpnext.sql";

    private static final String DOUBLE_NEWLINE = "\n\n";
    private static final int AMOUNT_OF_QUERY_SETS = 3;

    /**
     * Constructor for the class.
     */
    private CompareOutput() {
        throw new UnsupportedOperationException();
    }

    /**
     * Runs the comparison between the JSON file and our output.
     *
     * @param args - command line arguments - are ignored in the code
     */
    // Suppress IllegalCatch is there to make sure we can catch all exceptions and still continue
    //      comparing the rest of the queries
    // Suppress AvoidInstantiatingObjectsInLoops is needed to make sure we're reading all the different files
    // Suppress AvoidFileStream is needed to make sure we're able to read files
    @SuppressWarnings({"checkstyle:illegalcatch", "PMD.AvoidInstantiatingObjectsInLoops", "PMD.AvoidFileStream"})
    @SuppressFBWarnings(
            value = "DM_DEFAULT_ENCODING",
            justification = "Encoding for FileReader can be specified, so this needs to be suppressed")
    public static void main(String[] args) {
        String[] input = {ERPNEXT_INPUT_PATH, SUITECRM_INPUT_PATH, ESPOCRM_INPUT_PATH};
        String[] output = {ERPNEXT_OUTPUT_PATH + SUITECRM_OUTPUT_PATH, ESPOCRM_OUTPUT_PATH};

        Scanner sc = null;
        JSONParser parser = new JSONParser();
        Object object = null;
        int wrongCounter = 0;
        int totalCounter = 0;

        for (int i = 0; i < AMOUNT_OF_QUERY_SETS; i++) {
            try {
                sc = new Scanner(new File(input[i]), "UTF-8");
                object = parser.parse(new FileReader(output[i]));

            } catch (Exception e) {
                e.printStackTrace();
            }

            JSONObject jsonObject = (JSONObject) object;
            JSONArray entries = (JSONArray) jsonObject.get("entries");
            Iterator<JSONObject> iterator = entries.iterator();

            while (iterator.hasNext() && sc.hasNextLine()) {
                JSONArray expectedQueries = (JSONArray) iterator.next().get("pathList");
                String nextQuery = sc.nextLine();
                Set<String> ourResults = null;
                totalCounter++;
                try {
                    ourResults = Generator.generateRules(nextQuery);
                } catch (Exception e) {
                    System.out.println(nextQuery);
                    System.out.println("The query on the previous line caused the following exception: "
                            + e.getMessage() + DOUBLE_NEWLINE);
                    continue;
                }
                int ourResultSize = ourResults.size();
                int expectedResultSize = expectedQueries.size();

                if (ourResultSize != expectedResultSize) {
                    System.out.println(wrongCounter + ") The following query is not yet handled correctly: ");
                    System.out.println(nextQuery);
                    System.out.printf("Expected %d rules, got %d%n", expectedResultSize, ourResultSize);
                    System.out.println("These queries were expected:");
                    for (Object expectedQuery : expectedQueries) {
                        System.out.println(expectedQuery.toString());
                    }
                    System.out.println(DOUBLE_NEWLINE);
                    wrongCounter++;
                }
            }

            System.out.println("FINISHED!! we handled " + (totalCounter - wrongCounter)
                    + " queries out of " + totalCounter + " correctly!");
        }

        if (sc != null) {
            sc.close();
        }
    }
}
