package nl.tudelft.st01;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import nl.tudelft.st01.visitors.SelectStatementVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * The entry point of the coverage rule generator.
 */
public final class Generator {

    /**
     * No instance of this class should be created.
     */
    private Generator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates coverage targets for the given query.
     *
     * @param query the query for which coverage rules should be generated.
     * @return the rules that are generated for the input query.
     */
    public static Set<String> generateRules(String query) {
        Set<String> result = new HashSet<>();

        if (query == null) {
            System.err.println("Input cannot be null.");
            return result;
        }

        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(query);
        } catch (JSQLParserException e) {
            System.err.println("Input query could not be parsed.");
            return result;
        }

        if (!(statement instanceof Select)) {
            System.err.println("Only SELECT statements are supported.");
            return result;
        }

        SelectBody selectBody = ((Select) statement).getSelectBody();

        SelectStatementVisitor selectStatementVisitor = new SelectStatementVisitor(result);
        selectBody.accept(selectStatementVisitor);

        return result;
    }

    /**
     * Main method for manual testing.
     *
     * @param args unused.
     */
    @SuppressWarnings("checkstyle:innerAssignment")
    public static void main(String[] args) {
        String query = "SELECT * FROM a INNER JOIN b ON a.id = b.id OR a.length < b.length WHERE b.length IS NOT NULL";

        System.out.println("Enter \"demo\" to run the generator for a precompiled list of queries.\n"
            + "If you want to try out your own query, enter \"sandbox\" instead.");
        Scanner scanner = new Scanner(System.in, "UTF-8");
        String choice = scanner.nextLine();

        if ("demo".equals(choice)) {
            System.out.println("Usage: Press <RETURN> to start generation for the query displayed.\n"
                + "Once the queries have been generated, you can move on by pressing <RETURN> again.");

            String[] queries = {
                "SELECT * FROM Movies WHERE year < 2000",
                "SELECT * FROM Movies WHERE year < 2000 AND id <> 30 OR title = 'Generic Movie Title'",
                "SELECT * FROM Movies WHERE title NOT LIKE '%generic%'",
                "SELECT * FROM Movies WHERE year BETWEEN 1980 AND 1987",
                "SELECT * FROM Movies WHERE Id IN (8, 9, 10)",
                "SELECT * FROM Movies WHERE year IS NULL",
                "SELECT director FROM Movies GROUP BY director",
                "SELECT MAX(duration) FROM Movies GROUP BY year",
                "SELECT director, COUNT(*) FROM Movies GROUP BY director HAVING director LIKE 'B%'",
                "SELECT * FROM Movies INNER JOIN Boxoffice ON Movies.id = Boxoffice.movie_id",
                "SELECT * FROM Movies RIGHT JOIN Boxoffice ON Movies.id = Boxoffice.movie_id",
                "UPDATE Account SET balance = 999999999 WHERE id = 123",
                "CAN'T PARSE THIS",
            };

            for (String querie : queries) {

                System.out.println("Current query: " + querie);
                scanner.nextLine();

                printResults(generateRules(querie));
                scanner.nextLine();
            }

            System.out.println("That's all folks!");

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
