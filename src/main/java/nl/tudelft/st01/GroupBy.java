package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that generates rules based on the GROUP BY clause.
 */
public class GroupBy {

    /**
     * Main, public method that generates the rules for the GROUP BY clause.
     *
     * @param plainSelect - query object to generate rules for
     * @return list of query objects which represent the rules for the GROUP BY clause
     */
    public List<PlainSelect> generate(PlainSelect plainSelect) {
        List<PlainSelect> outputWithGroupBy = new ArrayList<>();
        outputWithGroupBy.add(firstRule(plainSelect));
        outputWithGroupBy.add(secondRule(plainSelect));

        return outputWithGroupBy;
    }

    /**
     * Adds "HAVING count(*)>1" to a plainSelect item.
     *
     * @param plainSelect - select to add the part to
     * @return - select item with the having part added
     */
    private PlainSelect firstRule(PlainSelect plainSelect) {
        PlainSelect plainSelectOut = UtilityGetters.deepCopy(plainSelect, true);

        // Create COUNT(*) object
        Function count = UtilityGetters.getCountAllColumns();

        // Create COUNT(*) > 1
        GreaterThan greaterThan1 = UtilityGetters.getGreaterThan1(count);

        // Add to plainselect
        plainSelectOut.setHaving(greaterThan1);

        return plainSelectOut;
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
    private PlainSelect secondRule(PlainSelect plainSelect) {
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
        GreaterThan greaterThan1 = UtilityGetters.getGreaterThan1(countColumn);

        // Add to plainselect
        plainSelectOut.setHaving(greaterThan1);

        return plainSelectOut;
    }

}
