package com.github.sergdelft.sqlcorgi;

import com.github.sergdelft.sqlcorgi.exceptions.CannotBeNullException;
import com.github.sergdelft.sqlcorgi.exceptions.CannotBeParsedException;
import com.github.sergdelft.sqlcorgi.exceptions.UnsupportedInputException;
import com.github.sergdelft.sqlcorgi.schema.Schema;
import com.github.sergdelft.sqlcorgi.schema.TableStructure;
import com.github.sergdelft.sqlcorgi.visitors.SelectStatementVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

import java.util.HashSet;

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

        TableStructure tableStructure = new TableStructure();
        tableStructure.setSchema(schema);
        SelectStatementVisitor selectStatementVisitor = new SelectStatementVisitor(tableStructure, result);
        selectBody.accept(selectStatementVisitor);

        return result;
    }

    public static void main(String[] args) {
        for (String s : generateRules("SELECT * FROM t1 WHERE t1.c1 + 7 > t1.c2 - 8", null)) {
            System.out.println("\"" + s + "\",");
        }
        System.out.println();
    }
}
