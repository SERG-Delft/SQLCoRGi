package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Custom Visitor for SELECT statements.
 */
public class RuleGeneratorSelectVisitor extends SelectVisitorAdapter {

    private List<PlainSelect> outputAfterAggregator;
    private List<PlainSelect> output;

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void visit(PlainSelect plainSelect) {

        if (output == null || !output.isEmpty()) {
            throw new IllegalArgumentException(
                "To use this visitor, you must first give it an empty list so it can pass along the generated queries."
            );
        }

        // check if there is a Function in one of the columns
        // for now assuming there is at most 1 function in the selectitems list
        boolean noFunction = true;
        outputAfterAggregator = new ArrayList<>();
        for(SelectItem selectItem : plainSelect.getSelectItems()) {
            if(selectItem instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                if(selectExpressionItem.getExpression() instanceof Function) {
                    // here we know the selectItem is a function (AVG, SUM, MAX etc.) so we can start adding the rules for it
                    noFunction = false;
                    outputAfterAggregator.add(addCount(plainSelect));
                    outputAfterAggregator.add(atLeastOneEntry(plainSelect, (Function) selectExpressionItem.getExpression()));
                }
            }
        }
        if(noFunction) {
            outputAfterAggregator.add(plainSelect);
        }


        Expression where = plainSelect.getWhere();
        if (where != null) {
            RuleGeneratorExpressionVisitor ruleGeneratorExpressionVisitor = new RuleGeneratorExpressionVisitor();
            ArrayList<Expression> expressions = new ArrayList<>();
            ruleGeneratorExpressionVisitor.setOutput(expressions);
            where.accept(ruleGeneratorExpressionVisitor);

            for(PlainSelect plainSelectDupe : outputAfterAggregator) {
                for (Expression expression : expressions) {
                    PlainSelect plainSelectOut = deepCopy(plainSelectDupe);
                    plainSelectOut.setWhere(expression);

                    output.add(plainSelectOut);
                }
            }
        } else {
            // since there is no where, we don't need that part.
            // we do want the result of the output from the aggregator part,
            //      so we add those plainSelects to the output list
            for(PlainSelect p : outputAfterAggregator) {
                output.add(p);
            }
        }

        output = null;
    }

    public void setOutput(List<PlainSelect> output) {
        this.output = output;
    }

    /** Adds "HAVING count(*)>1" to a plainSelect item
     *  This is needed for handling aggregate operators
     *
     * @param plainSelect - select to add the part to
     * @return - select item with the having part added
     */
    public PlainSelect addCount(PlainSelect plainSelect) {
        PlainSelect plainSelectOut = deepCopy(plainSelect);

        // create COUNT(*) object
        Function count = new Function();
        count.setName("COUNT");
        count.setAllColumns(true);

        // Create COUNT(*) > 1
        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(count);
        greaterThan.setRightExpression(new DoubleValue("1"));

        // add to plainselect
        plainSelectOut.setHaving(greaterThan);

        return plainSelectOut;
    }

    /** Creates the aggregator statement that checks for at least one entry
     *  having a certain column. Example result:
     *
     *  `SELECT COUNT(*) FROM Movies HAVING count(distinct Director)>1`
     *
     * @param plainSelect - select to add the part to
     * @return - select item in the above specified form
     */
    public PlainSelect atLeastOneEntry(PlainSelect plainSelect, Function function) {
        PlainSelect plainSelectOut = deepCopy(plainSelect);

        // create COUNT(*) object
        Function count = new Function();
        count.setName("COUNT");
        count.setAllColumns(true);

        // create selectItem object with the count in it
        SelectExpressionItem selectExpressionItem = new SelectExpressionItem();
        selectExpressionItem.setExpression(count);
        SelectItem si = (SelectItem) selectExpressionItem;

        List<SelectItem> selectItemList = new ArrayList<>();
        selectItemList.add(si);

        // set selectItems to be only the count object
        plainSelectOut.setSelectItems(selectItemList);

        // get Group by selectItem
        Expression groupBy = plainSelect.getGroupBy().getGroupByExpressions().get(0);

        // create COUNT(distinct groupByColumn) object
        Function countColumn = new Function();
        countColumn.setName("COUNT");
        ExpressionList parameters = new ExpressionList(groupBy);
        countColumn.setParameters(parameters);
        countColumn.setDistinct(true);

        // Create count > 1
        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(countColumn);
        greaterThan.setRightExpression(new DoubleValue("1"));

        // add to plainselect
        plainSelectOut.setHaving(greaterThan);

        return plainSelectOut;
    }

    /** Returns a deep copy of a plainSelect. The idea here is that you use this
     *  to get a copy of the object, then again add the attributes that you wanted
     *  to change in the first place.
     *
     * @param plainSelect - object to copy
     * @return deep copy of object
     */
    public PlainSelect deepCopy(PlainSelect plainSelect) {
        PlainSelect newPlainSelect = new PlainSelect();
        newPlainSelect.setSelectItems(plainSelect.getSelectItems());
        newPlainSelect.setFromItem(plainSelect.getFromItem());
        newPlainSelect.setGroupByElement(plainSelect.getGroupBy());
        newPlainSelect.setHaving(plainSelect.getHaving());
        newPlainSelect.setWhere(plainSelect.getWhere());
        newPlainSelect.setJoins(plainSelect.getJoins());

        return newPlainSelect;
    }
}
