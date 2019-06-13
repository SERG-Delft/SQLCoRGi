package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import nl.tudelft.st01.visitors.SelectStatementVisitor;
import nl.tudelft.st01.visitors.subqueries.SubqueryFinder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class contains functionality needed to generate coverage rules for subqueries.
 */
public final class SubqueryGenerator {

    /**
     * Prevents instantiation of this class.
     */
    private SubqueryGenerator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates coverage rules for subqueries in the given {@link PlainSelect}.
     *
     * @param plainSelect the plainSelect to cover.
     * @return a set of coverage rules in string form.
     */
    public static Set<String> coverSubqueries(PlainSelect plainSelect) {

        List<SubSelect> fromSubSelects = new LinkedList<>(extractSubqueriesFromFromItem(plainSelect.getFromItem()));

        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            fromSubSelects.addAll(extractSubqueriesFromJoins(joins));
        }

        Set<String> rules = new HashSet<>();

        for (SubSelect subSelect : fromSubSelects) {
            Set<String> subRules = new HashSet<>();
            SelectStatementVisitor selectStatementVisitor = new SelectStatementVisitor(subRules);
            subSelect.getSelectBody().accept(selectStatementVisitor);
            rules.addAll(subRules);
        }

        // TODO: find subselects in where and have

        SubqueryFinder subqueryFinder = new SubqueryFinder();

        Expression where = plainSelect.getWhere();
        if (where != null) {
            where.accept(subqueryFinder);
            Set<String> whereSubs = subqueryFinder.getSubqueries();
        }

        Expression having = plainSelect.getHaving();
        if (having != null) {
            having.accept(subqueryFinder);
            Set<String> havingSubs = subqueryFinder.getSubqueries();
        }

        // TODO: Remove subqueries, check left and right of AND/ORs and propagate removal if needed


        return rules;
    }

    /**
     * Extracts all {@link SubSelect}s from a given list of {@link Join}s.
     *
     * @param joins the list of joins from which subqueries must be extracted.
     * @return a list of subqueries that have been found in the list of joins.
     */
    private static List<SubSelect> extractSubqueriesFromJoins(List<Join> joins) {

        List<SubSelect> subqueries = new LinkedList<>();

        for (Join join : joins) {
            subqueries.addAll(extractSubqueriesFromFromItem(join.getRightItem()));
        }

        return subqueries;
    }

    /**
     * Extracts all {@link SubSelect}s from a given {@link FromItem}.
     *
     * @param fromItem the fromItem from which subqueries must be extracted.
     * @return a list of subqueries that have been found in the fromItem.
     */
    private static List<SubSelect> extractSubqueriesFromFromItem(FromItem fromItem) {

        List<SubSelect> subqueries = new LinkedList<>();

        if (fromItem instanceof SubJoin) {
            subqueries.addAll(extractSubqueriesFromSubJoin((SubJoin) fromItem));
        } else if (fromItem instanceof SubSelect) {
            subqueries.add((SubSelect) fromItem);
        }

        return subqueries;
    }

    /**
     * Extracts all {@link SubSelect}s from a given {@link SubJoin}.
     *
     * @param subJoin the subJoin from which subqueries must be extracted.
     * @return a list of subqueries that have been found in the subJoin.
     */
    private static List<SubSelect> extractSubqueriesFromSubJoin(SubJoin subJoin) {

        List<SubSelect> subSelects = extractSubqueriesFromFromItem(subJoin.getLeft());
        subSelects.addAll(extractSubqueriesFromJoins(subJoin.getJoinList()));

        return subSelects;
    }

}
