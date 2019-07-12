package com.github.sergdelft.sqlcrg.unit.visitors.join;

import com.github.sergdelft.sqlcrg.visitors.join.OnExpressionVisitor;
import manifold.ext.api.Jailbreak;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Unit tests for the {@link OnExpressionVisitorTest}.
 */
public class OnExpressionVisitorTest {

    private static final String TABLE_1 = "table1";
    private static final String TABLE_2 = "table2";
    private static final String TABLE_3 = "table3";

    private static final String COLUMN_1 = "column1";
    private static final String COLUMN_2 = "column2";
    private static final String COLUMN_3 = "column3";
    private static final String COLUMN_4 = "column4";
    private static final String COLUMN_5 = "column5";
    private static final String COLUMN_6 = "column6";

    private final Table tab1 = new Table(TABLE_1);
    private final Table tab2 = new Table(TABLE_2);
    private final Table tab3 = new Table(TABLE_3);

    private final Column col1 = new Column(tab1, COLUMN_1);
    private final Column col2 = new Column(tab1, COLUMN_2);
    private final Column col3 = new Column(tab2, COLUMN_3);
    private final Column col4 = new Column(tab2, COLUMN_4);

    private List<Column> listTab1 = new ArrayList<>();
    private List<Column> listTab2 = new ArrayList<>();

    private Map<String, List<Column>> output = new HashMap<>();

    /**
     * Set-up a "database scheme" ({@link HashMap}) with 2 tables, each with 2 columns.
     */
    @BeforeEach
    public void setUpMap() {
        Collections.addAll(listTab1, col1, col2);
        Collections.addAll(listTab2, col3, col4);

        output.put(tab1.toString(), listTab1);
        output.put(tab2.toString(), listTab2);
    }

    /**
     * Assert that the {@code updateColumnList} method adds a new column to the correct table in the "scheme" when the
     * table itself is already in there.
     */
    @Test
    public void testUpdateColumnListNewColumnFromExistingTable() {
        @Jailbreak OnExpressionVisitor onExpressionVisitor = new OnExpressionVisitor(output);

        List<Column> updatedListTab1 = new ArrayList<>();
        Column col5 = new Column(tab1, COLUMN_5);
        Collections.addAll(updatedListTab1, col1, col2, col5);

        onExpressionVisitor.updateColumnList(col5);

        assertThat(output).containsOnly(
                entry(TABLE_1, updatedListTab1),
                entry(TABLE_2, listTab2)
        );
    }

    /**
     * Assert that the {@code updateColumnList} method adds a new column to the correct table in the "scheme" when the
     * table itself is not yet in there.
     */
    @Test
    public void testUpdateColumnListNewColumnFromNewTable() {
        @Jailbreak OnExpressionVisitor onExpressionVisitor = new OnExpressionVisitor(output);

        List<Column> listTab3 = new ArrayList<>();
        Column col6 = new Column(tab3, COLUMN_6);
        listTab3.add(col6);

        onExpressionVisitor.updateColumnList(col6);

        assertThat(output).containsOnly(
                entry(TABLE_1, listTab1),
                entry(TABLE_2, listTab2),
                entry(TABLE_3, listTab3)
        );
    }

    /**
     * Assert that the {@code updateColumnList} method adds a new column to the correct table in the "scheme" when the
     * new column's name is the same as a column name in another table.
     */
    @Test
    public void testUpdateColumnListSameColumnNameOnDifferentTables() {
        @Jailbreak OnExpressionVisitor onExpressionVisitor = new OnExpressionVisitor(output);

        List<Column> updatedListTab2 = new ArrayList<>();
        Column col1 = new Column(tab2, COLUMN_1);
        Collections.addAll(updatedListTab2, col3, col4, col1);

        onExpressionVisitor.updateColumnList(col1);

        assertThat(output).containsOnly(
                entry(TABLE_1, listTab1),
                entry(TABLE_2, updatedListTab2)
        );
    }

    /**
     * Test that the {@code contains} method returns false when the list of columns to look through is {@code null}.
     */
    @Test
    public void testContainsNullListShouldReturnFalse() {
        @Jailbreak OnExpressionVisitor onExpressionVisitor = null;

        boolean result = onExpressionVisitor.contains(null, new Column());

        assertThat(result).isFalse();
    }

    /**
     * Test that the {@code contains} method returns true when a column is in the map.
     */
    @Test
    public void testContainsColumnIsInListShouldReturnTrue() {
        @Jailbreak OnExpressionVisitor onExpressionVisitor = null;

        boolean result = onExpressionVisitor.contains(listTab1, col2);

        assertThat(result).isTrue();
    }

    /**
     * Test that the {@code contains} method returns false when a column is not in the map.
     */
    @Test
    public void testContainsColumnIsNotInListShouldReturnFalse() {
        @Jailbreak OnExpressionVisitor onExpressionVisitor = null;

        boolean result = onExpressionVisitor.contains(listTab1, col3);

        assertThat(result).isFalse();
    }
}
