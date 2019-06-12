package nl.tudelft.st01;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import nl.tudelft.st01.util.exceptions.CannotBeNullException;
import nl.tudelft.st01.util.exceptions.CannotBeParsedException;
import nl.tudelft.st01.util.exceptions.ShouldNotBeInstantiatedException;
import nl.tudelft.st01.util.exceptions.UnsupportedInputException;
import nl.tudelft.st01.visitors.SelectStatementVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * The entry point of the coverage rule generator.
 */
public final class Generator {

    /**
     * No instance of this class should be created.
     */
    private Generator() {
        throw new ShouldNotBeInstantiatedException();
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
    public static void main(String[] args) {

        String query = "SELECT * FROM t WHERE a < 10 AND a > 0";

        Set<String> result = generateRules(query);

        System.out.println("Result:");
        for (String s : result) {
            System.out.println(s);
        }
    }

}
