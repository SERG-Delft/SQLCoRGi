package nl.tudelft.st01;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import nl.tudelft.st01.exceptions.CannotBeNullException;
import nl.tudelft.st01.exceptions.CannotBeParsedException;
import nl.tudelft.st01.exceptions.UnsupportedInputException;
import nl.tudelft.st01.visitors.SelectStatementVisitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * The entry point of the coverage rule generator.
 */
@SuppressWarnings("checkstyle")
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
    // It's neater to throw a CannotBeParsedException instead of printing something. PMD doesn't like it, so
    // we have to suppress the warning that we should not throw a new exception in a catch block.
    @SuppressWarnings({"PMD.PreserveStackTrace"})
    public static Set<String> generateRules(String query) {
        Set<String> result = new HashSet<>();

        if (query == null) {
            throw new CannotBeNullException("Input cannot be null.");
        }

        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(query);
        } catch (JSQLParserException e) {
            throw new CannotBeParsedException("Input query could not be parsed.");
        }

        if (!(statement instanceof Select)) {
            throw new UnsupportedInputException("Only SELECT statements are supported.");
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
                "SELECT * FROM Movies WHERE year IS NULL",
                "SELECT MAX(duration) FROM Movies GROUP BY year",
                "select parent, options from tabDocField where fieldtype='Table' and options in (select name from tabDocType where istable='1'and name in ('Portal Settings', 'Workflow Transition', 'Page Role', 'Stock Settings', 'Event Role', 'Authorization Rule', 'Email Alert Recipient', 'DocPerm', 'Portal Menu Item', 'Accounts Settings', 'Custom DocPerm', 'ToDo', 'Workflow Document State', 'UserRole'))",
                "SELECT sum(debit) from \"tabGL Entry\" gle WHERE posting_date <= '2013-02-14' and posting_date >= '2013-01-01' and voucher_type != 'Period Closing Voucher' and exists ( select name from \"tabaccount\" ac where ac.name = gle.account and ac.lft >='377'and ac.rgt <='386')",
                "SELECT t.* FROM (SELECT * FROM table WHERE a = 0) t",
            };

            for (String query : queries) {

                System.out.println("Current query: " + query);
                scanner.nextLine();

                try {
                    printResults(generateRules(query));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                scanner.nextLine();
            }

            System.out.println("That's all folks!");

        } else if ("sandbox".equals(choice)) {
            System.out.println("Sandbox: Enter any query for which you would like to generate coverage targets.\n"
                + "Enter \"quit\" instead to leave.");

            while (!(choice = scanner.nextLine()).equals("quit")) {
                try {
                    printResults(Generator.generateRules(choice));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
