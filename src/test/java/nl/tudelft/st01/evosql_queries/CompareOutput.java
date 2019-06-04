package nl.tudelft.st01.evosql_queries;

import nl.tudelft.st01.Generator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
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

        Scanner sc = null;
        try {
            sc = new Scanner(new FileReader(basePath + suitecrm_input));
        } catch (Exception e) {
            e.printStackTrace();
        }
        int i = 2;
        while(sc.hasNextLine()) {
            String query = sc.nextLine();
            System.out.println(query);

            Set<String> result = Generator.generateRules(query);

            System.out.println("-----------------");
            System.out.println(result.size() + " ^^ , below here is line: " + i);
            i++;
            System.out.println("-----------------");

        }
    }
}
