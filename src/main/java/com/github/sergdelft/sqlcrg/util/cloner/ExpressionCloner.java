package com.github.sergdelft.sqlcrg.util.cloner;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This visitor can be used to create a deep copy of an {@link Expression}. Note that by calling
 * {@link Expression#accept(ExpressionVisitor)} the {@code copy} field of the visitor instance will be overwritten.
 */
public class ExpressionCloner implements ExpressionVisitor, ItemsListVisitor {

    private Expression copy;
    private ItemsList itemsList;

    private OrderByCloner orderByCloner;
    private SelectCloner selectCloner;

    /**
     * Creates a new instance of this class, which uses a new {@link SelectCloner} for {@link SubSelect}s.
     */
    private ExpressionCloner() {
        this.orderByCloner = new OrderByCloner(this);
        this.selectCloner = new SelectCloner(this, this.orderByCloner);
    }

    /**
     * Creates a new instance of this class, which uses the provided {@link SelectCloner} for {@link SubSelect}s.
     *
     * @param selectCloner the {@code SelectCloner} to use.
     * @param orderByCloner the {@code OrderByCloner} to use.
     */
    ExpressionCloner(SelectCloner selectCloner, OrderByCloner orderByCloner) {
        this.selectCloner = selectCloner;
        this.orderByCloner = orderByCloner;
    }

    /**
     * Creates a deep copy of an {@link Expression}. This can be useful if you need to modify part of an expression,
     * but other parts of your code need to use the unmodified expression.
     *
     * @param expression the expression that needs to be copied.
     * @return a copy of {@code expression}.
     */
    public static Expression copy(Expression expression) {

        ExpressionCloner expressionCloner = new ExpressionCloner();
        expression.accept(expressionCloner);

        return expressionCloner.copy;
    }

    /**
     * Creates a deep copy of an {@link ItemsList}. This can be useful if you need to modify part of an expression,
     * but other parts of your code need to use the unmodified expression.
     *
     * @param itemsList the list of items that needs to be copied.
     * @return a copy of {@code itemsList}.
     */
    public static ItemsList copy(ItemsList itemsList) {

        ExpressionCloner expressionCloner = new ExpressionCloner();
        itemsList.accept(expressionCloner);

        return expressionCloner.itemsList;
    }

    /**
     * Copies the {@code not} field, and the left and right expressions of a given {@link BinaryExpression}.
     *
     * @param toBeCopied the {@code BinaryExpression} that needs to be copied.
     */
    private void copyBinaryExpression(BinaryExpression toBeCopied) {

        BinaryExpression temp = (BinaryExpression) this.copy;
        this.copy = null;

        toBeCopied.getLeftExpression().accept(this);
        Expression leftCopy = this.copy;

        toBeCopied.getRightExpression().accept(this);
        Expression rightCopy = this.copy;

        temp.setLeftExpression(leftCopy);
        temp.setRightExpression(rightCopy);
        this.copy = temp;
    }

    /**
     * Copies the {@code oldOracleJoinSyntax} and {@code oraclePriorPosition} fields, and {@link BinaryExpression}
     * fields of a given {@link OldOracleJoinBinaryExpression}.
     *
     * @param toBeCopied the {@code OldOracleJoinBinaryExpression} that needs to be copied.
     */
    private void copyOldOracleJoinBinaryExpression(OldOracleJoinBinaryExpression toBeCopied) {

        OldOracleJoinBinaryExpression temp = (OldOracleJoinBinaryExpression) this.copy;
        temp.setOldOracleJoinSyntax(toBeCopied.getOldOracleJoinSyntax());
        temp.setOraclePriorPosition(toBeCopied.getOraclePriorPosition());

        copyBinaryExpression(toBeCopied);
    }

    /**
     * Creates a deep copy of a {@link List} of {@link Expression}s.
     *
     * @param expressions the list that needs to be copied.
     * @return a copy of {@code expressions}.
     */
    private List<Expression> copyExpressionsList(List<Expression> expressions) {

        if (expressions == null) {
            return null;
        }

        List<Expression> expressionsCopy = new ArrayList<>(expressions.size());
        for (Expression expression : expressions) {
            expression.accept(this);
            expressionsCopy.add(this.copy);
        }

        return expressionsCopy;
    }

    @Override
    public void visit(BitwiseRightShift bitwiseRightShift) {
        this.copy = new BitwiseRightShift();
        copyBinaryExpression(bitwiseRightShift);
    }

    @Override
    public void visit(BitwiseLeftShift bitwiseLeftShift) {
        this.copy = new BitwiseLeftShift();
        copyBinaryExpression(bitwiseLeftShift);
    }

    @Override
    public void visit(NullValue nullValue) {
        this.copy = new NullValue();
    }

    @Override
    public void visit(Function function) {

        Function copy = new Function();

        copy.setName(function.getName());
        copy.setAllColumns(function.isAllColumns());
        copy.setDistinct(function.isDistinct());
        copy.setEscaped(function.isEscaped());
        copy.setAttributeName(function.getAttributeName());

        ExpressionList parameters = function.getParameters();
        if (parameters != null) {
            parameters.accept(this);
            copy.setParameters((ExpressionList) this.itemsList);
        }

        NamedExpressionList namedParameters = function.getNamedParameters();
        if (namedParameters != null) {
            namedParameters.accept(this);
            copy.setNamedParameters((NamedExpressionList) this.itemsList);
        }

        Expression attribute = function.getAttribute();
        if (attribute != null) {
            attribute.accept(this);
            copy.setAttribute(this.copy);
        }

        KeepExpression keep = function.getKeep();
        if (keep != null) {
            keep.accept(this);
            copy.setKeep((KeepExpression) this.copy);
        }

        this.copy = copy;
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        signedExpression.getExpression().accept(this);
        this.copy = new SignedExpression(signedExpression.getSign(), this.copy);
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        this.copy = new JdbcParameter(jdbcParameter.getIndex(), jdbcParameter.isUseFixedIndex());
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        this.copy = new JdbcNamedParameter(jdbcNamedParameter.getName());
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        this.copy = new DoubleValue(doubleValue.toString());
    }

    @Override
    public void visit(LongValue longValue) {
        this.copy = new LongValue(longValue.getValue());
    }

    @Override
    public void visit(HexValue hexValue) {
        this.copy = new HexValue(hexValue.getValue());
    }

    @Override
    public void visit(DateValue dateValue) {
        this.copy = new DateValue('\'' + dateValue.getValue().toString() + '\'');
    }

    @Override
    public void visit(TimeValue timeValue) {
        this.copy = new TimeValue('\'' + timeValue.getValue().toString() + '\'');
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        this.copy = new TimestampValue(timestampValue.getValue().toString());
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
        this.copy = new Parenthesis(this.copy);
    }

    @Override
    public void visit(StringValue stringValue) {

        StringValue copy = new StringValue("");
        copy.setValue(stringValue.getValue());
        copy.setPrefix(stringValue.getPrefix());

        this.copy = copy;
    }

    @Override
    public void visit(Addition addition) {
        this.copy = new Addition();
        copyBinaryExpression(addition);
    }

    @Override
    public void visit(Division division) {
        this.copy = new Division();
        copyBinaryExpression(division);
    }

    @Override
    public void visit(Multiplication multiplication) {
        this.copy = new Multiplication();
        copyBinaryExpression(multiplication);
    }

    @Override
    public void visit(Subtraction subtraction) {
        this.copy = new Subtraction();
        copyBinaryExpression(subtraction);
    }

    @Override
    public void visit(AndExpression andExpression) {
        this.copy = new AndExpression(null, null);
        copyBinaryExpression(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        this.copy = new OrExpression(null, null);
        copyBinaryExpression(orExpression);
    }

    @Override
    public void visit(Between between) {
        Between copy = new Between();
        copy.setNot(between.isNot());

        between.getLeftExpression().accept(this);
        copy.setLeftExpression(this.copy);

        between.getBetweenExpressionStart().accept(this);
        copy.setBetweenExpressionStart(this.copy);

        between.getBetweenExpressionEnd().accept(this);
        copy.setBetweenExpressionEnd(this.copy);

        this.copy = copy;
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        this.copy = new EqualsTo();
        copyOldOracleJoinBinaryExpression(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        this.copy = new GreaterThan();
        copyOldOracleJoinBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        this.copy = new GreaterThanEquals();
        copyOldOracleJoinBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {

        InExpression copy = new InExpression();
        copy.setNot(inExpression.isNot());
        copy.setOldOracleJoinSyntax(inExpression.getOldOracleJoinSyntax());

        Expression leftExpression = inExpression.getLeftExpression();
        if (leftExpression != null) {
            leftExpression.accept(this);
            copy.setLeftExpression(this.copy);
        }

        ItemsList leftItemsList = inExpression.getLeftItemsList();
        if (leftItemsList != null) {
            leftItemsList.accept(this);
            copy.setLeftItemsList(this.itemsList);
        }


        ItemsList rightItemsList = inExpression.getRightItemsList();
        if (rightItemsList != null) {
            rightItemsList.accept(this);
            copy.setRightItemsList(this.itemsList);
        }

        this.copy = copy;
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {

        IsNullExpression copy = new IsNullExpression();
        copy.setUseIsNull(isNullExpression.isUseIsNull());
        copy.setNot(isNullExpression.isNot());

        isNullExpression.getLeftExpression().accept(this);
        copy.setLeftExpression(this.copy);

        this.copy = copy;
    }

    @Override
    public void visit(LikeExpression likeExpression) {

        LikeExpression copy = new LikeExpression();
        copy.setNot(likeExpression.isNot());
        copy.setEscape(likeExpression.getEscape());
        copy.setCaseInsensitive(likeExpression.isCaseInsensitive());

        this.copy = copy;
        copyBinaryExpression(likeExpression);
    }

    @Override
    public void visit(MinorThan minorThan) {
        this.copy = new MinorThan();
        copyOldOracleJoinBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        this.copy = new MinorThanEquals();
        copyOldOracleJoinBinaryExpression(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        this.copy = new NotEqualsTo();
        copyOldOracleJoinBinaryExpression(notEqualsTo);
    }

    /**
     * Creates a <b>shallow copy</b> of a {@link Column}.
     *
     * @param tableColumn the {@code Column} to be copied.
     */
    @Override
    public void visit(Column tableColumn) {
        this.copy = new Column(tableColumn.getTable(), tableColumn.getColumnName());
    }

    /**
     * Only deep copies the {@code selectBody} and {@code alias} fields of the given {@link SubSelect}.
     *
     * @param subSelect the {@code SubSelect} to be copied.
     */
    @Override
    public void visit(SubSelect subSelect) {

        SubSelect copy = new SubSelect();
        copy.setUseBrackets(subSelect.isUseBrackets());
        copy.setPivot(subSelect.getPivot());

        List<WithItem> withItemsList = subSelect.getWithItemsList();
        if (withItemsList != null) {

            ArrayList<WithItem> withItemsCopy = new ArrayList<>(withItemsList.size());
            for (WithItem withItem : withItemsList) {

                withItem.accept(this.selectCloner);
                withItemsCopy.add((WithItem) this.selectCloner.getCopy());
            }

            copy.setWithItemsList(withItemsCopy);
        }

        Alias alias = subSelect.getAlias();
        if (alias != null) {
            copy.setAlias(new Alias(alias.getName(), alias.isUseAs()));
        }

        SelectBody selectBody = subSelect.getSelectBody();
        if (selectBody != null) {
            selectBody.accept(selectCloner);
            copy.setSelectBody(selectCloner.getCopy());
        }

        this.copy = copy;
        this.itemsList = copy;
    }

    @Override
    public void visit(ExpressionList expressionList) {
        ExpressionList copy = new ExpressionList();
        copy.setExpressions(copyExpressionsList(expressionList.getExpressions()));

        this.itemsList = copy;
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {

        NamedExpressionList copy = new NamedExpressionList();
        copy.setExpressions(copyExpressionsList(namedExpressionList.getExpressions()));

        List<String> names = namedExpressionList.getNames();
        if (names != null) {
            copy.setNames(new ArrayList<>(names));
        }

        this.itemsList = copy;
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {

        MultiExpressionList copy = new MultiExpressionList();

        for (ExpressionList expressionList : multiExprList.getExprList()) {
            expressionList.accept(this);
            copy.addExpressionList((ExpressionList) this.itemsList);
        }

        this.itemsList = copy;
    }

    @Override
    public void visit(CaseExpression caseExpression) {

        CaseExpression copy = new CaseExpression();

        Expression switchExpression = caseExpression.getSwitchExpression();
        if (switchExpression != null) {
            switchExpression.accept(this);
            copy.setSwitchExpression(this.copy);
        }

        Expression elseExpression = caseExpression.getElseExpression();
        if (elseExpression != null) {
            elseExpression.accept(this);
            copy.setElseExpression(this.copy);
        }

        List<WhenClause> whenClauses = caseExpression.getWhenClauses();
        if (whenClauses != null) {

            List<WhenClause> whenClausesCopy = new ArrayList<>(whenClauses.size());
            for (WhenClause whenClause : whenClauses) {
                whenClause.accept(this);
                whenClausesCopy.add((WhenClause) this.copy);
            }

            copy.setWhenClauses(whenClausesCopy);
        }

        this.copy = copy;
    }

    @Override
    public void visit(WhenClause whenClause) {

        WhenClause copy = new WhenClause();

        Expression whenExpression = whenClause.getWhenExpression();
        if (whenExpression != null) {
            whenExpression.accept(this);
            copy.setWhenExpression(this.copy);
        }

        Expression thenExpression = whenClause.getThenExpression();
        if (thenExpression != null) {
            thenExpression.accept(this);
            copy.setThenExpression(this.copy);
        }

        this.copy = copy;
    }

    @Override
    public void visit(ExistsExpression existsExpression) {

        ExistsExpression copy = new ExistsExpression();
        copy.setNot(existsExpression.isNot());

        existsExpression.getRightExpression().accept(this);
        copy.setRightExpression(this.copy);

        this.copy = copy;
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        allComparisonExpression.getSubSelect().accept((ExpressionVisitor) this);
        this.copy = new AllComparisonExpression((SubSelect) this.copy);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSubSelect().accept((ExpressionVisitor) this);
        this.copy = new AnyComparisonExpression(anyComparisonExpression.getAnyType(), (SubSelect) this.copy);
    }

    @Override
    public void visit(Concat concat) {
        this.copy = new Concat();
        copyBinaryExpression(concat);
    }

    @Override
    public void visit(Matches matches) {
        this.copy = new Matches();
        copyOldOracleJoinBinaryExpression(matches);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        this.copy = new BitwiseAnd();
        copyBinaryExpression(bitwiseAnd);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        this.copy = new BitwiseOr();
        copyBinaryExpression(bitwiseOr);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        this.copy = new BitwiseXor();
        copyBinaryExpression(bitwiseXor);
    }

    @Override
    public void visit(CastExpression cast) {

        CastExpression copy = new CastExpression();
        copy.setUseCastKeyword(cast.isUseCastKeyword());

        cast.getLeftExpression().accept(this);
        copy.setLeftExpression(this.copy);

        ColDataType type = cast.getType();
        ColDataType typeCopy = new ColDataType();

        List<String> argumentsStringList = type.getArgumentsStringList();
        if (argumentsStringList != null) {
            typeCopy.setArgumentsStringList(new ArrayList<>(argumentsStringList));
        }

        typeCopy.setArrayData(new ArrayList<>(type.getArrayData()));
        typeCopy.setCharacterSet(type.getCharacterSet());
        typeCopy.setDataType(type.getDataType());

        copy.setType(typeCopy);

        this.copy = copy;
    }

    @Override
    public void visit(Modulo modulo) {
        this.copy = new Modulo();
        copyBinaryExpression(modulo);
    }

    @Override
    public void visit(AnalyticExpression analyticExpression) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(ExtractExpression extractExpression) {

        ExtractExpression copy = new ExtractExpression();
        copy.setName(extractExpression.getName());

        extractExpression.getExpression().accept(this);
        copy.setExpression(this.copy);

        this.copy = copy;
    }

    @Override
    public void visit(IntervalExpression intervalExpression) {

        IntervalExpression copy = new IntervalExpression();
        copy.setIntervalType(intervalExpression.getIntervalType());
        copy.setParameter(intervalExpression.getParameter());

        Expression expression = intervalExpression.getExpression();
        if (expression != null) {
            expression.accept(this);
            copy.setExpression(this.copy);
        }

        this.copy = copy;
    }

    @Override
    public void visit(OracleHierarchicalExpression hierarchicalExpression) {

        OracleHierarchicalExpression copy = new OracleHierarchicalExpression();
        copy.setNoCycle(hierarchicalExpression.isNoCycle());
        copy.setConnectFirst(hierarchicalExpression.isConnectFirst());

        hierarchicalExpression.getStartExpression().accept(this);
        copy.setStartExpression(this.copy);

        hierarchicalExpression.getConnectExpression().accept(this);
        copy.setConnectExpression(this.copy);

        this.copy = copy;
    }

    @Override
    public void visit(RegExpMatchOperator regExpMatchOperator) {
        this.copy = new RegExpMatchOperator(regExpMatchOperator.getOperatorType());
        copyBinaryExpression(regExpMatchOperator);
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        this.copy = new JsonOperator(jsonExpr.getStringExpression());
        copyBinaryExpression(jsonExpr);
    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {

        RegExpMySQLOperator copy = new RegExpMySQLOperator(regExpMySQLOperator.getOperatorType());
        if (regExpMySQLOperator.isUseRLike()) {
            copy.useRLike();
        }

        this.copy = copy;
        copyBinaryExpression(regExpMySQLOperator);
    }

    @Override
    public void visit(UserVariable var) {

        UserVariable copy = new UserVariable();
        copy.setName(var.getName());
        copy.setDoubleAdd(var.isDoubleAdd());

        this.copy = copy;
    }

    @Override
    public void visit(NumericBind bind) {

        NumericBind copy = new NumericBind();
        copy.setBindId(bind.getBindId());

        this.copy = copy;
    }

    @Override
    public void visit(KeepExpression keepExpression) {

        KeepExpression copy = new KeepExpression();
        copy.setName(keepExpression.getName());
        copy.setFirst(keepExpression.isFirst());

        copy.setOrderByElements(this.orderByCloner.copy(keepExpression.getOrderByElements()));

        this.copy = copy;
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(ValueListExpression valueList) {

        ValueListExpression copy = new ValueListExpression();

        valueList.getExpressionList().accept(this);
        copy.setExpressionList((ExpressionList) this.itemsList);

        this.copy = copy;
    }

    @Override
    public void visit(RowConstructor rowConstructor) {

        RowConstructor copy = new RowConstructor();
        copy.setName(rowConstructor.getName());

        rowConstructor.getExprList().accept(this);
        copy.setExprList((ExpressionList) this.itemsList);

        this.copy = copy;
    }

    @Override
    public void visit(OracleHint hint) {

        OracleHint copy = new OracleHint();
        copy.setValue(hint.getValue());
        copy.setSingleLine(hint.isSingleLine());

        this.copy = copy;
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        this.copy = new TimeKeyExpression(timeKeyExpression.getStringValue());
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {

        DateTimeLiteralExpression copy = new DateTimeLiteralExpression();
        copy.setType(literal.getType());
        copy.setValue(literal.getValue());

        this.copy = copy;
    }

    @Override
    public void visit(NotExpression notExpression) {
        notExpression.getExpression().accept(this);
        this.copy = new NotExpression(this.copy, notExpression.isExclamationMark());
    }

    @Override
    public void visit(NextValExpression nextValExpression) {
        this.copy = new NextValExpression(Arrays.asList(nextValExpression.getName().split("\\.")));
    }

    @Override
    public void visit(CollateExpression collateExpression) {
        collateExpression.getLeftExpression().accept(this);
        this.copy = new CollateExpression(this.copy, collateExpression.getCollate());
    }

    @Override
    public void visit(SimilarToExpression similarToExpression) {

        SimilarToExpression copy = new SimilarToExpression();
        copy.setNot(similarToExpression.isNot());
        copy.setEscape(similarToExpression.getEscape());

        this.copy = copy;
        copyBinaryExpression(similarToExpression);
    }

    public Expression getCopy() {
        return this.copy;
    }

    ItemsList getItemsList() {
        return this.itemsList;
    }

}
