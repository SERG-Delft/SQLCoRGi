package nl.tudelft.st01.unit.util.cloner;

import net.sf.jsqlparser.expression.*;
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
     * {@code limit} field.
     */
    @Test
    void testCopyPlainSelectLimitNullExpr() {

        PlainSelect original = new PlainSelect();

        Limit limit = new Limit();
        limit.setLimitNull(false);

        original.setLimit(limit);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getLimit()).isNotSameAs(limit);
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
        fetch.setRowCount(0);
        fetch.setFetchParam(STRING_ABC);
        fetch.setFetchParamFirst(false);

        original.setFetch(fetch);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getFetch()).isNotSameAs(fetch);
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
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code first} field.
     */
    @Test
    void testCopyPlainSelectFirstJdbcParam() {

        PlainSelect original = new PlainSelect();

        First first = new First();
        first.setKeyword(First.Keyword.FIRST);
        first.setRowCount(1L);
        first.setVariable(STRING_ABC);

        JdbcParameter jdbcParameter = new JdbcParameter();
        first.setJdbcParameter(jdbcParameter);

        original.setFirst(first);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getFirst()).isNotSameAs(first);
        assertThat(copy.getFirst().getJdbcParameter()).isNotSameAs(jdbcParameter);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code first} field.
     */
    @Test
    void testCopyPlainSelectFirstNullJdbcParam() {

        PlainSelect original = new PlainSelect();

        First first = new First();
        first.setKeyword(First.Keyword.FIRST);
        first.setRowCount(1L);
        first.setVariable(STRING_ABC);

        original.setFirst(first);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getFirst()).isNotSameAs(first);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code top} field.
     */
    @Test
    void testCopyPlainSelectTop() {

        PlainSelect original = new PlainSelect();

        Top top = new Top();
        top.setPercentage(true);
        top.setParenthesis(true);

        NullValue expression = new NullValue();
        top.setExpression(expression);

        original.setTop(top);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getTop()).isNotSameAs(top);
        assertThat(copy.getTop().getExpression()).isNotSameAs(expression);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code top} field.
     */
    @Test
    void testCopyPlainSelectTopNullExpr() {

        PlainSelect original = new PlainSelect();

        Top top = new Top();
        top.setPercentage(true);
        top.setParenthesis(true);

        original.setTop(top);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getTop()).isNotSameAs(top);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code oracleHierarchical} field.
     */
    @Test
    void testCopyPlainSelectOracleHierarchical() {

        PlainSelect original = new PlainSelect();

        OracleHierarchicalExpression oracleHierarchical = new OracleHierarchicalExpression();
        oracleHierarchical.setNoCycle(true);
        oracleHierarchical.setConnectFirst(true);

        NullValue startExpression = new NullValue();
        oracleHierarchical.setStartExpression(startExpression);

        NullValue connectExpression = new NullValue();
        oracleHierarchical.setConnectExpression(connectExpression);

        original.setOracleHierarchical(oracleHierarchical);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getOracleHierarchical()).isNotSameAs(oracleHierarchical);
        assertThat(copy.getOracleHierarchical().getStartExpression()).isNotSameAs(startExpression);
        assertThat(copy.getOracleHierarchical().getConnectExpression()).isNotSameAs(connectExpression);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code oracleHint} field.
     */
    @Test
    void testCopyPlainSelectOracleHint() {

        PlainSelect original = new PlainSelect();

        OracleHint oracleHint = new OracleHint();
        oracleHint.setValue(STRING_ABC);
        oracleHint.setSingleLine(true);
        oracleHint.setComment(STRING_ABC);

        original.setOracleHint(oracleHint);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getOracleHint()).isNotSameAs(oracleHint);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Focuses on its
     * {@code wait} field.
     */
    @Test
    void testCopyPlainSelectWait() {

        PlainSelect original = new PlainSelect();

        Wait wait = new Wait();
        wait.setTimeout(1);

        original.setWait(wait);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getWait()).isNotSameAs(wait);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}. Checks all
     * simple fields.
     */
    @Test
    void testCopyPlainSelect() {

        PlainSelect original = new PlainSelect();

        original.setOracleSiblings(true);
        original.setForUpdate(true);
        original.setForUpdateTable(new Table());
        original.setUseBrackets(true);
        original.setMySqlSqlCalcFoundRows(true);
        original.setMySqlSqlNoCache(true);
        original.setForXmlPath(STRING_ABC);

        PlainSelect copy = (PlainSelect) SelectCloner.copy(original);
        assertCopyEquals(original, copy);
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
        original.setRecursive(true);

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

        ArrayList<Expression> expressions = new ArrayList<>();

        NullValue nullValue = new NullValue();
        expressions.add(nullValue);

        LongValue longValue = new LongValue(1);
        expressions.add(longValue);

        ValuesStatement original = new ValuesStatement(expressions);

        ValuesStatement copy = (ValuesStatement) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getExpressions().get(0)).isNotSameAs(nullValue);
        assertThat(copy.getExpressions().get(1)).isNotSameAs(longValue);
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
