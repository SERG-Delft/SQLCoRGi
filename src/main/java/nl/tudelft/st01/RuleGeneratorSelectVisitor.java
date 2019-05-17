package nl.tudelft.st01;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Visitor for SELECT statements.
 */
public class RuleGeneratorSelectVisitor extends SelectVisitorAdapter {
    private static final String COUNT_STRING = "COUNT";

    private List<PlainSelect> output;

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void visit(PlainSelect plainSelect) {

        if (output == null || !output.isEmpty()) {
            throw new IllegalArgumentException(
                "To use this visitor, you must first give it an empty list so it can pass along the generated queries."
            );
        }

        GenAggregateFunctions genAggregateFunctions = new GenAggregateFunctions();
        List<PlainSelect> outputAfterAggregator = genAggregateFunctions.generate(plainSelect);

        Expression where = plainSelect.getWhere();
        GroupByElement groupBy = plainSelect.getGroupBy();

        ArrayList<Expression> whereExpressions = new ArrayList<>();

        if (where != null || groupBy != null) {
            if (where != null) {
                RuleGeneratorExpressionVisitor ruleGeneratorExpressionVisitor = new RuleGeneratorExpressionVisitor();
                ruleGeneratorExpressionVisitor.setOutput(whereExpressions);
                where.accept(ruleGeneratorExpressionVisitor);

                for (PlainSelect plainSelectAfterAggregator : outputAfterAggregator) {
                    for (Expression whereExpression : whereExpressions) {
                        PlainSelect plainSelectOut = GenAggregateFunctions.deepCopy(plainSelectAfterAggregator, true);
                        plainSelectOut.setWhere(whereExpression);

                        output.add(plainSelectOut);
                    }
                }
            }
            // Only add output from aggregation functions when it is has more than 1 element, otherwise it will interfere with GROUP BY
            else if (outputAfterAggregator.size() > 1) {
                for (PlainSelect plainSelectAfterAggregator : outputAfterAggregator) {
                    PlainSelect plainSelectOut = GenAggregateFunctions.deepCopy(plainSelectAfterAggregator, true);

                    output.add(plainSelectOut);
                }
            }

            if (groupBy != null) {
                output.add(firstRule(plainSelect));
                output.add(secondRule(plainSelect));
            }
        } else {
            // Since there is no where, we don't need that part.
            // We do want the result of the output from the aggregator part,
            //      so we add those plainSelects to the output list
            output.addAll(outputAfterAggregator);
        }

        output = null;
    }

    public void setOutput(List<PlainSelect> output) {
        this.output = output;
    }

    /**
     * Adds "HAVING count(*)>1" to a plainSelect item.
     *
     * @param plainSelect - select to add the part to
     * @return - select item with the having part added
     */
    private PlainSelect firstRule(PlainSelect plainSelect) {
        PlainSelect plainSelectOut = deepCopy(plainSelect, true);

        // Create COUNT(*) object
        Function count = getCountAllColumns();

        // Create COUNT(*) > 1
        GreaterThan greaterThan1 = getGreaterThan1(count);

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
        GreaterThan greaterThan1 = getGreaterThan1(countColumn);

        // Add to plainselect
        plainSelectOut.setHaving(greaterThan1);

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
