package nl.tudelft.st01.sqlfpcws.json;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class BulkMain {

    private static final String DATABASE = "suitecrm";

    private static final String SQL_INPUT_PATH = "/database/input_queries/" + DATABASE + ".sql";
    private static final String XML_SCHEMA_PATH = "/database/schema_xml/" + DATABASE + ".xml";
    private static final String JSON_OUTPUT_PATH = "/database/output_json/" + DATABASE + ".json";

    private static final int RULE_GENERATOR_INTERVAL = 1000;

    public static void main(String[] args) throws Exception{
        SAXReader reader = new SAXReader();

        InputStreamReader isr = new InputStreamReader(BulkMain.class.getResourceAsStream(XML_SCHEMA_PATH), UTF_8);

        Document schema = reader.read(isr);

        System.out.println(schema.getStringValue());

    }

}
