package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class that generates rules for the aggregate functions such as MAX, AVG etc.
 */
public class GenAggregateFunctions {
    private static final String COUNT_STRING = "COUNT";

    /** Main, public method that generates the rules for the aggregate functions.
     *
     * @param plainSelect - query object to generate rules for
     * @return list of query objects which represent the rules for the aggregator function
     */
    public Set<String> generate(PlainSelect plainSelect) {
        // Check if there is a Function in one of the columns. If so, generate rules for it.
        Set<String> outputAfterAggregator = new TreeSet<>();

        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            if (selectItem instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                if (selectExpressionItem.getExpression() instanceof Function) {
                    // Here we know the selectItem is a function (AVG, SUM, MAX etc.)
                    //      so we can start adding the rules for it.
                    Function func = (Function) selectExpressionItem.getExpression();

                    if (plainSelect.getGroupBy() != null) {
                        outputAfterAggregator.add(firstRule(plainSelect).toString());
                        outputAfterAggregator.add(secondRule(plainSelect).toString());
                        outputAfterAggregator.add(thirdRule(plainSelect, func).toString());
                    } else if (!func.getName().equals("COUNT")) {
                        outputAfterAggregator.add(thirdRule(plainSelect, func).toString());
                    }

                    outputAfterAggregator.add(fourthRule(plainSelect, func).toString());
                }
            }
        }

        return outputAfterAggregator;
    }

    /**
     * Creates the aggregator statement that checks for at least one entry
     *  having a certain column. Example result:
     *
     *  `SELECT COUNT(*) FROM Movies HAVING count(distinct Director) > 1`
     *
     * @param plainSelect - select to add the part to
     * @return - select item in the above specified form
     */
    private PlainSelect firstRule(PlainSelect plainSelect) {
        // Get a deep copy of the plainSelect
        PlainSelect plainSelectOut = deepCopy(plainSelect, false);

        // Create COUNT(*) object
        Function count = getCountAllColumns();

        // Create selectItem object with the count in it
        SelectItem si = getSelectItemWithObject(count);

        List<SelectItem> selectItemList = new ArrayList<>();
        selectItemList.add(si);

        // Set selectItemList of plainSelectOut to be only the count object, overwriting the others
        plainSelectOut.setSelectItems(selectItemList);

        // Get selectItem inside the Group By clause
        Expression groupBy = plainSelect.getGroupBy().getGroupByExpressions().get(0);

        // Create COUNT(distinct groupByColumn) object
        Function countColumn = getCountDistinctColumn(groupBy, true);

        // Create count > 1
        GreaterThan greaterThan = getGreaterThan1(countColumn);

        // Add to plainselect
        plainSelectOut.setHaving(greaterThan);

        // You may guess what this line does by yourself
        return plainSelectOut;
    }

    /**
     * Adds "HAVING count(*)>1" to a plainSelect item.
     *  This is needed for handling aggregate operators.
     *
     * @param plainSelect - select to add the part to
     * @return - select item with the having part added
     */
    private PlainSelect secondRule(PlainSelect plainSelect) {
        PlainSelect plainSelectOut = deepCopy(plainSelect, true);

        // Create COUNT(*) object
        Function count = getCountAllColumns();

        // Create COUNT(*) > 1
        GreaterThan greaterThan = getGreaterThan1(count);

        // Add to plainselect
        plainSelectOut.setHaving(greaterThan);

        return plainSelectOut;
    }

    /**
     * Generates the third rule for the aggregator.
     *
     * @param plainSelect - query object to generate the rule for
     * @param function - function object that resides in the query
     * @return - query object representing the third rule for the aggregator
     */
    private PlainSelect thirdRule(PlainSelect plainSelect, Function function) {
        PlainSelect plainSelectOut = deepCopy(plainSelect, true);

        // Create COUNT(*) object
        Function count = getCountAllColumns();

        // Retrieve column in function
        Expression expr = function.getParameters().getExpressions().get(0);

        // Create count(*) > column in function
        GreaterThan leftGreaterThan = new GreaterThan();
        leftGreaterThan.setLeftExpression(count);
        leftGreaterThan.setRightExpression(getCountDistinctColumn(expr, false));

        // Create count(distinct FunctionColumn) > 1
        GreaterThan rightGreaterThan = getGreaterThan1(getCountDistinctColumn(expr, true));

        // Create AND
        BinaryExpression binaryExpression = new AndExpression(leftGreaterThan, rightGreaterThan);

        plainSelectOut.setHaving(binaryExpression);

        return plainSelectOut;
    }

    /**
     * Generates the fourth rule for the aggregator.
     *
     * @param plainSelect - query object to generate the rule for
     * @param function - function object that resides in the query
     * @return - query object representing the fourth rule for the aggregator
     */
    private PlainSelect fourthRule(PlainSelect plainSelect, Function function) {
        PlainSelect plainSelectOut = deepCopy(plainSelect, true);

        // Retrieve column in function
        Expression expr = function.getParameters().getExpressions().get(0);

        // Create left condition
        GreaterThan leftGreaterThan = new GreaterThan();
        leftGreaterThan.setLeftExpression(getCountDistinctColumn(expr, false));
        leftGreaterThan.setRightExpression(getCountDistinctColumn(expr, true));

        // Create right condition
        GreaterThan rightGreaterThan = getGreaterThan1(getCountDistinctColumn(expr, true));

        // Create AND
        BinaryExpression binaryExpression = new AndExpression(leftGreaterThan, rightGreaterThan);

        plainSelectOut.setHaving(binaryExpression);

        return plainSelectOut;
    }

    /**
     * Generates a `__ > 1` expression.
     *
     * @param expr - expression to fill in the __
     * @return `expr > 1` object
     */
    private GreaterThan getGreaterThan1(Expression expr) {
        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(expr);
        greaterThan.setRightExpression(new DoubleValue("1"));

        return greaterThan;
    }

    /**
     * Generates a COUNT(*) object.
     *
     * @return a COUNT(*) object
     */
    private Function getCountAllColumns() {
        Function count = new Function();
        count.setName(COUNT_STRING);
        count.setAllColumns(true);

        return count;
    }

    /**
     * Generates a COUNT(DISTINCT __) object.
     *
     * @param expression expression to fill in the __
     * @param distinct toggles whether or not you want to include DISTINCT
     * @return a COUNT(DISTINCT __) object
     */
    private Function getCountDistinctColumn(Expression expression, boolean distinct) {
        Function countColumn = new Function();
        countColumn.setName(COUNT_STRING);
        ExpressionList parameters = new ExpressionList(expression);
        countColumn.setParameters(parameters);
        countColumn.setDistinct(distinct);

        return countColumn;
    }

    /**
     * Generates a SelectItem but with a certain expression as content.
     *  This is a bit cumbersome, so this method eases that task.
     *
     * @param expression - expression to put in the SelectItem
     * @return a SelectItem with the expression inside
     */
    private SelectItem getSelectItemWithObject(Expression expression) {
        SelectExpressionItem selectExpressionItem = new SelectExpressionItem();
        selectExpressionItem.setExpression(expression);

        return selectExpressionItem;
    }

    /**
     * Returns a deep copy of a plainSelect. The idea here is that you use this
     *  to get a copy of the object, then again add the attributes that you wanted
     *  to change in the first place.
     *
     * @param plainSelect - object to copy
     * @param copyGroupBy - boolean to determine whether or not you want to also include the
     *                    GroupBy clause in the deep copy
     * @return deep copy of object
     */
    public static PlainSelect deepCopy(PlainSelect plainSelect, boolean copyGroupBy) {
        PlainSelect newPlainSelect = new PlainSelect();

        newPlainSelect.setSelectItems(plainSelect.getSelectItems());
        newPlainSelect.setFromItem(plainSelect.getFromItem());
        newPlainSelect.setHaving(plainSelect.getHaving());
        newPlainSelect.setWhere(plainSelect.getWhere());
        newPlainSelect.setJoins(plainSelect.getJoins());
        if (copyGroupBy) {
            newPlainSelect.setGroupByElement(plainSelect.getGroupBy());
        }

        return newPlainSelect;
    }
}
