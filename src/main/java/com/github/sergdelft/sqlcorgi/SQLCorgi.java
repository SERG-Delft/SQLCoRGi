package com.github.sergdelft.sqlcorgi;

import com.github.sergdelft.sqlcorgi.exceptions.CannotBeNullException;
import com.github.sergdelft.sqlcorgi.exceptions.CannotBeParsedException;
import com.github.sergdelft.sqlcorgi.exceptions.UnsupportedInputException;
import com.github.sergdelft.sqlcorgi.schema.Schema;
import com.github.sergdelft.sqlcorgi.visitors.SelectStatementVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The entry point of the coverage rule generator.
 */
public final class SQLCorgi {

    /**
     * No instance of this class should be created.
     */
    private SQLCorgi() {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates coverage targets for the given query.
     *
     * @param query the query for which coverage rules should be generated.
     * @param schema The database schema.
     * @return the rules that are generated for the input query.
     */
    public static Set<String> generateRules(String query, Schema schema) {
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

    public static void main(String[] args) {
        List<String> set = new ArrayList<>(generateRules("SELECT * FROM a, b, c, d WHERE a.l = 6 AND d.id = b.id AND b.id = c.id", null));
        Collections.sort(set);
        for (String s : set) {
            System.out.println(s);
        }

    }
}