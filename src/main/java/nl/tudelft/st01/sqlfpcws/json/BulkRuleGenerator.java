package nl.tudelft.st01.sqlfpcws.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BulkRuleGenerator {

    private List<String> queriesToParse;
    private Document schema;

    private FileWriter jsonWriter;
    private Gson gson;

    private SQLjson sqlJson;
    private int queryNo;


    public BulkRuleGenerator(String sqlInputPath, String xmlSchemaPath, String jsonOutputPath) {
        setUpQueriesToParse(sqlInputPath);
        setUpSchemaDocument(xmlSchemaPath);
        setUpOutputJsonWriter(jsonOutputPath);

        this.queryNo = 0;
        this.sqlJson = new SQLjson();
    }

    private void setUpQueriesToParse(String sqlInputPath) {
        try (Stream<String> stream = Files.lines(Paths.get(sqlInputPath))) {
            queriesToParse = stream
                    .filter(query -> !query.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Could not open schema file: " + e.getMessage());
            queriesToParse = new ArrayList<>();
        }
    }

    private void setUpSchemaDocument(String xmlSchemaPath) {
        try {
            schema = new SAXReader().read(new File(xmlSchemaPath));
        } catch (DocumentException e) {
            System.err.println("Schema could not be parsed: " + e.getMessage());
            schema = DocumentHelper.createDocument();
        }
    }

    private void setUpOutputJsonWriter(String jsonOutputPath) {
        try {
            jsonWriter = new FileWriter(jsonOutputPath);
        } catch (IOException e) {
            System.err.println("Output file could not be opened: " + e.getMessage());
        }

        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
    }



}
