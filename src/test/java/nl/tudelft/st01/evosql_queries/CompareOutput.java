package nl.tudelft.st01.evosql_queries;

import nl.tudelft.st01.Generator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URI;
import java.net.URL;

public class CompareOutput {


    public static void main(String[] args) {
        String basePath = "C:/Users/Timon Bestebreur/Dropbox/Opleiding/Technische Informatica/Y2/Context Project/coverage_generator/src/test/java/nl/tudelft/st01/evosql_queries/query_resources/";

        String suitecrm_input = "suitecrm_input.txt";
        String suitecrm_output = "suitecrm_output.json";
        String espocrm_input = "espocrm_input.txt";
        String espocrm_output = "espocrm_output.json";
        String erpnext_input = "erpnext_input.txt";
        String erpnext_output = "erpnext_output.json";

        String[] input = {basePath + erpnext_input, basePath + suitecrm_input, basePath + espocrm_input};
        String[] output = {basePath + erpnext_output, basePath + suitecrm_output, basePath + espocrm_output};

        Scanner sc = null;
        JSONParser parser = new JSONParser();
        Object object = null;
        int counter = 0;

        for(int i = 0; i <= 2; i++) {
            try {
                sc = new Scanner(new FileReader(input[i]));
                object = parser.parse(new FileReader(output[i]));

            } catch (Exception e) {
                e.printStackTrace();
            }

            JSONObject jsonObject = (JSONObject) object;

            JSONArray entries = (JSONArray) jsonObject.get("entries");
            Iterator<JSONObject> iterator = entries.iterator();

            // nu kunnen we gaan!

            while(iterator.hasNext() && sc.hasNextLine()) {
                JSONArray queries = (JSONArray) iterator.next().get("pathList");
                String nextQuery = sc.nextLine();
                Set<String> ourResults = null;
                try {
                     ourResults = Generator.generateRules(nextQuery);
                } catch (Exception e) {
                    System.out.println(nextQuery);
                    System.out.println("The query on the previous line caused the following exception: " + e.getMessage());
                    System.out.println("i: " + i);
                }
                int ourResultSize = ourResults.size();
                int expectedResultSize = queries.size();

                if (ourResultSize != expectedResultSize) {
                    counter++;
                    System.out.println(counter + ") The following query is not yet handled correctly: ");
                    System.out.println(nextQuery);
                    System.out.printf("Expected %d rules, got %d\n", expectedResultSize, ourResultSize);
                    System.out.println("These queries were expected:");
                    for (Object o : queries) {
                        System.out.println(o.toString());
                    }
                    System.out.println("\n\n");
                }
            }
        }




    }
}
