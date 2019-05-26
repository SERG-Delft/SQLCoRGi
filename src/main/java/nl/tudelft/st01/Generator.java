package nl.tudelft.st01;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import nl.tudelft.st01.visitors.RuleGeneratorSelectVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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

        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(query);
        } catch (JSQLParserException e) {
            System.out.println("Input query could not be parsed.");
            return result;
        }

        if (!(statement instanceof Select)) {
            throw new IllegalArgumentException("Only SELECT statements are accepted.");
        }

        SelectBody selectBody = ((Select) statement).getSelectBody();

        RuleGeneratorSelectVisitor ruleGeneratorSelectVisitor = new RuleGeneratorSelectVisitor();
        ArrayList<PlainSelect> plainSelects = new ArrayList<>();

        Set<String> out = new TreeSet<>();
        ruleGeneratorSelectVisitor.setOutput(out);
        selectBody.accept(ruleGeneratorSelectVisitor);

        result.addAll(out);
        for (PlainSelect plainSelect : plainSelects) {
            result.add(plainSelect.toString());
        }

        return result;
    }


    /**
     * Main method for manual testing.
     *
     * @param args unused.
     */
    public static void main(String[] args) {

        String query = "SELECT * FROM Movies INNER JOIN a ON Movies.id > 5";
        Set<String> result = generateRules(query);

        System.out.println("Result:");
        for (String s : result) {
            System.out.println(s);
        }
    }

}
