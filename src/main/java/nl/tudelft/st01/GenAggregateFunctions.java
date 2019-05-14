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

public class GenAggregateFunctions {

    public List<PlainSelect> generate(PlainSelect plainSelect) {
        // check if there is a Function in one of the columns
        // for now assuming there is at most 1 function in the selectitems list
        boolean noFunction = true;
        List<PlainSelect> outputAfterAggregator = new ArrayList<>();
        for(SelectItem selectItem : plainSelect.getSelectItems()) {
            if(selectItem instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                if(selectExpressionItem.getExpression() instanceof Function) {
                    // here we know the selectItem is a function (AVG, SUM, MAX etc.) so we can start adding the rules for it
                    noFunction = false;
                    outputAfterAggregator.add(secondRule(plainSelect));
                    outputAfterAggregator.add(firstRule(plainSelect));
                    outputAfterAggregator.add(thirdRule(plainSelect, (Function) selectExpressionItem.getExpression()));
                    outputAfterAggregator.add(fourthRule(plainSelect, (Function) selectExpressionItem.getExpression()));
                }
            }
        }
        if(noFunction) {
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

    /** Adds "HAVING count(*)>1" to a plainSelect item
     *  This is needed for handling aggregate operators
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
     * @return deep copy of object
     */
    private PlainSelect deepCopy(PlainSelect plainSelect, boolean copyGroupBy) {
        PlainSelect newPlainSelect = new PlainSelect();

        newPlainSelect.setSelectItems(plainSelect.getSelectItems());
        newPlainSelect.setFromItem(plainSelect.getFromItem());
        newPlainSelect.setHaving(plainSelect.getHaving());
        newPlainSelect.setWhere(plainSelect.getWhere());
        newPlainSelect.setJoins(plainSelect.getJoins());
        if(copyGroupBy) {
            newPlainSelect.setGroupByElement(plainSelect.getGroupBy());
        }

        return newPlainSelect;
    }

    private GreaterThan getGreaterThan1(Expression expr) {
        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(expr);
        greaterThan.setRightExpression(new DoubleValue("1"));

        return greaterThan;
    }

    private Function getCountAllColumns() {
        Function count = new Function();
        count.setName("COUNT");
        count.setAllColumns(true);

        return count;
    }

    private Function getCountDistinctColumn(Expression expression, boolean distinct) {
        Function countColumn = new Function();
        countColumn.setName("COUNT");
        ExpressionList parameters = new ExpressionList(expression);
        countColumn.setParameters(parameters);
        countColumn.setDistinct(distinct);

        return countColumn;
    }

    private SelectItem getSelectItemWithObject(Expression expression) {
        SelectExpressionItem selectExpressionItem = new SelectExpressionItem();
        selectExpressionItem.setExpression(expression);

        return selectExpressionItem;
    }
}
