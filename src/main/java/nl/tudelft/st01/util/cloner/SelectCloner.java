package nl.tudelft.st01.util.cloner;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * This visitor can be used to create a deep copy of a {@link SelectBody}. Any {@link Table}s that are encountered will
 * not have a deep copy.
 */
public class SelectCloner implements SelectVisitor, SelectItemVisitor, FromItemVisitor {

    private SelectBody copy;
    private SelectItem selectItem;
    private FromItem fromItem;

    private ExpressionCloner expressionCloner;

    /**
     * Creates a new instance of this class, which uses a new {@link ExpressionCloner} for {@link Expression}s.
     */
    private SelectCloner() {
        this.expressionCloner = new ExpressionCloner(this);
    }

    /**
     * Creates a new instance of this class, which uses a new {@link ExpressionCloner} for {@link Expression}s.
     *
     * @param expressionCloner the {@code ExpressionCloner} to use.
     */
    SelectCloner(ExpressionCloner expressionCloner) {
        this.expressionCloner = expressionCloner;
    }

    /**
     * Creates a deep copy of a {@link SelectBody}.
     *
     * @param selectBody the select body that needs to be copied.
     * @return a copy of {@code selectBody}.
     */
    public static SelectBody copy(SelectBody selectBody) {

        SelectCloner selectCloner = new SelectCloner();
        selectBody.accept(selectCloner);

        return selectCloner.copy;
    }

    /**
     * Creates a deep copy of the given {@link Distinct}.
     *
     * @param distinct the {@code Distinct} that needs to be copied.
     * @return a clone of {@code distinct}.
     */
    private Distinct copyDistinct(Distinct distinct) {

        if (distinct == null) {
            return null;
        }

        Distinct copy = new Distinct(distinct.isUseUnique());
        copy.setOnSelectItems(distinct.getOnSelectItems());

        return copy;
    }

    /**
     * Creates a copy of the given {@link Fetch}.
     *
     * @param fetch the {@code Fetch} that needs to be copied.
     * @return a clone of {@code fetch}.
     */
    private Fetch copyFetch(Fetch fetch) {

        if (fetch == null) {
            return null;
        }

        Fetch copy = new Fetch();
        copy.setRowCount(fetch.getRowCount());
        copy.setFetchParamFirst(fetch.isFetchParamFirst());
        copy.setFetchParam(fetch.getFetchParam());

        JdbcParameter fetchJdbcParameter = fetch.getFetchJdbcParameter();
        if (fetchJdbcParameter != null) {
            fetchJdbcParameter.accept(expressionCloner);
            copy.setFetchJdbcParameter((JdbcParameter) expressionCloner.getCopy());
        }

        return copy;
    }

    /**
     * Creates a copy of the given {@link First}.
     *
     * @param first the {@code Skip} that needs to be copied.
     * @return a clone of {@code first}.
     */
    private First copyFirst(First first) {

        if (first == null) {
            return null;
        }

        First copy = new First();
        copy.setRowCount(first.getRowCount());
        copy.setVariable(first.getVariable());
        copy.setKeyword(first.getKeyword());

        JdbcParameter jdbcParameter = copy.getJdbcParameter();
        if (jdbcParameter != null) {
            jdbcParameter.accept(expressionCloner);
            copy.setJdbcParameter((JdbcParameter) expressionCloner.getCopy());
        }

        return copy;
    }

    /**
     * Creates a deep copy of the given {@link Limit}.
     *
     * @param limit the {@code Limit} that needs to be copied.
     * @return a clone of {@code limit}.
     */
    private Limit copyLimit(Limit limit) {

        if (limit == null) {
            return null;
        }

        Limit copy = new Limit();
        copy.setLimitAll(limit.isLimitAll());
        copy.setLimitNull(limit.isLimitNull());

        Expression offset = limit.getOffset();
        if (offset != null) {
            offset.accept(expressionCloner);
            copy.setOffset(expressionCloner.getCopy());
        }

        Expression rowCount = limit.getRowCount();
        if (rowCount != null) {
            rowCount.accept(expressionCloner);
            copy.setRowCount(expressionCloner.getCopy());
        }

        return copy;
    }

    /**
     * Creates a copy of the given {@link Offset}.
     *
     * @param offset the {@code Offset} that needs to be copied.
     * @return a clone of {@code offset}.
     */
    private Offset copyOffset(Offset offset) {

        if (offset == null) {
            return null;
        }

        Offset copy = new Offset();

        copy.setOffset(offset.getOffset());
        copy.setOffsetParam(offset.getOffsetParam());

        Expression offsetJdbcParameter = offset.getOffsetJdbcParameter();
        if (offsetJdbcParameter != null) {
            offsetJdbcParameter.accept(expressionCloner);
            copy.setOffsetJdbcParameter((JdbcParameter) expressionCloner.getCopy());
        }

        return copy;
    }

    /**
     * Creates a copy of the given {@link OptimizeFor}.
     *
     * @param optimizeFor the {@code OptimizeFor} that needs to be copied.
     * @return a clone of {@code optimizeFor}.
     */
    private OptimizeFor copyOptimizeFor(OptimizeFor optimizeFor) {

        if (optimizeFor == null) {
            return null;
        }

        return new OptimizeFor(optimizeFor.getRowCount());
    }

    /**
     * Creates a copy of the given {@link Skip}.
     *
     * @param skip the {@code Skip} that needs to be copied.
     * @return a clone of {@code skip}.
     */
    private Skip copySkip(Skip skip) {

        if (skip == null) {
            return null;
        }

        Skip copy = new Skip();
        copy.setRowCount(skip.getRowCount());
        copy.setVariable(skip.getVariable());

        JdbcParameter jdbcParameter = copy.getJdbcParameter();
        if (jdbcParameter != null) {
            jdbcParameter.accept(expressionCloner);
            copy.setJdbcParameter((JdbcParameter) expressionCloner.getCopy());
        }

        return copy;
    }

    /**
     * Creates a copy of the given {@link Top}.
     *
     * @param top the {@code Top} that needs to be copied.
     * @return a clone of {@code top}.
     */
    private Top copyTop(Top top) {

        if (top == null) {
            return null;
        }

        Top copy = new Top();
        copy.setParenthesis(top.hasParenthesis());
        copy.setPercentage(top.isPercentage());

        Expression expression = top.getExpression();
        if (expression != null) {
            expression.accept(expressionCloner);
            copy.setExpression(expressionCloner.getCopy());
        }

        return copy;
    }

    /**
     * Creates a copy of the given {@link Wait}.
     *
     * @param wait the {@code Wait} that needs to be copied.
     * @return a clone of {@code wait}.
     */
    private Wait copyWait(Wait wait) {

        if (wait == null) {
            return null;
        }

        Wait copy = new Wait();
        copy.setTimeout(wait.getTimeout());

        return copy;
    }

    /**
     * Creates a deep copy of the given {@link List} of {@link SelectItem}s.
     *
     * @param selectItems the list that needs to be copied.
     * @return a clone of {@code selectItems}.
     */
    private List<SelectItem> copySelectItems(List<SelectItem> selectItems) {

        if (selectItems == null) {
            return null;
        }

        List<SelectItem> copy = new ArrayList<>(selectItems.size());
        for (SelectItem selectItem : selectItems) {

            selectItem.accept(this);
            copy.add(this.selectItem);
        }

        return copy;
    }

    @Override
    public void visit(PlainSelect plainSelect) {

        PlainSelect copy = new PlainSelect();

        copy.setDistinct(copyDistinct(plainSelect.getDistinct()));

        copy.setSelectItems(copySelectItems(plainSelect.getSelectItems()));

        List<Table> intoTables = plainSelect.getIntoTables();
        if (intoTables != null) {
            copy.setIntoTables(new ArrayList<>(intoTables));
        }

        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem != null) {
            fromItem.accept(this);
            copy.setFromItem(this.fromItem);
        }

        // TODO:
        copy.setJoins(plainSelect.getJoins());

        Expression where = plainSelect.getWhere();
        if (where != null) {
            where.accept(expressionCloner);
            copy.setWhere(expressionCloner.getCopy());
        }

        // TODO:
        copy.setGroupByElement(plainSelect.getGroupBy());

        // TODO:
        copy.setOrderByElements(plainSelect.getOrderByElements());

        Expression having = plainSelect.getHaving();
        if (having != null) {
            having.accept(expressionCloner);
            copy.setHaving(expressionCloner.getCopy());
        }

        copy.setLimit(copyLimit(plainSelect.getLimit()));

        copy.setOffset(copyOffset(plainSelect.getOffset()));

        copy.setFetch(copyFetch(plainSelect.getFetch()));

        copy.setOptimizeFor(copyOptimizeFor(plainSelect.getOptimizeFor()));

        copy.setSkip(copySkip(plainSelect.getSkip()));

        copy.setFirst(copyFirst(plainSelect.getFirst()));

        copy.setTop(copyTop(plainSelect.getTop()));

        OracleHierarchicalExpression oracleHierarchical = plainSelect.getOracleHierarchical();
        if (oracleHierarchical != null) {
            oracleHierarchical.accept(expressionCloner);
            copy.setOracleHierarchical((OracleHierarchicalExpression) expressionCloner.getCopy());
        }

        OracleHint oracleHint = plainSelect.getOracleHint();
        if (oracleHint != null) {
            oracleHint.accept(expressionCloner);
            copy.setOracleHint((OracleHint) expressionCloner.getCopy());
        }

        copy.setOracleSiblings(plainSelect.isOracleSiblings());

        copy.setForUpdate(plainSelect.isForUpdate());

        copy.setForUpdateTable(plainSelect.getForUpdateTable());

        copy.setUseBrackets(plainSelect.isUseBrackets());

        copy.setWait(copyWait(plainSelect.getWait()));

        copy.setMySqlSqlCalcFoundRows(plainSelect.getMySqlSqlCalcFoundRows());

        copy.setMySqlSqlNoCache(plainSelect.getMySqlSqlNoCache());

        copy.setForXmlPath(plainSelect.getForXmlPath());

        this.copy = copy;
    }

    @Override
    public void visit(SetOperationList setOpList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(WithItem withItem) {

        WithItem copy = new WithItem();
        copy.setName(withItem.getName());
        copy.setRecursive(withItem.isRecursive());

        copy.setWithItemList(copySelectItems(withItem.getWithItemList()));

        withItem.getSelectBody().accept(this);
        copy.setSelectBody(this.copy);

        this.copy = copy;
    }

    @Override
    public void visit(ValuesStatement valuesStatement) {

        List<Expression> expressions = valuesStatement.getExpressions();

        List<Expression> expressionsCopy = new ArrayList<>(expressions.size());
        for (Expression expression : expressions) {

            expression.accept(this.expressionCloner);
            expressionsCopy.add(expressionCloner.getCopy());
        }

        this.copy = new ValuesStatement(expressionsCopy);
    }

    @Override
    public void visit(AllColumns allColumns) {
        this.selectItem = new AllColumns();
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        this.selectItem = new AllTableColumns(allTableColumns.getTable());
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {

        SelectExpressionItem copy = new SelectExpressionItem();

        selectExpressionItem.getExpression().accept(expressionCloner);
        copy.setExpression(expressionCloner.getCopy());

        Alias alias = selectExpressionItem.getAlias();
        copy.setAlias(new Alias(alias.getName(), alias.isUseAs()));

        this.selectItem = copy;
    }

    @Override
    public void visit(Table tableName) {
        this.fromItem = tableName;
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.accept((ExpressionVisitor) expressionCloner);
        this.fromItem = (SubSelect) expressionCloner.getCopy();
    }

    @Override
    public void visit(SubJoin subjoin) {
        // TODO
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        // TODO
    }

    @Override
    public void visit(ValuesList valuesList) {

        ValuesList copy = new ValuesList();
        copy.setNoBrackets(valuesList.isNoBrackets());

        Alias alias = valuesList.getAlias();
        if (alias != null) {
            copy.setAlias(new Alias(alias.getName(), alias.isUseAs()));
        }

        List<String> columnNames = valuesList.getColumnNames();
        if (columnNames != null) {
            copy.setColumnNames(new ArrayList<>(columnNames));
        }

        MultiExpressionList multiExpressionList = valuesList.getMultiExpressionList();
        if (multiExpressionList != null) {
            multiExpressionList.accept(this.expressionCloner);
            copy.setMultiExpressionList((MultiExpressionList) this.expressionCloner.getItemsList());
        }

        this.fromItem = copy;
    }

    @Override
    public void visit(TableFunction tableFunction) {
        // TODO
    }

    @Override
    public void visit(ParenthesisFromItem aThis) {
        // TODO
    }

    public SelectBody getCopy() {
        return this.copy;
    }

}