package nl.tudelft.st01.visitors.join;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This visitor allows for extracting the column used in the given expression.
 */
public class RuleGeneratorOnExpressionVisitor extends ExpressionVisitorAdapter {

    private Map<String, List<Expression>> output = new HashMap<>();

    /**
     * Stores each column corresponding to its table.
     * @param e The column that should be added.
     */
    private void updateColumnList(Column e) {
        String table = e.getTable().toString().toLowerCase();

        if (!output.containsKey(table)) {
            List<Expression> list = new ArrayList<>();
            list.add(e);
            output.put(table, list);
        } else if (!contains(output.get(table), e)) {
            output.get(table).add(e);
        }
    }

    public void setOutput(Map map) {
        this.output = map;
    }

    /**
     * Ensures that the list of columns used in the on expression contains no duplicate column names.
     * NOTE: Suppose the column name "id" in "Movies" and the column name is unique.
     * Yet, the function considers Movies.id to be different from id.
     * @param list list
     * @param expression e
     * @return boolean
     */
    private static boolean contains(List<Expression> list, Expression expression) {
        if (list == null) {
            return false;
        }
        for (Expression e : list) {
            if (e.toString().toLowerCase().equals(expression.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(Column column) {
        updateColumnList(column);
    }

}
