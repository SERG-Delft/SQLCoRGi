package nl.tudelft.st01;

import net.sf.jsqlparser.schema.Column;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JoinOnConditionColumns {
    private List<Column> leftColumns;

    private Set<String> leftTables = new HashSet<>();

    private Set<String> rightTables = new HashSet<>();

    private List<Column> rightColumns;

    public JoinOnConditionColumns(List<Column> leftColumns, List<Column> rightColumns) {
        this.leftColumns = leftColumns;
        this.rightColumns = rightColumns;
    }

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

    public void setLeftColumns(List<Column> leftColumns) {
        this.leftColumns = leftColumns;
        leftTables.clear();
        updateTables(leftColumns, leftTables);
    }

    public void setRightColumns(List<Column> rightColumns) {
        this.rightColumns = rightColumns;
        rightTables.clear();
        updateTables(rightColumns, rightTables);
    }

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

    private void updateTables(List<Column> columns, Set<String> tables) {
        for (Column column : columns) {
            if (column.getTable() != null) {
                tables.add(column.getTable().toString().toLowerCase());
            }
        }
    }
    @Override
    public String toString() {
        return "<JOCC\n\t<LEFT:\n\t\t<COLUMNS\t" + leftColumns + ">\n\t\t<TABLES:\t" + leftTables + ">>\n\t<RIGHT:\n\t\t<COLUMNS\t"
                + rightColumns + ">\n\t\t<TABLES:\t" + rightTables + ">>";
    }

}
