package nl.tudelft.st01;

import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class allows for storing the columns corresponding to the left or table related to a given join.
 */
public class JoinOnConditionColumns {
    private List<Column> leftColumns;

    private Set<String> leftTables = new HashSet<>();

    private Set<String> rightTables = new HashSet<>();

    private List<Column> rightColumns;

    /**
     * Constructor for this class. Initializes left and right columns lists.
     */
    public JoinOnConditionColumns() {
        leftColumns = new ArrayList<>();
        rightColumns = new ArrayList<>();
    }

    public List<Column> getLeftColumns() {
        return leftColumns;
    }

    public List<Column> getRightColumns() {
        return rightColumns;
    }

    public Set<String> getLeftTables() {
        return leftTables;
    }

    public Set<String> getRightTables() {
        return rightTables;
    }

    /**
     * Add the provided columns to the left columns list.
     * @param columns Columns to add.
     */
    public void addToLeftColumns(List<Column> columns) {
        if (columns != null) {
            if (leftColumns != null) {
                leftColumns.addAll(columns);
            } else {
                leftColumns = columns;
            }
        }

        updateTables(leftColumns, leftTables);
    }

    /**
     * Add the provided columns to the right columns list.
     * @param columns Columns to add.
     */
    public void addToRightColumns(List<Column> columns) {
        if (columns != null) {
            if (rightColumns != null) {
                rightColumns.addAll(columns);
            } else {
                rightColumns = columns;
            }
        }

        updateTables(rightColumns, rightTables);
    }

    /**
     * Adds the table corresponding to any of the columns provided.
     * @param columns The columns from which the table should be added.
     * @param tables The tables to which the table should be added.
     */
    private void updateTables(List<Column> columns, Set<String> tables) {
        for (Column column : columns) {
            if (column.getTable() != null) {
                tables.add(column.getTable().toString().toLowerCase());
            }
        }
    }
    //    @Override
    //    public String toString() {
    //        return "<JOCC\n\t<LEFT:\n\t\t<COLUMNS\t" + leftColumns + ">\n\t\t<TABLES:\t"
    //                  + leftTables + ">>\n\t<RIGHT:\n\t\t<COLUMNS\t"
    //                  + rightColumns + ">\n\t\t<TABLES:\t" + rightTables + ">>";
    //    }

}
