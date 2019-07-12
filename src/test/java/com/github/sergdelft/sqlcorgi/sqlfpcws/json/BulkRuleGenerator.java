package com.github.sergdelft.sqlcorgi.sqlfpcws.json;

import com.github.sergdelft.sqlcorgi.exceptions.CannotBeParsedException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;

import com.github.sergdelft.sqlcorgi.sqlfpcws.SQLFpcWS;
import com.github.sergdelft.sqlcorgi.sqlfpcws.CannotParseInputSQLFileException;
import com.github.sergdelft.sqlcorgi.sqlfpcws.CannotWriteJSONOutputException;
import com.github.sergdelft.sqlcorgi.sqlfpcws.InvalidSchemaException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class that generates the coverage targets for multiple sql queries by using the SQLFpc web service and stores them
 * in a single JSON file.
 */
public class BulkRuleGenerator {

    private List<String> queriesToParse;
    private Document schema;
    private String jsonOutputPath;

    private JSONEntries sqlJson;
    private int queryNo;

    private static final int RULE_GENERATOR_INTERVAL = 1000;

    private static final String SCHEMA_TABLE_XPATH = "/schema/table";
    private static final String SCHEMA_COLUMN_XPATH = "/schema/table/column";

    private static final String TABLE_ATTRIBUTE_NAME = "name";

    /**
     * Creates a new {@link BulkRuleGenerator} from the paths to an input file with sql queries (1 query per line),
     * the schema of the database and and an output file where the generated coverage targets will be stored.
     *
     * @param sqlInputPath   Path to a file that contains the SQL queries to be covered.
     * @param xmlSchemaPath  Path to a file with the schema of the database used in the queries.
     * @param jsonOutputPath Path to a file where the JSON output can be stored.
     */
    public BulkRuleGenerator(String sqlInputPath, String xmlSchemaPath, String jsonOutputPath) {
        setUpQueriesToParse(sqlInputPath);
        setUpSchemaDocument(xmlSchemaPath);
        this.jsonOutputPath = jsonOutputPath;

        this.queryNo = 0;
        this.sqlJson = new JSONEntries();
    }

    /**
     * Gets the number of queries for which coverage targets should be generated.
     *
     * @return the number of queries.
     */
    public int getNumberOfQueries() {
        return queriesToParse.size();
    }

    /**
     * Gets the number of tables in the provided schema.
     *
     * @return the number of tables.
     */
    public int getNumberOfTablesInSchema() {
        return schema.selectNodes(SCHEMA_TABLE_XPATH).size();
    }

    /**
     * Gets the total number of columns in the provided schema.
     *
     * @return the number of columns.
     */
    public int getNumberOfColumnsInSchema() {
        return schema.selectNodes(SCHEMA_COLUMN_XPATH).size();
    }

    /**
     * Gets the estimated amount of time required to generate the coverage targets for all queries.
     *
     * @return the amount of time required.
     */
    public long getEstimatedGenerationDurationInMilliSeconds() {
        return RULE_GENERATOR_INTERVAL * (long) getNumberOfQueries();
    }

    /**
     * Parses the queries from the provided input file into a list. Each query is parsed by JSQLParser to check if they
     * are syntactically correct.
     *
     * @param sqlInputPath Path to a file that contains the SQL queries to be covered.
     */
    private void setUpQueriesToParse(String sqlInputPath) {
        try (Stream<String> stream = Files.lines(Paths.get(sqlInputPath))) {
            queriesToParse = stream
                    .filter(query -> !query.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CannotParseInputSQLFileException("SQL queries could not be read in correctly: " + e.getMessage());
        }

        for (String query : queriesToParse) {
            try {
                CCJSqlParserUtil.parse(query);
            } catch (JSQLParserException e) {
                throw new CannotBeParsedException("Input query is not valid: " + query);
            }
        }
    }

    /**
     * Parses the schema from the provided input file into a {@link Document}.
     *
     * @param xmlSchemaPath Path to a file with the schema of the database used in the queries.
     */
    private void setUpSchemaDocument(String xmlSchemaPath) {
        try {
            schema = new SAXReader().read(new File(xmlSchemaPath));
        } catch (DocumentException e) {
            throw new InvalidSchemaException("Schema is not syntactically valid.");
        }
    }

    /**
     * Outputs the generated coverage targets for each query into an ordered JSON file.
     */
    private void outputToJsonFile() {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();

        try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(jsonOutputPath), StandardCharsets.UTF_8)) {
            gson.toJson(sqlJson, jsonWriter);
            jsonWriter.flush();
        } catch (IOException e) {
            throw new CannotWriteJSONOutputException("Could not save JSON output.");
        }
    }

    /**
     * Calls the SQLFpc web service to generate coverage targets for each query and saves the results as a JSON file.
     *
     * @throws InterruptedException if {@code Thread.sleep()} is interrupted.
     */
    public void generate() throws InterruptedException {
        for (String query : queriesToParse) {
            String schema = filterSchema(query).asXML();
            List<String> coverageTargets = SQLFpcWS.getCoverageTargets(query, schema, "");

            SQLRules sqlRules = new SQLRules(++queryNo, coverageTargets);
            sqlJson.addEntry(sqlRules);

            System.out.println("[" + queryNo + "]: " + query);
            System.out.println("Generated rules (" + coverageTargets.size() + "):");
            coverageTargets.forEach(target -> System.out.println("\t" + target));
            System.out.println();

            Thread.sleep(RULE_GENERATOR_INTERVAL);
        }

        outputToJsonFile();
    }

    /**
     * Filters the schema {@link Document} to only include the tables that are mentioned in the provided query. This is
     * necessary as the SQLFpc web service limits the size of the schema that it is willing to parse.
     *
     * @param query The query for which the schema should be filtered.
     * @return A filtered {@link Document}.
     */
    private Document filterSchema(String query) {
        List<String> tableNames = getInvolvedTableFromQuery(query);
        Document filteredSchema = (Document) schema.clone();

        tableNames.replaceAll(String::toLowerCase);

        List<Node> tables = filteredSchema.selectNodes(SCHEMA_TABLE_XPATH);
        for (Node table : tables) {
            String tableNameXML = ((Element) table).attribute(TABLE_ATTRIBUTE_NAME).getValue().toLowerCase();

            if (!tableNames.contains(tableNameXML)) {
                table.detach();
            }
        }

        return filteredSchema;
    }

    /**
     * Extracts the names of the tables that are used as sources by the provided query.
     *
     * @param query from which the table names need to be extracted.
     * @return A list of table names.
     */
    private List<String> getInvolvedTableFromQuery(String query) {

        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(query);
        } catch (JSQLParserException e) {
            throw new CannotBeParsedException("Input query could not be parsed: " + query);
        }

        List<String> tableNames = new TablesNamesFinder().getTableList(statement);
        tableNames.replaceAll(e -> e.replace("\"", ""));

        return tableNames;
    }
}
