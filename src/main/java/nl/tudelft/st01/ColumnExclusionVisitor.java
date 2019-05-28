package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

import java.util.List;
import java.util.Set;

/**
 * This class allows modifying an expression such that none of the provided columns,
 * or columns related to the provided tables, are in the expression anymore.
 */
public class ColumnExclusionVisitor extends ExpressionVisitorAdapter {
    private List<Column> nullColumns;

    private Set<String> tables;

    private Expression expression;

    @Override
    public void visit(AndExpression andExpression) {
        AndExpression and = new AndExpression(null, null);
        andExpression.getLeftExpression().accept(this);
        handleExpression(andExpression, and);
    }

    @Override
    public void visit(OrExpression orExpression) {
        OrExpression or = new OrExpression(null, null);
        handleExpression(orExpression, or);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        EqualsTo eq = new EqualsTo();
        handleExpression(equalsTo, eq);

    }

    @Override
    public void visit(GreaterThan greaterThan) {
        handleExpression(greaterThan, new GreaterThan());
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        handleExpression(greaterThanEquals, new GreaterThanEquals());
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        /*  If the column is in the null columns list, then either
            a) It is an IS NULL expression, leading to a duplicate.
            b) It is an IS NOT NULL expression, leading to a contradiction.
            Both must be excluded.
            If the column is not in the null columns list, then the column must not be null.
            If it is an IS NULL expression, then there is a contradiction, so it should be excluded.
         */
        if (contains((Column) isNullExpression.getLeftExpression())) {
            expression = null;
        } else if (!isNullExpression.isNot()) {
            expression = null;
        } else {
            expression = isNullExpression;
        }
    }

    @Override
    public void visit(MinorThan minorThan) {
        handleExpression(minorThan, new MinorThan());
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        handleExpression(minorThanEquals, new MinorThanEquals());
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        handleExpression(notEqualsTo, new NotEqualsTo());
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        expression = doubleValue;
    }

    @Override
    public void visit(LongValue longValue) {
        expression = longValue;
    }

    @Override
    public void visit(Column column) {
        if (contains(column)) {
            expression = null;
        } else {
            expression = column;
        }
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    public void setNullColumns(List<Column> nullColumns) {
        this.nullColumns = nullColumns;
    }

    /**
     * Set the tables of this instance.
     * If the tables are not initialized yet, it will be.
     * Otherwise the tables are added to the set.
     * @param tables The tables to be set.
     */
    public void setTables(Set<String> tables) {
        if (this.tables != null) {
            this.tables.clear();
            for (String table : tables) {
                this.tables.add(table.toLowerCase());
            }
        } else {
            this.tables = tables;
        }

    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    /**
     * Check whether the columns list contain the given column or if the column belongs to the table.
     * @param column The column that should be checked.
     * @return True if the column is in the list or if it belongs to the table.
     */
    private boolean contains(Column column) {
        if (nullColumns != null && !nullColumns.isEmpty()) {
            for (Column c : nullColumns) {
                if (c.toString().toLowerCase().equals(column.toString().toLowerCase())) {
                    return true;
                }
            }
        }

        if (tables != null && !tables.isEmpty()) {
            return tables.contains(column.getTable().toString().toLowerCase());
        }

        return false;
    }

    /**
     * In case of a binary expression, both sides should be traversed and modified if needed.
     * @param binaryExpression The expression to handle.
     * @param seedExpression A new instance of the same class as from where the method is called.
     *                       This acts as a seed from which the modified expression will be created.
     */
    private void handleExpression(BinaryExpression binaryExpression, BinaryExpression seedExpression) {
        binaryExpression.getLeftExpression().accept(this);
        Expression left = expression;
        if (left != null) {
            seedExpression.setLeftExpression(left);

            binaryExpression.getRightExpression().accept(this);
            Expression right = expression;

            if (right != null) {
                seedExpression.setRightExpression(right);
                expression = seedExpression;
            } else {
                expression = left;
            }
        } else {
            binaryExpression.getRightExpression().accept(this);
        }
    }

    private void handleExpression(ComparisonOperator comparisonOperator, ComparisonOperator seedExpression) {
        comparisonOperator.getLeftExpression().accept(this);
        Expression left = expression;

        if (left != null) {
            seedExpression.setLeftExpression(left);

            comparisonOperator.getRightExpression().accept(this);
            Expression right = expression;

            if (right != null) {
                seedExpression.setRightExpression(right);
                expression = seedExpression;
            }
        } else {
            expression = null;
        }
    }

    /**
     * In case of a comparison operator, the original expression may only be retained if neither side contains
     * columns that should be excluded.
     * @param comparisonOperator The comparison operator to evaluate.
     */
    private void handleComparisonOperator(ComparisonOperator comparisonOperator) {
        Expression left = comparisonOperator.getLeftExpression();
        Expression right = comparisonOperator.getRightExpression();

        if (expressionContainsExcludedColumn(left) || expressionContainsExcludedColumn(right)) {
            expression = null;
        } else {
            expression = comparisonOperator;
        }


    }

    /**
     * Checks if the expression contains a column that should be excluded.
     * @param expression The expression to evaluate.
     * @return If the expression contains a column that should be excluded, true is returned. False otherwise.
     */
    private boolean expressionContainsExcludedColumn(Expression expression) {
        expression.accept(this);

        return this.expression == null;
    }



}
