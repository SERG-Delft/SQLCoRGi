package nl.tudelft.st01.sqlfpcws.json;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class contains the main that uses the SQLFpc web service to generate coverage targets for multiple sql queries.
 */
public final class BulkMain {

    private static final String DATABASE = "espocrm";

    private static final String RESOURCE_PATH = ".\\src\\main\\resources\\database";
    private static final String SQL_INPUT_PATH = RESOURCE_PATH + "\\input_queries\\" + DATABASE + ".sql";
    private static final String XML_SCHEMA_PATH = RESOURCE_PATH + "\\schema_xml\\" + DATABASE + ".xml";
    private static final String JSON_OUTPUT_PATH = RESOURCE_PATH + "\\output_json\\" + DATABASE + ".json";

    /**
     * No instance of this class should be created.
     */
    protected BulkMain() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Main method that demonstrates the use of the {@link BulkRuleGenerator}.
     *
     * @param args unused.
     */
    public static void main(String[] args) throws InterruptedException {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);

        long durationNanoSeconds = 1000000L * bulkRuleGenerator.getEstimatedGenerationDurationInMilliSeconds();
        LocalDateTime finishTime = LocalDateTime.now().plusNanos(durationNanoSeconds);
        String formattedFinishTime = finishTime.format(DateTimeFormatter.ISO_DATE_TIME);

        System.out.println(
                    "Generating bulk coverage targets for file: " + DATABASE
                        + "- Number of queries provided: " + bulkRuleGenerator.getNumberOfQueries() + "\n"
                        + "- Number of tables in schema: " + bulkRuleGenerator.getNumberOfTablesInSchema() + "\n"
                        + "- Number of columns in schema: " + bulkRuleGenerator.getAmountOfColumnsInSchema() + "\n"
                        + "- Estimated completion time: " + formattedFinishTime + "\n"
        );

        bulkRuleGenerator.generate();

        System.out.println("Bulk coverage target generation complete!");
    }
}
