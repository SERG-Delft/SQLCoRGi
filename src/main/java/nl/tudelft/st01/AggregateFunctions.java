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
public class AggregateFunctions {

    /** Main, public method that generates the rules for the aggregate functions.
     *
     * @param plainSelect - query object to generate rules for
     * @return list of query objects which represent the rules for the aggregator function
     */
    public List<PlainSelect> generate(PlainSelect plainSelect) {
        // Check if there is a Function in one of the columns. If so, generate rules for it.
        boolean noFunction = true;
        List<PlainSelect> outputAfterAggregator = new ArrayList<>();
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            if (selectItem instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                if (selectExpressionItem.getExpression() instanceof Function) {
                    // Here we know the selectItem is a function (AVG, SUM, MAX etc.)
                    //      so we can start adding the rules for it.
                    noFunction = false;
                    outputAfterAggregator.add(firstRule(plainSelect));
                    outputAfterAggregator.add(secondRule(plainSelect));
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
        PlainSelect plainSelectOut = UtilityGetters.deepCopy(plainSelect, false);

        // Create COUNT(*) object
        Function count = UtilityGetters.getCountAllColumns();

        // Create selectItem object with the count in it
        SelectItem si = UtilityGetters.getSelectItemWithObject(count);

        List<SelectItem> selectItemList = new ArrayList<>();
        selectItemList.add(si);

        // Set selectItemList of plainSelectOut to be only the count object, overwriting the others
        plainSelectOut.setSelectItems(selectItemList);

        // Get selectItem inside the Group By clause
        Expression groupBy = plainSelect.getGroupBy().getGroupByExpressions().get(0);

        // Create COUNT(distinct groupByColumn) object
        Function countColumn = UtilityGetters.getCountDistinctColumn(groupBy, true);

        // Create count > 1
        GreaterThan greaterThan = UtilityGetters.getGreaterThan1(countColumn);

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
        PlainSelect plainSelectOut = UtilityGetters.deepCopy(plainSelect, true);

        // Create COUNT(*) object
        Function count = UtilityGetters.getCountAllColumns();

        // Create COUNT(*) > 1
        GreaterThan greaterThan = UtilityGetters.getGreaterThan1(count);

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
        PlainSelect plainSelectOut = UtilityGetters.deepCopy(plainSelect, true);

        // Create COUNT(*) object
        Function count = UtilityGetters.getCountAllColumns();

        // Retrieve column in function
        Expression expr = function.getParameters().getExpressions().get(0);

        // Create count(*) > column in function
        GreaterThan leftGreaterThan = new GreaterThan();
        leftGreaterThan.setLeftExpression(count);
        leftGreaterThan.setRightExpression(UtilityGetters.getCountDistinctColumn(expr, false));

        // Create count(distinct FunctionColumn) > 1
        GreaterThan rightGreaterThan = UtilityGetters.getGreaterThan1(UtilityGetters.getCountDistinctColumn(expr, true));

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
        PlainSelect plainSelectOut = UtilityGetters.deepCopy(plainSelect, true);

        // Retrieve column in function
        Expression expr = function.getParameters().getExpressions().get(0);

        // Create left condition
        GreaterThan leftGreaterThan = new GreaterThan();
        leftGreaterThan.setLeftExpression(UtilityGetters.getCountDistinctColumn(expr, false));
        leftGreaterThan.setRightExpression(UtilityGetters.getCountDistinctColumn(expr, true));

        // Create right condition
        GreaterThan rightGreaterThan = UtilityGetters.getGreaterThan1(UtilityGetters.getCountDistinctColumn(expr, true));

        // Create AND
        BinaryExpression binaryExpression = new AndExpression(leftGreaterThan, rightGreaterThan);

        plainSelectOut.setHaving(binaryExpression);

        return plainSelectOut;
    }
}
