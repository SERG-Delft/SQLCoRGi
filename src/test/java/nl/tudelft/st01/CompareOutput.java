package nl.tudelft.st01;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.Set;

public class CompareOutput {
    private static String path = "C:/Users/Timon Bestebreur/Dropbox/Opleiding/Technische Informatica/Y2/Context Project/st-01/src/main/resources/example_queries.txt";
    static Scanner sc;

    public static void main(String[] args) {
        System.out.println("hi");
        try {
            sc = new Scanner(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(sc.hasNextLine()) {
            String query = sc.nextLine();
            System.out.println(query);

            Set<String> result = Generator.generateRules(query);

            System.out.println("-----------------");
            System.out.println(result.size());
            System.out.println("-----------------");

        }
    }
}
