package nl.tudelft.st01.queries;


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
 * Checks output of our tool agains predefined output from another tool.
 */
public final class CompareOutput {
    private static final String DOUBLE_NEWLINE = "\n\n";

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
        String basePath = "C:/Users/Timon Bestebreur/Dropbox/Opleiding/Technische Informatica/Y2/"
            + "Context Project/coverage_generator/src/test/java/nl/tudelft/st01/queries/query_resources/";

        String suitecrmInput = "suitecrm_input.txt";
        String suitecrmOutput = "suitecrm_output.json";
        String espocrmInput = "espocrm_input.txt";
        String espocrmOutput = "espocrm_output.json";
        String erpnextInput = "erpnext_input.txt";
        String erpnextOutput = "erpnext_output.json";

        String[] input = {basePath + erpnextInput, basePath + suitecrmInput, basePath + espocrmInput};
        String[] output = {basePath + erpnextOutput, basePath + suitecrmOutput, basePath + espocrmOutput};

        Scanner sc = null;
        JSONParser parser = new JSONParser();
        Object object = null;
        int wrongCounter = 0;
        int totalCounter = 0;

        for (int i = 0; i <= 2; i++) {
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
                totalCounter++;
                JSONArray queries = (JSONArray) iterator.next().get("pathList");
                String nextQuery = sc.nextLine();
                Set<String> ourResults = null;
                try {
                    ourResults = Generator.generateRules(nextQuery);
                } catch (Exception e) {
                    System.out.println(nextQuery);
                    System.out.println("The query on the previous line caused the following exception: "
                            + e.getMessage() + DOUBLE_NEWLINE);
                    continue;
                }
                int ourResultSize = ourResults.size();
                int expectedResultSize = queries.size();

                if (ourResultSize != expectedResultSize) {
                    wrongCounter++;
                    System.out.println(wrongCounter + ") The following query is not yet handled correctly: ");
                    System.out.println(nextQuery);
                    System.out.printf("Expected %d rules, got %d%n", expectedResultSize, ourResultSize);
                    System.out.println("These queries were expected:");
                    for (Object o : queries) {
                        System.out.println(o.toString());
                    }
                    System.out.println(DOUBLE_NEWLINE);
                }
            }

            System.out.println("FINISHED!! we handled " + (totalCounter - wrongCounter)
                    + " queries out of " + totalCounter + " correctly!");
        }




    }
}
