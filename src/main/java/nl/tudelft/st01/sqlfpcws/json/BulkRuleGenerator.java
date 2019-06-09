package nl.tudelft.st01.sqlfpcws.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import nl.tudelft.st01.sqlfpcws.SQLFpcWS;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BulkRuleGenerator {

    private List<String> queriesToParse;
    private Document schema;
    private String jsonOutputPath;

    private SQLjson sqlJson;
    private int queryNo;

    private static final int RULE_GENERATOR_INTERVAL = 1000;

    private static final String SCHEMA_TABLE_XPATH = "/schema/table";
    private static final String SCHEMA_COLUMN_XPATH = "/schema/table/column";

    private static final String TABLE_ATTRIBUTE_NAME = "name";


    public BulkRuleGenerator(String sqlInputPath, String xmlSchemaPath, String jsonOutputPath) {
        setUpQueriesToParse(sqlInputPath);
        setUpSchemaDocument(xmlSchemaPath);
        this.jsonOutputPath = jsonOutputPath;

        this.queryNo = 0;
        this.sqlJson = new SQLjson();
    }

    private void setUpQueriesToParse(String sqlInputPath) {
        try (Stream<String> stream = Files.lines(Paths.get(this.getClass().getResource(sqlInputPath).toURI()))) {
            queriesToParse = stream
                    .filter(query -> !query.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException | URISyntaxException e) {
            System.err.println("Could not open schema file: " + e.getMessage());
            queriesToParse = new ArrayList<>();
        }
    }

    private void setUpSchemaDocument(String xmlSchemaPath) {
        try {
            schema = new SAXReader().read(this.getClass().getResourceAsStream(xmlSchemaPath));
        } catch (DocumentException e) {
            System.err.println("Schema could not be parsed: " + e.getMessage());
            schema = DocumentHelper.createDocument();
        }
    }

    private void outputToJsonFile() {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();

        try (FileWriter jsonWriter = new FileWriter(jsonOutputPath)) {
            gson.toJson(sqlJson, jsonWriter);
            jsonWriter.flush();
        } catch (IOException e) {
            System.err.println("Unable to write to output file: " + e.getMessage());
        }
    }

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

    @SuppressWarnings("unchecked")
    private Document filterSchema(String query) {
        List<String> tableNames = getInvolvedTableFromQuery(query);
        Document filteredSchema = (Document)schema.clone();

        tableNames.replaceAll(String::toLowerCase);

        List<Node> tables = (List<Node>)filteredSchema.selectNodes(SCHEMA_TABLE_XPATH);
        for (Node table : tables) {
            String tableNameXML = ((Element) table).attribute(TABLE_ATTRIBUTE_NAME).getValue().toLowerCase();

            if(!tableNames.contains(tableNameXML)) {
                table.detach();
            }
        }

        return filteredSchema;
    }

    private List<String> getInvolvedTableFromQuery(String query) {
        List<String> tableNames = Collections.emptyList();

        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(query);
        } catch (JSQLParserException e) {
            System.err.println("Input query could not be parsed: " + e.getMessage());
            return tableNames;
        }

        tableNames = new TablesNamesFinder().getTableList(statement);
        tableNames.replaceAll(e -> e.replace("\"", ""));


        return tableNames;
    }

    public String statisticsString() {
        StringBuilder sBuilder = new StringBuilder();

        long queries = queriesToParse.size();
        long tables = schema.selectNodes(SCHEMA_TABLE_XPATH).size();
        long columns = schema.selectNodes(SCHEMA_COLUMN_XPATH).size();
        long duration = RULE_GENERATOR_INTERVAL * 1000000 * queries;

        LocalDateTime finishTime = LocalDateTime.now().plusNanos(duration);
        String formattedFinishTime = finishTime.format(DateTimeFormatter.ISO_DATE_TIME);

        sBuilder.append("- Number of queries provided: " + queries + "\n")
                .append("- Number of tables in schema: " + tables + "\n")
                .append("- Number of columns in schema: " + columns + "\n")
                .append("- Estimated completion time: " + formattedFinishTime + "\n");

        return sBuilder.toString();
    }
}
