package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import nl.tudelft.st01.util.AggregateComponentFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.tudelft.st01.util.cloner.SelectCloner.copy;

/**
 * Class that generates rules based on the GROUP BY clause.
 */
public class GroupByGenerator {

    /**
     * Main method that generates the rules for the GROUP BY clause.
     *
     * @param plainSelect - query object to generate rules for
     * @return list of query objects which represent the rules for the GROUP BY clause
     */
    public Set<String> generate(PlainSelect plainSelect) {
        Set<String> outputWithGroupBy = new HashSet<>(2);
        outputWithGroupBy.add(firstRule(plainSelect).toString());
        outputWithGroupBy.add(secondRule(plainSelect).toString());

        return outputWithGroupBy;
    }

    /**
     * Adds {@code HAVING count(*) > 1} to a plainSelect item.
     *
     * @param plainSelect - select to add the part to
     * @return - select item with the having part added
     */
    private PlainSelect firstRule(PlainSelect plainSelect) {
        PlainSelect plainSelectOut = (PlainSelect) copy(plainSelect);

        Function count = AggregateComponentFactory.createCountAllColumns();

        GreaterThan greaterThan1 = AggregateComponentFactory.createGreaterThanOne(count);

        Expression having = plainSelect.getHaving();
        if (having != null) {
            plainSelectOut.setHaving(new AndExpression(greaterThan1, having));
        } else {
            plainSelectOut.setHaving(greaterThan1);
        }

        return plainSelectOut;
    }

    /**
     * Creates the aggregator statement that checks for at least one entry
     * having a certain column. Example result:
     *
     * `SELECT COUNT(*) FROM Movies HAVING count(distinct Director) > 1`
     *
     * @param plainSelect - select to add the part to
     * @return - select item in the above specified form
     */
    private PlainSelect secondRule(PlainSelect plainSelect) {

        PlainSelect plainSelectOut = (PlainSelect) copy(plainSelect);
        plainSelectOut.setGroupByElement(null);

        Function count = AggregateComponentFactory.createCountAllColumns();

        List<SelectItem> selectItemList = new ArrayList<>();

        SelectExpressionItem selectExpressionItem = new SelectExpressionItem(count);

        selectItemList.add(selectExpressionItem);

        plainSelectOut.setSelectItems(selectItemList);

        Expression groupBy = plainSelect.getGroupBy().getGroupByExpressions().get(0);

        Function countColumn = AggregateComponentFactory.createCountColumn(groupBy, true);

        GreaterThan greaterThan1 = AggregateComponentFactory.createGreaterThanOne(countColumn);

        Expression having = plainSelect.getHaving();
        if (having != null) {
            plainSelectOut.setHaving(new AndExpression(greaterThan1, having));
        } else {
            plainSelectOut.setHaving(greaterThan1);
        }

        return plainSelectOut;
    }

}
