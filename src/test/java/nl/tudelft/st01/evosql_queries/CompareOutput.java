package nl.tudelft.st01.evosql_queries;

import nl.tudelft.st01.Generator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.util.Scanner;
import java.util.Set;

public class CompareOutput {
    private static String path = "C:/Users/Timon Bestebreur/Dropbox/Opleiding/Technische Informatica/Y2/Context Project/coverage_generator/src/test/java/nl/tudelft/st01/evosql_queries/suitecrm_input.txt";
    static Scanner sc;
    static File f;

    public static void main(String[] args) {
        System.out.println("Latsgooo");
        try {
            f = new File(path);
            sc = new Scanner(new FileReader(path));
        } catch (FileNotFoundException e) {
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
