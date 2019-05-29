package nl.tudelft.st01.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

/**
 * Provides utility functions for JSQLParser query objects.
 */
public final class Queries {

    /**
     * No instances of this class can be created.
     */
    private Queries() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a deep copy of an {@link Expression}. This can be useful if you need to modify part of an expression,
     * but other parts of your code need to use the unmodified expression.
     *
     * @param expression the expression that needs to be copied.
     * @return a copy of {@code expression}.
     */
    public static Expression copy(Expression expression) {

        try {
            return CCJSqlParserUtil.parseCondExpression(expression.toString(), false);
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        return null;
    }

}
