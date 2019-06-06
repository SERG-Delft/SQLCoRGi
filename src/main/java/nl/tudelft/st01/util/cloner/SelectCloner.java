package nl.tudelft.st01.util.cloner;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
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
    private OrderByCloner orderByCloner;

    /**
     * Creates a new instance of this class, which uses new cloners itself.
     */
    private SelectCloner() {

        this.orderByCloner = new OrderByCloner(null);
        this.expressionCloner = new ExpressionCloner(this);
        this.orderByCloner.setExpressionCloner(this.expressionCloner);
    }

    /**
     * Creates a new instance of this class, which uses the provided cloners.
     *
     * @param expressionCloner the {@code ExpressionCloner} to use.
     * @param orderByCloner the {@code OrderByCloner} to use.
     */
    SelectCloner(ExpressionCloner expressionCloner, OrderByCloner orderByCloner) {
        this.expressionCloner = expressionCloner;
        this.orderByCloner = orderByCloner;
    }

    /**
     * Creates a deep copy of a {@link SelectBody}.
     *
     * @param selectBody the {@code selectBody} that needs to be copied.
     * @return a copy of {@code selectBody}.
     */
    public static SelectBody copy(SelectBody selectBody) {

        SelectCloner selectCloner = new SelectCloner();
        selectBody.accept(selectCloner);

        return selectCloner.copy;
    }

    /**
     * Creates a deep copy of a {@link SelectItem}.
     *
     * @param selectItem the {@code selectItem} that needs to be copied.
     * @return a copy of {@code selectItem}.
     */
    public static SelectItem copy(SelectItem selectItem) {

        SelectCloner selectCloner = new SelectCloner();
        selectItem.accept(selectCloner);

        return selectCloner.selectItem;
    }

    /**
     * Creates a deep copy of a {@link FromItem}.
     *
     * @param fromItem the {@code FromItem} that needs to be copied.
     * @return a copy of {@code fromItem}.
     */
    public static FromItem copy(FromItem fromItem) {

        SelectCloner selectCloner = new SelectCloner();
        fromItem.accept(selectCloner);

        return selectCloner.fromItem;
    }

    /**
     * Creates a copy of the given {@link Alias}.
     *
     * @param alias the {@code Alias} that needs to be copied.
     * @return a clone of {@code alias}.
     */
    private Alias copyAlias(Alias alias) {

        if (alias == null) {
            return null;
        }

        return new Alias(alias.getName(), alias.isUseAs());
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
        copy.setOnSelectItems(copySelectItems(distinct.getOnSelectItems()));

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

        JdbcParameter jdbcParameter = first.getJdbcParameter();
        if (jdbcParameter != null) {
            jdbcParameter.accept(expressionCloner);
            copy.setJdbcParameter((JdbcParameter) expressionCloner.getCopy());
        }

        return copy;
    }

    /**
     * Creates a copy of the given {@link Join}.
     *
     * @param join the {@code Join} that needs to be copied.
     * @return a clone of {@code join}.
     */
    private Join copyJoin(Join join) {

        Join copy = new Join();
        copy.setOuter(join.isOuter());
        copy.setRight(join.isRight());
        copy.setLeft(join.isLeft());
        copy.setNatural(join.isNatural());
        copy.setFull(join.isFull());
        copy.setInner(join.isInner());
        copy.setSimple(join.isSimple());
        copy.setCross(join.isCross());
        copy.setSemi(join.isSemi());

        join.getRightItem().accept(this);
        copy.setRightItem(this.fromItem);

        Expression onExpression = join.getOnExpression();
        if (onExpression != null) {
            onExpression.accept(this.expressionCloner);
            copy.setOnExpression(this.expressionCloner.getCopy());
        }

        List<Column> usingColumns = join.getUsingColumns();
        if (usingColumns != null) {

            List<Column> usingColumnsCopy = new ArrayList<>(usingColumns.size());
            for (Column column : usingColumns) {

                column.accept(this.expressionCloner);
                usingColumnsCopy.add((Column) this.expressionCloner.getCopy());
            }
            copy.setUsingColumns(usingColumnsCopy);
        }

        KSQLJoinWindow joinWindow = join.getJoinWindow();
        if (joinWindow != null) {

            KSQLJoinWindow joinWindowCopy = new KSQLJoinWindow();
            joinWindowCopy.setAfterDuration(joinWindow.getAfterDuration());
            joinWindowCopy.setAfterTimeUnit(joinWindow.getAfterTimeUnit());
            joinWindowCopy.setBeforeAfterWindow(joinWindow.isBeforeAfterWindow());
            joinWindowCopy.setBeforeDuration(joinWindow.getBeforeDuration());
            joinWindowCopy.setBeforeTimeUnit(joinWindow.getBeforeTimeUnit());
            joinWindowCopy.setDuration(joinWindow.getDuration());
            joinWindowCopy.setTimeUnit(joinWindow.getTimeUnit());

            copy.setJoinWindow(joinWindowCopy);
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

            Expression jdbcCopy = expressionCloner.getCopy();
            if (jdbcCopy instanceof JdbcParameter) {
                copy.setOffsetJdbcParameter((JdbcParameter) jdbcCopy);
            } else {
                copy.setOffsetJdbcParameter((JdbcNamedParameter) jdbcCopy);
            }
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
     * Creates a copy of the given list of {@link SetOperation}s.
     *
     * @param setOperations the list of {@code SetOperation}s that needs to be copied.
     * @return a clone of {@code setOperations}.
     */
    private List<SetOperation> copySetOperations(List<SetOperation> setOperations) {

        List<SetOperation> copy = new ArrayList<>(setOperations.size());

        for (SetOperation setOperation : setOperations) {

            if (setOperation instanceof ExceptOp) {
                copy.add(new ExceptOp());
            } else if (setOperation instanceof IntersectOp) {
                copy.add(new IntersectOp());
            } else if (setOperation instanceof MinusOp) {
                copy.add(new MinusOp());
            } else {
                UnionOp unionOp = (UnionOp) setOperation;

                UnionOp unionCopy = new UnionOp();
                unionCopy.setAll(unionOp.isAll());
                unionCopy.setDistinct(unionOp.isDistinct());

                copy.add(unionCopy);
            }
        }

        return copy;
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

        JdbcParameter jdbcParameter = skip.getJdbcParameter();
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

        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {

            List<Join> joinsCopy = new ArrayList<>(joins.size());
            for (Join join : joins) {
                joinsCopy.add(copyJoin(join));
            }
            copy.setJoins(joinsCopy);
        }

        Expression where = plainSelect.getWhere();
        if (where != null) {
            where.accept(expressionCloner);
            copy.setWhere(expressionCloner.getCopy());
        }

        // TODO:
        copy.setGroupByElement(plainSelect.getGroupBy());

        copy.setOrderByElements(this.orderByCloner.copy(plainSelect.getOrderByElements()));

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

        SetOperationList copy = new SetOperationList();
        copy.setFetch(copyFetch(setOpList.getFetch()));
        copy.setLimit(copyLimit(setOpList.getLimit()));
        copy.setOffset(copyOffset(setOpList.getOffset()));

        copy.setOrderByElements(this.orderByCloner.copy(setOpList.getOrderByElements()));

        List<Boolean> brackets = new ArrayList<>(setOpList.getBrackets());

        List<SelectBody> selects = setOpList.getSelects();
        List<SelectBody> selectsCopy = new ArrayList<>(selects.size());
        for (SelectBody selectBody : selects) {

            selectBody.accept(this);
            selectsCopy.add(this.copy);
        }

        List<SetOperation> operations = copySetOperations(setOpList.getOperations());

        copy.setBracketsOpsAndSelects(brackets, selectsCopy, operations);

        this.copy = copy;
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

        copy.setAlias(copyAlias(selectExpressionItem.getAlias()));

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

        SubJoin copy = new SubJoin();
        copy.setAlias(copyAlias(subjoin.getAlias()));
        copy.setPivot(subjoin.getPivot());

        subjoin.getLeft().accept(this);
        copy.setLeft(this.fromItem);

        ArrayList<Join> joinListCopy = new ArrayList<>();
        for (Join join : subjoin.getJoinList()) {
            joinListCopy.add(copyJoin(join));
        }

        copy.setJoinList(joinListCopy);

        this.fromItem = subjoin;
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

        LateralSubSelect copy = new LateralSubSelect();
        copy.setAlias(lateralSubSelect.getAlias());

        lateralSubSelect.setPivot(lateralSubSelect.getPivot());

        lateralSubSelect.getSubSelect().accept(this);
        copy.setSubSelect((SubSelect) this.fromItem);

        this.fromItem = copy;
    }

    @Override
    public void visit(ValuesList valuesList) {

        ValuesList copy = new ValuesList();
        copy.setNoBrackets(valuesList.isNoBrackets());
        copy.setAlias(copyAlias(valuesList.getAlias()));

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

        TableFunction copy = new TableFunction();
        copy.setAlias(copyAlias(tableFunction.getAlias()));

        Function function = tableFunction.getFunction();
        if (function != null) {
            function.accept(this.expressionCloner);
            copy.setFunction((Function) this.expressionCloner.getCopy());
        }

        this.fromItem = copy;
    }

    @Override
    public void visit(ParenthesisFromItem parenthesisFromItem) {

        ParenthesisFromItem copy = new ParenthesisFromItem();
        copy.setAlias(copyAlias(parenthesisFromItem.getAlias()));

        FromItem fromItem = parenthesisFromItem.getFromItem();
        if (fromItem != null) {
            fromItem.accept(this);
            copy.setFromItem(this.fromItem);
        }

        this.fromItem = copy;
    }

    public SelectBody getCopy() {
        return this.copy;
    }

}
