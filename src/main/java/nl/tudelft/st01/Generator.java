package nl.tudelft.st01;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Utility class for coverage target generation.
 */
public final class Generator {

    /**
     * Nope.
     */
    private Generator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates coverage targets for a given query.
     * @param query the query that needs to be covered.
     * @return the generated queries.
     */
    public static Set<String> generateRules(String query) {

        Set<String> result = new HashSet<>();

        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(query);
        } catch (JSQLParserException e) {
            System.err.println("Input query could not be parsed.");
            return result;
        }

        if (!(statement instanceof Select)) {
            System.err.println("Only SELECT statements are accepted.");
            return result;
        }

        SelectBody selectBody = ((Select) statement).getSelectBody();
        RuleGeneratorSelectVisitor ruleGeneratorSelectVisitor = new RuleGeneratorSelectVisitor();
        ArrayList<PlainSelect> plainSelects = new ArrayList<>();
        ruleGeneratorSelectVisitor.setOutput(plainSelects);
        selectBody.accept(ruleGeneratorSelectVisitor);

        for (PlainSelect plainSelect : plainSelects) {
            result.add(plainSelect.toString());
        }

        return result;
    }

    /**
     * Example query to try out the generator.
     * @param args unused.
     */
    @SuppressWarnings("checkstyle:innerAssignment")
    public static void main(String[] args) {

        System.out.println("Enter \"demo\" to run the generator for a precompiled list of queries.\n"
            + "If you want to try out your own query, enter \"sandbox\" instead.");
        Scanner scanner = new Scanner(System.in, "UTF-8");
        String choice = scanner.nextLine();

        if ("demo".equals(choice)) {
            System.out.println("Usage: Press <RETURN> to start generation for the query displayed.\n"
                + "Once the queries have been generated, you can move on by pressing <RETURN> again.");

            String[] queries = {
                "invalid",
                "SELECT * FROM Movies",
                "SELECT * FROM Movies WHERE year < 2000",
                "UPDATE Account SET balance = 999999999 WHERE id = 123",
            };

            for (String query : queries) {

                System.out.println("Current query: " + query);
                scanner.nextLine();

                printResults(generateRules(query));
                scanner.nextLine();
            }
        } else if ("sandbox".equals(choice)) {
            System.out.println("Sandbox: Enter any query for which you would like to generate coverage targets.\n"
                + "Enter \"quit\" instead to leave.");

            while (!(choice = scanner.nextLine()).equals("quit")) {
                printResults(Generator.generateRules(choice));
            }
        } else {
            System.out.println("You make me sad.");
        }
        scanner.close();
    }

    /**
     * Very well-designed function that will be erased from existence within a few days from writing this.
     * @param result Oh no.
     */
    private static void printResults(Set<String> result) {
        String[] a = result.toArray(new String[0]);
        Arrays.sort(a);

        System.out.println("Generated " + a.length + (a.length == 1 ? " query" : " queries") + ":\n");
        for (int i = 0; i < a.length; i++) {
            System.out.println(i + 1 + ":\t" + a[i]);
        }
        System.out.println("\n=======================================================================================");
    }

}
