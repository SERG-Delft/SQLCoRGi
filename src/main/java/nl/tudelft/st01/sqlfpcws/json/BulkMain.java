package nl.tudelft.st01.sqlfpcws.json;

public final class BulkMain {

    private static final String DATABASE = "espocrm";

    private static final String SQL_INPUT_PATH = "/database/input_queries/" + DATABASE + ".sql";
    private static final String XML_SCHEMA_PATH = "/database/schema_xml/" + DATABASE + ".xml";
    private static final String JSON_OUTPUT_PATH = ".\\src\\main\\resources\\database\\output_json\\" + DATABASE + ".json";

    public static void main(String[] args) throws InterruptedException {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);

        System.out.println("Generating bulk coverage targets for file: " + DATABASE);
        System.out.println(bulkRuleGenerator.statisticsString());

        bulkRuleGenerator.generate();

        System.out.println("Bulk coverage target generation complete!");
    }
}
