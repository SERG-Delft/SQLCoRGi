package nl.tudelft.st01.unit.util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import nl.tudelft.st01.util.ExpressionCloner;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ExpressionCloner} utility class.
 */
// Justification: Some expressions have more than two object fields, which means we already need three asserts just
// to test those and the actual expression.
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class ExpressionClonerTest {

    private static final String STRING_ABC = "abc";

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes a deep copy of a {@link NotExpression}.
     */
    @Test
    void testCopyNotExpression() {

        NullValue nullValue = new NullValue();
        NotExpression original = new NotExpression(nullValue);

        NotExpression copy = (NotExpression) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getExpression()).isNotSameAs(nullValue);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes a deep copy of a {@link Parenthesis}.
     */
    @Test
    void testCopyParenthesis() {

        Parenthesis original = new Parenthesis();
        original.setExpression(new NullValue());

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes a deep copy of an {@link InExpression}.
     */
    @Test
    void testCopyInExpression() {

        InExpression original = new InExpression();
        original.setLeftExpression(new Column(STRING_ABC));

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes a deep copy of an {@link InExpression}.
     */
    @Test
    void testCopyInExpressionLeftItems() {

        ExpressionList left = new ExpressionList();
        left.setExpressions(new ArrayList<>());
        ExpressionList right = new ExpressionList();

        InExpression original = new InExpression();
        original.setLeftItemsList(left);
        original.setRightItemsList(right);

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link Between}s.
     */
    @Test
    void testCopyBetween() {

        Column column = new Column(STRING_ABC);
        LongValue lowerVal = new LongValue(0);
        LongValue upperVal = new LongValue(1);

        Between original = new Between();
        original.setLeftExpression(column);
        original.setBetweenExpressionStart(lowerVal);
        original.setBetweenExpressionEnd(upperVal);

        Between copy = (Between) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        column.setColumnName(null);
        assertThat(((Column) copy.getLeftExpression()).getColumnName()).isEqualTo(STRING_ABC);

        lowerVal.setValue(1);
        assertThat(((LongValue) copy.getBetweenExpressionStart()).getValue()).isEqualTo(0);

        upperVal.setValue(0);
        assertThat(((LongValue) copy.getBetweenExpressionEnd()).getValue()).isEqualTo(1);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link ExistsExpression}s.
     */
    @Test
    void testCopyExistsExpression() {

        Column column = new Column(STRING_ABC);

        ExistsExpression original = new ExistsExpression();
        original.setRightExpression(column);

        ExistsExpression copy = (ExistsExpression) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        column.setColumnName(null);
        assertThat(((Column) copy.getRightExpression()).getColumnName()).isEqualTo(STRING_ABC);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link IsNullExpression}s.
     */
    @Test
    void testCopyIsNullExpression() {

        Column column = new Column(STRING_ABC);

        IsNullExpression original = new IsNullExpression();
        original.setLeftExpression(column);

        IsNullExpression copy = (IsNullExpression) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        column.setColumnName(null);
        assertThat(((Column) copy.getLeftExpression()).getColumnName()).isEqualTo(STRING_ABC);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link ExpressionList}s.
     */
    @Test
    void testCopyExpressionList() {

        ExpressionList original = new ExpressionList();
        ArrayList<Expression> expressions = new ArrayList<>(2);

        LongValue expr0 = new LongValue(0);
        expressions.add(expr0);
        NullValue expr1 = new NullValue();
        expressions.add(expr1);

        original.setExpressions(expressions);

        ExpressionList copy = (ExpressionList) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getExpressions().get(0)).isNotSameAs(expr0);
        assertThat(copy.getExpressions().get(1)).isNotSameAs(expr1);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link NamedExpressionList}s.
     */
    @Test
    void testCopyNamedExpressionList() {

        NamedExpressionList original = new NamedExpressionList();
        ArrayList<Expression> expressions = new ArrayList<>(2);
        ArrayList<String> names = new ArrayList<>(2);

        LongValue expr0 = new LongValue(0);
        expressions.add(expr0);
        names.add("LONG");
        NullValue expr1 = new NullValue();
        expressions.add(expr1);
        names.add("NULL");

        original.setExpressions(expressions);
        original.setNames(names);

        NamedExpressionList copy = (NamedExpressionList) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getExpressions().get(0)).isNotSameAs(expr0);
        assertThat(copy.getExpressions().get(1)).isNotSameAs(expr1);
        assertThat(copy.getNames()).isNotSameAs(names);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link MultiExpressionList}s.
     */
    @Test
    void testCopyMultiExpressionList() {

        MultiExpressionList original = new MultiExpressionList();
        ExpressionList expressionList = new ExpressionList();
        original.addExpressionList(expressionList);

        MultiExpressionList copy = (MultiExpressionList) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getExprList().get(0)).isNotSameAs(expressionList);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of
     * {@link OracleHierarchicalExpression}s.
     */
    @Test
    void testCopyOracleHierarchicalExpression() {

        OracleHierarchicalExpression original = new OracleHierarchicalExpression();
        original.setStartExpression(new NullValue());
        original.setConnectExpression(new NullValue());

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link IntervalExpression}s.
     */
    @Test
    void testCopyIntervalExpression() {

        IntervalExpression original = new IntervalExpression();
        original.setExpression(new NullValue());
        original.setParameter(STRING_ABC);

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link WhenClause}s.
     */
    @Test
    void testCopyWhenClause() {

        WhenClause original = new WhenClause();
        NullValue when = new NullValue();
        original.setWhenExpression(when);
        LongValue then = new LongValue(1);
        original.setThenExpression(then);

        WhenClause copy = (WhenClause) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getWhenExpression()).isNotSameAs(when);
        assertThat(copy.getThenExpression()).isNotSameAs(then);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link JdbcNamedParameter}s.
     */
    @Test
    void testCopyJdbcNamedParameter() {

        JdbcNamedParameter original = new JdbcNamedParameter();
        original.setName(STRING_ABC);

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link Function}s.
     */
    @Test
    void testCopyFunction() {

        Function original = new Function();

        ExpressionList parameters = new ExpressionList();
        original.setParameters(parameters);

        NamedExpressionList namedParameters = new NamedExpressionList();
        original.setNamedParameters(namedParameters);

        original.setAllColumns(false);
        original.setDistinct(false);
        original.setEscaped(false);

        NullValue attribute = new NullValue();
        original.setAttribute(attribute);
        original.setAttributeName(STRING_ABC);

        KeepExpression keep = new KeepExpression();
        original.setKeep(keep);

        Function copy = (Function) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getParameters()).isNotSameAs(parameters);
        assertThat(copy.getNamedParameters()).isNotSameAs(namedParameters);
        assertThat(copy.getAttribute()).isNotSameAs(attribute);
        assertThat(copy.getKeep()).isNotSameAs(keep);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link Function}s.
     */
    @Test
    void testCopyFunctionAllNull() {

        Function original = new Function();
        original.setName(STRING_ABC);
        original.setAllColumns(true);
        original.setDistinct(true);
        original.setEscaped(true);

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link SignedExpression}s.
     */
    @Test
    void testCopySignedExpression() {

        LongValue longValue = new LongValue(0);
        SignedExpression original = new SignedExpression('+', longValue);

        SignedExpression copy = (SignedExpression) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getExpression()).isNotSameAs(longValue);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link CastExpression}s.
     */
    @Test
    void testCopyCastExpression() {

        CastExpression original = new CastExpression();
        original.setLeftExpression(new NullValue());

        ColDataType type = new ColDataType();
        type.setDataType(STRING_ABC);
        type.setCharacterSet(STRING_ABC);

        ArrayList<String> argumentsStringList = new ArrayList<>();
        type.setArgumentsStringList(argumentsStringList);

        ArrayList<Integer> arrayData = new ArrayList<>();
        type.setArrayData(arrayData);

        original.setType(type);

        CastExpression copy = (CastExpression) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getType()).isNotSameAs(type);
        assertThat(copy.getType().getArgumentsStringList()).isNotSameAs(argumentsStringList);
        assertThat(copy.getType().getArrayData()).isNotSameAs(type.getArrayData());
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link CastExpression}s.
     */
    @Test
    void testCopyCastExpressionEmptyType() {

        CastExpression original = new CastExpression();
        LongValue longValue = new LongValue(0);
        original.setLeftExpression(longValue);
        original.setType(new ColDataType());

        CastExpression copy = (CastExpression) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getLeftExpression()).isNotSameAs(longValue);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link CaseExpression}s.
     */
    @Test
    void testCopyCaseExpression() {

        CaseExpression original = new CaseExpression();

        NullValue switchExpression = new NullValue();
        original.setSwitchExpression(switchExpression);

        LongValue elseExpression = new LongValue(1);
        original.setElseExpression(elseExpression);

        ArrayList<WhenClause> whenClauses = new ArrayList<>(1);
        WhenClause whenClause = new WhenClause();
        whenClauses.add(whenClause);
        original.setWhenClauses(whenClauses);

        CaseExpression copy = (CaseExpression) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getSwitchExpression()).isNotSameAs(switchExpression);
        assertThat(copy.getElseExpression()).isNotSameAs(elseExpression);

        assertThat(copy.getWhenClauses()).isNotSameAs(whenClauses);
        assertThat(copy.getWhenClauses().get(0)).isNotSameAs(whenClause);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link CaseExpression}s.
     */
    @Test
    void testCopyCaseExpressionAllNull() {

        CaseExpression original = new CaseExpression();

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link JdbcParameter}s.
     */
    @Test
    void testCopyJdbcParameter() {

        JdbcParameter original = new JdbcParameter(1, true);

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link ExtractExpression}s.
     */
    @Test
    void testCopyExtractExpression() {

        ExtractExpression original = new ExtractExpression();
        original.setName(STRING_ABC);
        original.setExpression(new NullValue());

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes a deep copy of a {@link CollateExpression}.
     */
    @Test
    void testCopyCollateExpression() {

        CollateExpression original = new CollateExpression(new NullValue(), STRING_ABC);

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes a deep copy of an {@link OracleHint}.
     */
    @Test
    void testCopyOracleHint() {

        OracleHint original = new OracleHint();
        original.setValue(STRING_ABC);

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes a deep copy of a {@link TimeKeyExpression}.
     */
    @Test
    void testCopyTimeKeyExpression() {

        TimeKeyExpression original = new TimeKeyExpression(STRING_ABC);

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} throws an exception for {@link JsonExpression}s.
     */
    @Test
    void testCopyJsonExpression() {

        Throwable throwable = Assertions.catchThrowable(() -> ExpressionCloner.copy(new JsonExpression()));
        assertThat(throwable).isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of all {@link BinaryExpression}s.
     *
     * @param original the original binary expression.
     */
    @ParameterizedTest
    @MethodSource("provideBinaryExpressions")
    void testCopyBinaryExpressions(BinaryExpression original) {

        Column column = new Column(STRING_ABC);
        LongValue longValue = new LongValue(1);

        original.setLeftExpression(column);
        original.setRightExpression(longValue);

        BinaryExpression copy = (BinaryExpression) ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);

        column.setColumnName(null);
        assertThat(((Column) copy.getLeftExpression()).getColumnName()).isEqualTo(STRING_ABC);

        longValue.setValue(0L);
        assertThat(((LongValue) copy.getRightExpression()).getValue()).isEqualTo(1L);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of all value {@link Expression}s.
     *
     * @param original the original value.
     */
    @ParameterizedTest
    @MethodSource("provideValues")
    void testCopyValues(Expression original) {
        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
    }

    /**
     * Tests whether {@code copy} is equivalent to {@code original}.
     *
     * @param original the original {@code Expression}.
     * @param copy the copy of the original {@code Expression}.
     */
    private static void assertCopyEquals(Expression original, Expression copy) {
        assertThat(copy)
                .isNotSameAs(original)
                .hasSameClassAs(original)
                .usingComparatorForType((a, b) -> 0, SimpleNode.class)
                .isEqualToComparingFieldByFieldRecursively(original);
    }

    /**
     * Tests whether {@code copy} is equivalent to {@code original}.
     *
     * @param original the original {@code ItemsList}.
     * @param copy the copy of the original {@code ItemsList}.
     */
    private static void assertCopyEquals(ItemsList original, ItemsList copy) {
        assertThat(copy)
                .isNotSameAs(original)
                .hasSameClassAs(original)
                .isEqualToComparingFieldByFieldRecursively(original);
    }

    /**
     * Utility function that provides a stream of {@link BinaryExpression}s.
     *
     * @return a stream of binary expressions.
     */
    // Justification: This method is in fact used.
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> provideBinaryExpressions() {

        LikeExpression notLike = new LikeExpression();
        notLike.setNot();

        RegExpMySQLOperator regExpMySQLOperator = new RegExpMySQLOperator(RegExpMatchOperatorType.MATCH_CASESENSITIVE);
        regExpMySQLOperator.useRLike();

        return Stream.of(
                Arguments.of(new Addition()),
                Arguments.of(new AndExpression(null, null)),
                Arguments.of(new BitwiseAnd()),
                Arguments.of(new BitwiseLeftShift()),
                Arguments.of(new BitwiseOr()),
                Arguments.of(new BitwiseRightShift()),
                Arguments.of(new BitwiseXor()),
                Arguments.of(new Concat()),
                Arguments.of(new Division()),
                Arguments.of(new EqualsTo()),
                Arguments.of(new GreaterThan()),
                Arguments.of(new GreaterThanEquals()),
                Arguments.of(new JsonOperator("@>")),
                Arguments.of(new LikeExpression()),
                Arguments.of(notLike),
                Arguments.of(new Matches()),
                Arguments.of(new MinorThan()),
                Arguments.of(new MinorThanEquals()),
                Arguments.of(new Modulo()),
                Arguments.of(new Multiplication()),
                Arguments.of(new NotEqualsTo()),
                Arguments.of(new OrExpression(null, null)),
                Arguments.of(new RegExpMatchOperator(RegExpMatchOperatorType.MATCH_CASEINSENSITIVE)),
                Arguments.of(new RegExpMatchOperator(RegExpMatchOperatorType.MATCH_CASESENSITIVE)),
                Arguments.of(new RegExpMatchOperator(RegExpMatchOperatorType.NOT_MATCH_CASEINSENSITIVE)),
                Arguments.of(new RegExpMatchOperator(RegExpMatchOperatorType.NOT_MATCH_CASESENSITIVE)),
                Arguments.of(regExpMySQLOperator),
                Arguments.of(new RegExpMySQLOperator(RegExpMatchOperatorType.MATCH_CASEINSENSITIVE)),
                Arguments.of(new Subtraction())
        );
    }

    /**
     * Utility function that provides a stream of value {@link Expression}s.
     *
     * @return a stream of values.
     */
    // Justification: This method is in fact used.
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> provideValues() {

        ValueListExpression valueListExpression = new ValueListExpression();
        valueListExpression.setExpressionList(new ExpressionList());

        RowConstructor rowConstructor = new RowConstructor();
        rowConstructor.setName("rowC");
        rowConstructor.setExprList(new ExpressionList());

        DateTimeLiteralExpression dateTimeLiteralExpression = new DateTimeLiteralExpression();
        dateTimeLiteralExpression.setValue("1999-09-09");
        dateTimeLiteralExpression.setType(DateTimeLiteralExpression.DateTime.DATE);

        ArrayList<String> nameList = new ArrayList<>();
        nameList.add("a");
        nameList.add("b");

        return Stream.of(
                Arguments.of(new NullValue()),
                Arguments.of(new LongValue(1)),
                Arguments.of(new DoubleValue("1.0")),
                Arguments.of(new HexValue("0x1B")),
                Arguments.of(new DateValue("'2000-01-01'")),
                Arguments.of(new TimeValue("'12:34:56'")),
                Arguments.of(new TimestampValue("'2000-01-01 12:34:56'")),
                Arguments.of(new StringValue("'h'")),
                Arguments.of(new UserVariable()),
                Arguments.of(new NumericBind()),
                Arguments.of(valueListExpression),
                Arguments.of(rowConstructor),
                Arguments.of(dateTimeLiteralExpression),
                Arguments.of(new NextValExpression(nameList))
        );
    }

}
