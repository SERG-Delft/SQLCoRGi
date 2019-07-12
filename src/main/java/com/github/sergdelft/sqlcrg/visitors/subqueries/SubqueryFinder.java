package com.github.sergdelft.sqlcrg.visitors.subqueries;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.HashMap;
import java.util.Map;

/**
 * Finds all subqueries in an {@link Expression} and collects their string representations and references.
 */
public class SubqueryFinder extends ExpressionVisitorAdapter {

    private Map<String, SubSelect> subqueries = new HashMap<>();

    @Override
    public void visit(SubSelect subSelect) {
        subqueries.put(subSelect.toString(), subSelect);
    }

    public Map<String, SubSelect> getSubqueries() {
        return this.subqueries;
    }

}
