package nl.tudelft.st01.unit.util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
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
class ExpressionClonerTest {

    private static final String STRING_ABC = "abc";

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes a deep copy of a {@link NotExpression}.
     */
    @Test
    void testCopyNotExpression() {

        NotExpression original = new NotExpression(new NullValue());

        Expression copy = ExpressionCloner.copy(original);
        assertCopyEquals(original, copy);
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
    // Justification: Between has 3 subexpressions, which means we already need 3 asserts to test those.
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
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
