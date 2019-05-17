package nl.tudelft.st01;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

import java.util.ArrayList;
import java.util.HashSet;
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
            System.out.println("Input query could not be parsed.");
        }

        if (!(statement instanceof Select)) {
            throw new IllegalArgumentException("Only SELECT statements are accepted.");
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
    public static void main(String[] args) {
        String query = "SELECT * FROM Movies  INNER JOIN Boxoffice ON (Movies.id = Boxoffice.movie_id) "
                + "INNER JOIN Boxoffice ON (Boxoffice.rating < Movies.length) WHERE Movies.id > 4";
        Set<String> result = generateRules(query);

        System.out.println("Result: " + result.toString());
    }

}
