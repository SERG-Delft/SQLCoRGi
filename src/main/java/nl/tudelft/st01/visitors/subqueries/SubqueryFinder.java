package nl.tudelft.st01.visitors.subqueries;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.HashSet;
import java.util.Set;

/**
 * Finds all subqueries in an {@link Expression} and collects their string representations in a set.
 */
public class SubqueryFinder extends ExpressionVisitorAdapter {

    private Set<String> subqueries = new HashSet<>();

    @Override
    public void visit(SubSelect subSelect) {
        subqueries.add(subSelect.toString());
    }

    public Set<String> getSubqueries() {
        Set<String> temp = this.subqueries;
        this.subqueries = new HashSet<>();
        return temp;
    }

}
