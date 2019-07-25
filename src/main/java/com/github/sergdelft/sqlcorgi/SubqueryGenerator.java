package com.github.sergdelft.sqlcorgi;

import com.github.sergdelft.sqlcorgi.exceptions.CannotBeParsedException;
import com.github.sergdelft.sqlcorgi.schema.Schema;
import com.github.sergdelft.sqlcorgi.schema.TableStructure;
import com.github.sergdelft.sqlcorgi.util.cloner.SelectCloner;
import com.github.sergdelft.sqlcorgi.visitors.SelectStatementVisitor;
import com.github.sergdelft.sqlcorgi.visitors.subqueries.SubqueryFinder;
import com.github.sergdelft.sqlcorgi.visitors.subqueries.SubqueryRemover;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;

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
     * @param tableStructure the {@code TableStructure} obtained from the main query.
     * @return a set of coverage rules in string form.
     */
    public static Set<String> coverSubqueries(PlainSelect plainSelect, TableStructure tableStructure) {

        Set<String> rules = new HashSet<>();

        coverFromSubqueries(plainSelect, tableStructure.getSchema(), rules);
        coverSelectOperatorSubqueries(plainSelect, tableStructure, rules);

        return rules;
    }

    /**
     * Generates coverage targets for subqueries found in the WHERE and HAVING clauses of a query. Rules that are
     * generated will be added to the provided set.
     * @param plainSelect the query to cover.
     * @param tableStructure the table structure that provides the context of the main query.
     * @param rules a set in which all generated rules should be stored.
     */
    private static void coverSelectOperatorSubqueries(PlainSelect plainSelect, TableStructure tableStructure, Set<String> rules) {

        Map<String, SubSelect> whereSubs = obtainSubqueries(plainSelect.getWhere());
        Map<String, SubSelect> havingSubs = obtainSubqueries(plainSelect.getHaving());

        Map<String, SubSelect> combinedSubs = new HashMap<>(whereSubs.size() + havingSubs.size());
        combinedSubs.putAll(whereSubs);
        combinedSubs.putAll(havingSubs);

        for (Map.Entry<String, SubSelect> entry : combinedSubs.entrySet()) {

            String subquery = entry.getKey();
            SubSelect subCopy = (SubSelect) SelectCloner.copy(entry.getValue());

            PlainSelect selectCopy = (PlainSelect) SelectCloner.copy(plainSelect);

            boolean isWhereSub = whereSubs.containsKey(subquery);
            boolean isHavingSub = havingSubs.containsKey(subquery);

            removeSubquery(subquery, selectCopy, isWhereSub, isHavingSub);

            HashSet<String> mutations = new HashSet<>();

            SelectStatementVisitor selectVisitor = new SelectStatementVisitor(tableStructure, mutations);
            subCopy.getSelectBody().accept(selectVisitor);

            for (String mutation : mutations) {
                createSelectExprRules(mutation, selectCopy, isWhereSub, isHavingSub, rules);
            }
        }
    }

    /**
     * Creates one or two coverage rules for the WHERE and/or HAVING expressions of {@code query} using the given
     * {@code mutation}, depending on the values of {@code forWhere} and {@code forHaving}. The rules are added to the
     * specified {@code rules} set.
     *
     * @param mutation is the mutation of a subquery that should be used in the coverage rules.
     * @param query the query for which the coverage targets must be generated.
     * @param forWhere states whether a rule should be generated for the WHERE expression.
     * @param forHaving states whether a rule should be generated for the HAVING expression.
     * @param rules the set to which the coverage rules need to be added.
     */
    private static void createSelectExprRules(String mutation, PlainSelect query, boolean forWhere, boolean forHaving,
                                              Set<String> rules) {
        ExistsExpression existsExpression = new ExistsExpression();
        SubSelect existsSub = new SubSelect();
        existsExpression.setRightExpression(existsSub);
        try {
            existsSub.setSelectBody(((Select) CCJSqlParserUtil.parse(mutation)).getSelectBody());
        } catch (JSQLParserException e) {
            throw new CannotBeParsedException();
        }

        if (forWhere) {
            Expression where = query.getWhere();
            if (where == null) {
                query.setWhere(existsExpression);
            } else {
                query.setWhere(new AndExpression(existsExpression, where));
            }
            rules.add(query.toString());
            query.setWhere(where);
        }

        if (forHaving) {
            Expression having = query.getHaving();
            if (having == null) {
                query.setHaving(existsExpression);
            } else {
                query.setHaving(new AndExpression(existsExpression, having));
            }
            rules.add(query.toString());
            query.setHaving(having);
        }
    }

    /**
     * Removes {@code subquery} from {@code query}'s WHERE and/or HAVING expressions, depending on whether {@code
     * fromWhere} and {@code fromHaving} are set.
     *
     * @param subquery the subquery to remove from {@code query}.
     * @param query the query from which the given {@code subquery} needs to be removed.
     * @param fromWhere whether the {@code subquery} needs to be removed from {@code query}'s WHERE expression.
     * @param fromHaving whether the {@code subquery} needs to be removed from {@code query}'s HAVING expression.
     */
    private static void removeSubquery(String subquery, PlainSelect query, boolean fromWhere, boolean fromHaving) {

        if (fromWhere) {
            SubqueryRemover subqueryRemover = new SubqueryRemover(subquery);
            query.getWhere().accept(subqueryRemover);
            if (subqueryRemover.isUpdateChild()) {
                query.setWhere(subqueryRemover.getChild());
            }
        }

        if (fromHaving) {
            SubqueryRemover subqueryRemover = new SubqueryRemover(subquery);
            query.getHaving().accept(subqueryRemover);
            if (subqueryRemover.isUpdateChild()) {
                query.setHaving(subqueryRemover.getChild());
            }
        }
    }

    /**
     * Generates coverage targets for subqueries found in the FROM clause of a query. Rules that are generated will
     * be added to the provided set.
     *  @param plainSelect the SELECT to cover.
     * @param schema the schema used for the query.
     * @param rules a set in which all generated rules should be stored.
     */
    private static void coverFromSubqueries(PlainSelect plainSelect, Schema schema, Set<String> rules) {

        List<SubSelect> fromSubSelects = new LinkedList<>(extractSubqueriesFromFromItem(plainSelect.getFromItem()));

        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            fromSubSelects.addAll(extractSubqueriesFromJoins(joins));
        }

        for (SubSelect subSelect : fromSubSelects) {
            Set<String> subRules = new HashSet<>();
            TableStructure tableStructure = new TableStructure();
            tableStructure.setSchema(schema);
            SelectStatementVisitor selectStatementVisitor = new SelectStatementVisitor(tableStructure, subRules);
            subSelect.getSelectBody().accept(selectStatementVisitor);
            rules.addAll(subRules);
        }
    }

    /**
     * Obtains a collection of subqueries from a given {@link Expression}.
     *
     * @param expression the {@code Expression} from which subqueries should be obtained.
     * @return a map containing string representations of all subqueries found in the expression and a reference to
     *         the respective subqueries.
     */
    private static Map<String, SubSelect> obtainSubqueries(Expression expression) {

        if (expression == null) {
            return new HashMap<>(0);
        }

        SubqueryFinder subqueryFinder = new SubqueryFinder();
        expression.accept(subqueryFinder);

        return subqueryFinder.getSubqueries();
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
