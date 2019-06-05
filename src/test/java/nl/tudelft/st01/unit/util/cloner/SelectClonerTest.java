package nl.tudelft.st01.unit.util.cloner;

import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import nl.tudelft.st01.util.cloner.SelectCloner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link SelectCloner} utility class.
 */
// Justification: Some objects have multiple object fields, which we want to verify are copied as well.
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class SelectClonerTest {

    private static final String STRING_ABC = "abc";

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code distinct} field.
     */
    @Test
    void testCopyPlainSelectDistinct() {

        PlainSelect original = new PlainSelect();

        Distinct distinct = new Distinct();
        original.setDistinct(distinct);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getDistinct()).isNotSameAs(distinct);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code selectItems} field.
     */
    @Test
    void testCopyPlainSelectSelectItems() {

        PlainSelect original = new PlainSelect();

        List<SelectItem> selectItems = new ArrayList<>(2);
        original.setSelectItems(selectItems);

        AllTableColumns allTableColumns = new AllTableColumns();
        selectItems.add(allTableColumns);

        AllColumns allColumns = new AllColumns();
        selectItems.add(allColumns);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getSelectItems().get(0)).isNotSameAs(allTableColumns);
        assertThat(copy.getSelectItems().get(0)).isNotSameAs(allColumns);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code intoTables} field. NOTE: {@link Table} objects are only copied by reference.
     */
    @Test
    void testCopyPlainSelectIntoTables() {

        PlainSelect original = new PlainSelect();

        List<Table> intoTables = new ArrayList<>(0);
        original.setIntoTables(intoTables);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getIntoTables()).isNotSameAs(intoTables);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code fromItem} field.
     */
    @Test
    void testCopyPlainSelectFromItem() {

        PlainSelect original = new PlainSelect();

        FromItem fromItem = new ValuesList();
        original.setFromItem(fromItem);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getFromItem()).isNotSameAs(fromItem);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code joins} field.
     */
    @Test
    void testCopyPlainSelectJoins() {

        PlainSelect original = new PlainSelect();

        List<Join> joins = new ArrayList<>();
        original.setJoins(joins);

        Join join0 = new Join();
        join0.setOuter(true);
        join0.setRight(true);
        join0.setLeft(true);
        join0.setRightItem(new Table());
        NullValue onExpression = new NullValue();
        join0.setOnExpression(onExpression);

        Join join1 = new Join();
        join1.setNatural(true);
        join1.setFull(true);
        join1.setInner(true);
        join1.setRightItem(new Table());
        ArrayList<Column> usingColumns = new ArrayList<>();
        Column column = new Column();
        usingColumns.add(column);
        join1.setUsingColumns(usingColumns);

        Join join2 = new Join();
        join2.setSimple(true);
        join2.setCross(true);
        join2.setSemi(true);
        join2.setRightItem(new Table());

        KSQLJoinWindow joinWindow = new KSQLJoinWindow();
        joinWindow.setBeforeAfterWindow(true);
        joinWindow.setDuration(1);
        joinWindow.setAfterDuration(0);
        joinWindow.setBeforeDuration(2);
        joinWindow.setTimeUnit(KSQLJoinWindow.TimeUnit.DAY);
        joinWindow.setAfterTimeUnit(KSQLJoinWindow.TimeUnit.MILLISECOND);
        joinWindow.setBeforeTimeUnit(KSQLJoinWindow.TimeUnit.HOURS);
        join2.setJoinWindow(joinWindow);

        joins.add(join0);
        joins.add(join1);
        joins.add(join2);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getJoins().get(0)).isNotSameAs(join0);
        assertThat(copy.getJoins().get(0).getOnExpression()).isNotSameAs(onExpression);
        assertThat(copy.getJoins().get(1)).isNotSameAs(join1);
        assertThat(copy.getJoins().get(1).getUsingColumns().get(0)).isNotSameAs(column);
        assertThat(copy.getJoins().get(2)).isNotSameAs(join2);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code where} field.
     */
    @Test
    void testCopyPlainSelectWhere() {

        PlainSelect original = new PlainSelect();

        NullValue where = new NullValue();
        original.setWhere(where);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getWhere()).isNotSameAs(where);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code groupBy} field.
     */
    @Test
    void testCopyPlainSelectGroupBy() {

        PlainSelect original = new PlainSelect();

        // TODO: group by

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code orderBy} field.
     */
    @Test
    void testCopyPlainSelectOrderBy() {

        PlainSelect original = new PlainSelect();

        List<OrderByElement> orderByElements = new ArrayList<>();
        original.setOrderByElements(orderByElements);

        OrderByElement orderBy = new OrderByElement();
        NullValue expression = new NullValue();
        orderBy.setExpression(expression);
        orderBy.setNullOrdering(OrderByElement.NullOrdering.NULLS_FIRST);
        orderBy.setAscDescPresent(true);

        orderByElements.add(orderBy);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getOrderByElements().get(0)).isNotSameAs(orderBy);
        assertThat(copy.getOrderByElements().get(0).getExpression()).isNotSameAs(expression);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code having} field.
     */
    @Test
    void testCopyPlainSelectHaving() {

        PlainSelect original = new PlainSelect();

        NullValue having = new NullValue();
        original.setHaving(having);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getHaving()).isNotSameAs(having);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code limit} field.
     */
    @Test
    void testCopyPlainSelectLimit() {

        PlainSelect original = new PlainSelect();

        Limit limit = new Limit();
        limit.setLimitNull(true);

        NullValue rowCount = new NullValue();
        limit.setRowCount(rowCount);

        NullValue offset = new NullValue();
        limit.setOffset(offset);

        original.setLimit(limit);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getLimit()).isNotSameAs(limit);
        assertThat(copy.getLimit().getRowCount()).isNotSameAs(rowCount);
        assertThat(copy.getLimit().getOffset()).isNotSameAs(offset);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code offset} field.
     */
    @Test
    void testCopyPlainSelectOffsetJdbcParam() {

        PlainSelect original = new PlainSelect();

        Offset offset = new Offset();
        offset.setOffset(1);
        offset.setOffsetParam(STRING_ABC);

        JdbcParameter jdbc = new JdbcParameter(1, true);
        offset.setOffsetJdbcParameter(jdbc);

        original.setOffset(offset);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getOffset()).isNotSameAs(offset);
        assertThat(copy.getOffset().getOffsetJdbcParameter()).isNotSameAs(jdbc);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code offset} field.
     */
    @Test
    void testCopyPlainSelectOffsetJdbcNamedParam() {

        PlainSelect original = new PlainSelect();

        Offset offset = new Offset();
        offset.setOffset(1);
        offset.setOffsetParam(STRING_ABC);

        JdbcNamedParameter jdbc = new JdbcNamedParameter(STRING_ABC);
        offset.setOffsetJdbcParameter(jdbc);

        original.setOffset(offset);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getOffset()).isNotSameAs(offset);
        assertThat(copy.getOffset().getOffsetJdbcParameter()).isNotSameAs(jdbc);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code offset} field.
     */
    @Test
    void testCopyPlainSelectOffsetNullJdbcParam() {

        PlainSelect original = new PlainSelect();

        Offset offset = new Offset();
        offset.setOffset(1);
        offset.setOffsetParam(STRING_ABC);

        original.setOffset(offset);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getOffset()).isNotSameAs(offset);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code fetch} field.
     */
    @Test
    void testCopyPlainSelectFetchJdbcParam() {

        PlainSelect original = new PlainSelect();

        Fetch fetch = new Fetch();
        fetch.setRowCount(1);
        fetch.setFetchParam(STRING_ABC);
        fetch.setFetchParamFirst(true);

        JdbcParameter jdbc = new JdbcParameter(1, true);
        fetch.setFetchJdbcParameter(jdbc);

        original.setFetch(fetch);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getFetch()).isNotSameAs(fetch);
        assertThat(copy.getFetch().getFetchJdbcParameter()).isNotSameAs(jdbc);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code fetch} field.
     */
    @Test
    void testCopyPlainSelectFetchNullJdbcParam() {

        PlainSelect original = new PlainSelect();

        Fetch fetch = new Fetch();
        fetch.setRowCount(1);
        fetch.setFetchParam(STRING_ABC);
        fetch.setFetchParamFirst(true);

        JdbcParameter jdbc = new JdbcParameter(1, true);
        fetch.setFetchJdbcParameter(jdbc);

        original.setFetch(fetch);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getFetch()).isNotSameAs(fetch);
        assertThat(copy.getFetch().getFetchJdbcParameter()).isNotSameAs(jdbc);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code optimizeFor} field.
     */
    @Test
    void testCopyPlainSelectOptimizeFor() {

        PlainSelect original = new PlainSelect();

        OptimizeFor optimizeFor = new OptimizeFor(1);
        original.setOptimizeFor(optimizeFor);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getOptimizeFor()).isNotSameAs(optimizeFor);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code skip} field.
     */
    @Test
    void testCopyPlainSelectSkipJdbcParam() {

        PlainSelect original = new PlainSelect();

        Skip skip = new Skip();
        skip.setRowCount(1L);
        skip.setVariable(STRING_ABC);

        JdbcParameter jdbcParameter = new JdbcParameter();
        skip.setJdbcParameter(jdbcParameter);

        original.setSkip(skip);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getSkip()).isNotSameAs(skip);
        assertThat(copy.getSkip().getJdbcParameter()).isNotSameAs(jdbcParameter);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code skip} field.
     */
    @Test
    void testCopyPlainSelectSkipNullJdbcParam() {

        PlainSelect original = new PlainSelect();

        Skip skip = new Skip();
        original.setSkip(skip);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getSkip()).isNotSameAs(skip);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link SetOperationList}.
     */
    @Test
    void testCopySetOperationList() {
        // TODO
        assertThat(true);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link WithItem}.
     */
    @Test
    void testCopyWithItem() {

        WithItem original = new WithItem();

        PlainSelect selectBody = new PlainSelect();
        original.setSelectBody(selectBody);

        List<SelectItem> withItemList = new ArrayList<>();
        AllTableColumns allTableColumns = new AllTableColumns();
        withItemList.add(allTableColumns);

        original.setWithItemList(withItemList);

        WithItem copy = (WithItem) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getSelectBody()).isNotSameAs(selectBody);
        assertThat(copy.getWithItemList()).isNotSameAs(withItemList);
        assertThat(copy.getWithItemList().get(0)).isNotSameAs(allTableColumns);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link ValuesStatement}.
     */
    @Test
    void testCopyValuesStatement() {
        // TODO
        assertThat(true);
    }

    // TODO: Tests for SelectItems

    // TODO: Tests for FromItems

    /**
     * Tests whether {@code copy} is equivalent to {@code original}.
     *
     * @param original the original {@code SelectBody}.
     * @param copy the copy of the original {@code SelectBody}.
     */
    private static void assertCopyEquals(SelectBody original, SelectBody copy) {
        assertThat(copy)
                .isNotSameAs(original)
                .hasSameClassAs(original)
                .isEqualToComparingFieldByFieldRecursively(original);
    }

}
