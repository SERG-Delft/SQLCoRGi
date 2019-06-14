package nl.tudelft.st01.unit.sqlfpcws.json;

import es.uniovi.lsi.in2test.sqlfpcws.SQLFpcWSSoapProxy;

import nl.tudelft.st01.sqlfpcws.SQLFpcWS;
import nl.tudelft.st01.sqlfpcws.json.BulkRuleGenerator;


import nl.tudelft.st01.util.exceptions.CannotParseInputSQLFileException;
import nl.tudelft.st01.util.exceptions.CannotWriteJSONOutputException;
import nl.tudelft.st01.util.exceptions.InvalidSchemaException;

import org.junit.After;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Class that tests {@link BulkRuleGenerator}.
 */
// Suppresses the Checkstyle MultipleStringLiterals warning as these duplicate strings are necessary for readability.
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@PrepareForTest(SQLFpcWS.class)
@RunWith(PowerMockRunner.class)
public class BulkRuleGeneratorTest {

    private static final String RESOURCE_PATH = "./src/test/resources/";
    private static final String SQL_INPUT_PATH = RESOURCE_PATH + "input.sql";
    private static final String XML_SCHEMA_PATH = RESOURCE_PATH + "schema.xml";
    private static final String JSON_OUTPUT_PATH = RESOURCE_PATH + "output.json";

    public static final int NUMBER_OF_QUERIES = 2;
    public static final int NUMBER_OF_TABLES = 2;
    public static final int NUMBER_OF_COLUMNS = 3;

    public static final int TIME_REQUIRED_MS = 2000;

    /**
     * Asserts that the empty line in the input file is ignored and so exactly 2 queries are read in.
     */
    @Test
    public void testEmptyLineInInputFileIsIgnored() {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);

        assertThat(bulkRuleGenerator.getNumberOfQueries()).isEqualTo(NUMBER_OF_QUERIES);
    }

    /**
     * Asserts that the XML schema is parsed correctly and so exactly 2 tables should been known.
     */
    @Test
    public void testGetNumberOfTablesInSchema() {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);

        assertThat(bulkRuleGenerator.getNumberOfTablesInSchema()).isEqualTo(NUMBER_OF_TABLES);
    }

    /**
     * Asserts that the XML schema is parsed correctly and so exactly 3 columns should been known.
     */
    @Test
    public void testGetNumberOfColumnsInSchema() {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);

        assertThat(bulkRuleGenerator.getNumberOfColumnsInSchema()).isEqualTo(NUMBER_OF_COLUMNS);
    }

    /**
     * Asserts that estimated time to generate targets for the submitted queries is correct.
     */
    @Test
    public void testGetEstimatedGenerationDurationInMilliSeconds() {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);

        assertThat(bulkRuleGenerator.getEstimatedGenerationDurationInMilliSeconds()).isEqualTo(TIME_REQUIRED_MS);
    }

    /**
     * Asserts that the correct exception is thrown when non-existing file is supplied to the {@link BulkRuleGenerator}
     * constructor.
     */
    @Test
    public void testCorrectExceptionIsThrownForNonExistingInputFile() {
        String wrongInputPath = RESOURCE_PATH + "nonExistingFile.sql";

        assertThatExceptionOfType(CannotParseInputSQLFileException.class).isThrownBy(
            () -> new BulkRuleGenerator(wrongInputPath, XML_SCHEMA_PATH, JSON_OUTPUT_PATH)
        );
    }

    /**
     * Asserts that the correct exception is thrown when the input file contains invalid SQL queries.
     */
    @Test
    public void testCorrectExceptionIsThrownForInvalidSQLQuery() {
        String fileWithInvalidQuery = RESOURCE_PATH + "invalidQuery.sql";

        assertThatExceptionOfType(CannotParseInputSQLFileException.class).isThrownBy(
            () -> new BulkRuleGenerator(fileWithInvalidQuery, XML_SCHEMA_PATH, JSON_OUTPUT_PATH)
        );
    }

    /**
     * Asserts that the correct exception is thrown when the supplied xml schema is not syntactically valid.
     */
    @Test
    public void testCorrectExceptionIsThrownForInvalidXMLSchema() {
        String fileWithInvalidSchema = RESOURCE_PATH + "invalidSchema.xml";

        assertThatExceptionOfType(InvalidSchemaException.class).isThrownBy(
            () -> new BulkRuleGenerator(SQL_INPUT_PATH, fileWithInvalidSchema, JSON_OUTPUT_PATH)
        );
    }

    /**
     * Asserts that the correct exception is thrown when an output file in a non-existent path is supplied to the
     * {@link BulkRuleGenerator} constructor. The XML response from the SQLFpc web service is mocked in order for the
     * test to work independently of a working internet connection.
     *
     * @throws Exception because of the use of {@link PowerMockito}.
     */
    @org.junit.Test
    public void testCorrectExceptionIsThrownForInvalidJSONOutputPath() throws Exception {
        String invalidJSONPath = RESOURCE_PATH + "nonExistingFolder/outputFile.json";

        String serverXMLReply =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<sqlfpc>\n"
                        + "   <version>1.3.180.91</version>\n"
                        + "   <sql>SELECT * FROM tableB</sql>\n"
                        + "   <fpcrules />\n"
                        + "</sqlfpc>";

        SQLFpcWSSoapProxy mockWebService = mock(SQLFpcWSSoapProxy.class);
        when(mockWebService.getRules(any(), any(), any())).thenReturn(serverXMLReply);
        PowerMockito.whenNew(SQLFpcWSSoapProxy.class).withAnyArguments().thenReturn(mockWebService);

        assertThatExceptionOfType(CannotWriteJSONOutputException.class).isThrownBy(
            () -> new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, invalidJSONPath)
                    .generate()
        );
    }

    /**
     * Tests the {@code Generate} method. Verifies that queries and schema are read in correctly, the web service is
     * called and a JSON file with the correct contents is saved to disk.
     *
     * The XML response from the SQLFpc web service and parsing of the response is mocked in order for the test to work
     * independently of a working internet connection.
     *
     * @throws InterruptedException if {@code Thread.sleep()} is interrupted.
     */
    @org.junit.Test
    public void testGenerateOutputJSONShouldBeCorrect() throws InterruptedException {
        BulkRuleGenerator bulkRuleGenerator = new BulkRuleGenerator(SQL_INPUT_PATH, XML_SCHEMA_PATH, JSON_OUTPUT_PATH);

        mockStatic(SQLFpcWS.class);

        List<String> tableBTargets = new ArrayList<>();
        tableBTargets.add("SELECT * FROM TableB WHERE (TableB.Var = 2)");
        tableBTargets.add("SELECT * FROM TableB WHERE (TableB.Var = 1)");
        tableBTargets.add("SELECT * FROM TableB WHERE (TableB.Var = 0)");
        tableBTargets.add("SELECT * FROM TableB WHERE (TableB.Var IS NULL)");

        when(SQLFpcWS.getCoverageTargets(any(), any(), any())).thenReturn(new ArrayList<>()).thenReturn(tableBTargets);

        String expectedJSONContent =
                          "{\n"
                        + "  \"entries\": [\n"
                        + "    {\n"
                        + "      \"queryNo\": 1,\n"
                        + "      \"pathList\": []\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"queryNo\": 2,\n"
                        + "      \"pathList\": [\n"
                        + "        \"SELECT * FROM TableB WHERE (TableB.Var = 2)\",\n"
                        + "        \"SELECT * FROM TableB WHERE (TableB.Var = 1)\",\n"
                        + "        \"SELECT * FROM TableB WHERE (TableB.Var = 0)\",\n"
                        + "        \"SELECT * FROM TableB WHERE (TableB.Var IS NULL)\"\n"
                        + "      ]\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}";

        bulkRuleGenerator.generate();

        File jsonFile = new File(JSON_OUTPUT_PATH);

        assertThat(jsonFile)
                .isFile()
                .hasContent(expectedJSONContent);
    }

    /**
     * Cleans up the JSON output file after the tests have completed.
     *
     * @throws IOException if access to the file is denied.
     */
    @After
    public void removeOutputFile() throws IOException {
        Files.deleteIfExists(Paths.get(JSON_OUTPUT_PATH));
    }
}
