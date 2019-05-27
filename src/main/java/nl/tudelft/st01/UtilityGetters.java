package nl.tudelft.st01;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * Utility class that can be used to quickly create statements with COUNT(*), COUNT(DISTINCT __).
 */
public final class UtilityGetters {

    private static final String COUNT_STRING = "COUNT";

    /**
     * No instance of this class should be created.
     */
    private UtilityGetters() {
        throw new UnsupportedOperationException();
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
    // TODO possibly remove this deepCopy method
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

    /**
     * Generates a `__ > 1` expression.
     *
     * @param expr - expression to fill in the __
     * @return `expr > 1` object
     */
    public static GreaterThan getGreaterThan1(Expression expr) {
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
    public static Function getCountAllColumns() {
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
    public static Function getCountColumn(Expression expression, boolean distinct) {
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
    public static SelectItem getSelectItemWithObject(Expression expression) {
        SelectExpressionItem selectExpressionItem = new SelectExpressionItem();
        selectExpressionItem.setExpression(expression);

        return selectExpressionItem;
    }

}
