package nl.tudelft.st01.unit.sqlfpcws.json;

import nl.tudelft.st01.sqlfpcws.json.BulkRuleGenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assumptions.assumeThatCode;

public class BulkRuleGeneratorTest {

    private static final String RESOURCE_PATH = ".\\src\\test\\resources\\";
    private static final String SQL_INPUT_PATH = RESOURCE_PATH + "input.sql";
    private static final String XML_SCHEMA_PATH = RESOURCE_PATH + "schema.xml";
    private static final String JSON_OUTPUT_PATH = RESOURCE_PATH + "output.json";

    public static final int NUMBER_OF_QUERIES = 2;
    public static final int NUMBER_OF_TABLES = 2;
    public static final int NUMBER_OF_COLUMNS = 3;

    public static final String SQLFPCWS_ADDRESS = "in2test.lsi.uniovi.es";
    public static final int SQLFPCWS_PORT = 80;

    public void webServiceIsReachable() throws IOException {
        Socket socket = new Socket(SQLFPCWS_ADDRESS, SQLFPCWS_PORT);
        socket.close();
    }

    @Test
    public void testEmptyLineInInputFileIsIgnored() {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);


        assertThat(bulkRuleGenerator.getNumberOfQueries()).isEqualTo(NUMBER_OF_QUERIES);
    }

    @Test
    public void testGetNumberOfTablesInSchema() {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);

        assertThat(bulkRuleGenerator.getNumberOfTablesInSchema()).isEqualTo(NUMBER_OF_TABLES);
    }

    @Test
    public void testGetNumberOfColumnsInSchema() {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);

        assertThat(bulkRuleGenerator.getNumberOfColumnsInSchema()).isEqualTo(NUMBER_OF_COLUMNS);
    }

    @Test
    public void testGenerateOutputJSONShouldBeCorrect() throws InterruptedException {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);

        String expectedJSONContent =
                "{\n" +
                "  \"entries\": [\n" +
                "    {\n" +
                "      \"queryNo\": 1,\n" +
                "      \"pathList\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"queryNo\": 2,\n" +
                "      \"pathList\": [\n" +
                "        \"SELECT * FROM TableB WHERE (TableB.Var = 2)\",\n" +
                "        \"SELECT * FROM TableB WHERE (TableB.Var = 1)\",\n" +
                "        \"SELECT * FROM TableB WHERE (TableB.Var = 0)\",\n" +
                "        \"SELECT * FROM TableB WHERE (TableB.Var IS NULL)\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assumeThatCode(this::webServiceIsReachable).doesNotThrowAnyException();

        assertThatCode(() -> bulkRuleGenerator.generate()).doesNotThrowAnyException();

        File jsonFile = new File(JSON_OUTPUT_PATH);

        assertThat(jsonFile)
                .isFile()
                .hasContent(expectedJSONContent);
    }

    @AfterEach
    public void removeOutputFile() throws IOException {
        Files.deleteIfExists(Paths.get(JSON_OUTPUT_PATH));
    }

}
