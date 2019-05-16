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
import java.util.List;

/**
 * Class that generates rules for the Aggregate functions such as MAX, AVG etc.
 */
public class GenAggregateFunctions {
    private final String countString = "COUNT";

    /** Main, public method that generates the rules for the aggregate functions.
     *
     * @param plainSelect - query object to generate rules for
     * @return list of query objects which represent the rules for the aggregator function
     */
    public List<PlainSelect> generate(PlainSelect plainSelect) {
        // check if there is a Function in one of the columns
        // for now assuming there is at most 1 function in the selectitems list
        boolean noFunction = true;
        List<PlainSelect> outputAfterAggregator = new ArrayList<>();
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            if (selectItem instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                if (selectExpressionItem.getExpression() instanceof Function) {
                    // here we know the selectItem is a function (AVG, SUM, MAX etc.)
                    //      so we can start adding the rules for it
                    noFunction = false;
                    outputAfterAggregator.add(secondRule(plainSelect));
                    outputAfterAggregator.add(firstRule(plainSelect));
                    Function func = (Function) selectExpressionItem.getExpression();
                    outputAfterAggregator.add(thirdRule(plainSelect, func));
                    outputAfterAggregator.add(fourthRule(plainSelect, func));
                }
            }
        }
        if (noFunction) {
            outputAfterAggregator.add(plainSelect);
        }

        return outputAfterAggregator;
    }

    /** Creates the aggregator statement that checks for at least one entry
     *  having a certain column. Example result:
     *
     *  `SELECT COUNT(*) FROM Movies HAVING count(distinct Director)>1`
     *
     * @param plainSelect - select to add the part to
     * @return - select item in the above specified form
     */
    private PlainSelect firstRule(PlainSelect plainSelect) {
        // get a deep copy of the plainSelect
        PlainSelect plainSelectOut = deepCopy(plainSelect, false);

        // create COUNT(*) object
        Function count = getCountAllColumns();

        // create selectItem object with the count in it
        SelectItem si = getSelectItemWithObject(count);

        List<SelectItem> selectItemList = new ArrayList<>();
        selectItemList.add(si);

        // set selectItemList of plainSelectOut to be only the count object, overwriting the others
        plainSelectOut.setSelectItems(selectItemList);

        // get selectItem inside the Group By clause
        Expression groupBy = plainSelect.getGroupBy().getGroupByExpressions().get(0);

        // create COUNT(distinct groupByColumn) object
        Function countColumn = getCountDistinctColumn(groupBy, true);

        // Create count > 1
        GreaterThan greaterThan = getGreaterThan1(countColumn);

        // add to plainselect
        plainSelectOut.setHaving(greaterThan);

        // you may guess what this line does by yourself
        return plainSelectOut;
    }

    /** Adds "HAVING count(*)>1" to a plainSelect item.
     *  This is needed for handling aggregate operators.
     *
     * @param plainSelect - select to add the part to
     * @return - select item with the having part added
     */
    private PlainSelect secondRule(PlainSelect plainSelect) {
        PlainSelect plainSelectOut = deepCopy(plainSelect, true);

        // create COUNT(*) object
        Function count = getCountAllColumns();

        // Create COUNT(*) > 1
        GreaterThan greaterThan = getGreaterThan1(count);

        // add to plainselect
        plainSelectOut.setHaving(greaterThan);

        return plainSelectOut;
    }

    /** Generates the third rule for the aggregator.
     *
     * @param plainSelect - query object to generate the rule for
     * @param function - function object that resides in the query
     * @return - query object representing the third rule for the aggregator
     */
    private PlainSelect thirdRule(PlainSelect plainSelect, Function function) {
        PlainSelect plainSelectOut = deepCopy(plainSelect, true);

        // create COUNT(*) object
        Function count = getCountAllColumns();

        // retrieve column in function
        Expression expr = function.getParameters().getExpressions().get(0);

        // create count(*) > column in function
        GreaterThan leftGreaterThan = new GreaterThan();
        leftGreaterThan.setLeftExpression(count);
        leftGreaterThan.setRightExpression(getCountDistinctColumn(expr, false));

        // create count(distinct FunctionColumn) > 1
        GreaterThan rightGreaterThan = getGreaterThan1(getCountDistinctColumn(expr, true));

        // create AND
        BinaryExpression binaryExpression = new AndExpression(leftGreaterThan, rightGreaterThan);

        plainSelectOut.setHaving(binaryExpression);

        return plainSelectOut;
    }

    /** Generates the fourth rule for the aggregator.
     *
     * @param plainSelect - query object to generate the rule for
     * @param function - function object that resides in the query
     * @return - query object representing the fourth rule for the aggregator
     */
    private PlainSelect fourthRule(PlainSelect plainSelect, Function function) {
        PlainSelect plainSelectOut = deepCopy(plainSelect, true);

        // retrieve column in function
        Expression expr = function.getParameters().getExpressions().get(0);

        // create left condition
        GreaterThan leftGreaterThan = new GreaterThan();
        leftGreaterThan.setLeftExpression(getCountDistinctColumn(expr, false));
        leftGreaterThan.setRightExpression(getCountDistinctColumn(expr, true));

        // create right condition
        GreaterThan rightGreaterThan = getGreaterThan1(getCountDistinctColumn(expr, true));

        // create AND
        BinaryExpression binaryExpression = new AndExpression(leftGreaterThan, rightGreaterThan);

        plainSelectOut.setHaving(binaryExpression);

        return plainSelectOut;
    }

    /** Returns a deep copy of a plainSelect. The idea here is that you use this
     *  to get a copy of the object, then again add the attributes that you wanted
     *  to change in the first place.
     *
     * @param plainSelect - object to copy
     * @param copyGroupBy - boolean to determine whether or not you want to also include the
     *                    GroupBy clause in the deep copy
     * @return deep copy of object
     */
    private PlainSelect deepCopy(PlainSelect plainSelect, boolean copyGroupBy) {
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

    /** Generates a `__ > 1` expression.
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

    /** Generates a COUNT(*) object.
     *
     * @return a COUNT(*) object
     */
    private Function getCountAllColumns() {
        Function count = new Function();
        count.setName(countString);
        count.setAllColumns(true);

        return count;
    }

    /** Generates a COUNT(DISTINCT __) object.
     *
     * @param expression expression to fill in the __
     * @param distinct toggles whether or not you want to include DISTINCT
     * @return a COUNT(DISTINCT __) object
     */
    private Function getCountDistinctColumn(Expression expression, boolean distinct) {
        Function countColumn = new Function();
        countColumn.setName(countString);
        ExpressionList parameters = new ExpressionList(expression);
        countColumn.setParameters(parameters);
        countColumn.setDistinct(distinct);

        return countColumn;
    }

    /** Generates a SelectItem but with a certain expression as content.
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
}
