package nl.tudelft.st01.unit.util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import nl.tudelft.st01.util.ExpressionCloner;
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

    private static final String COLUMN_NAME = "abc";

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link Between}s.
     */
    // Justification: Between has 3 subexpressions, which means we already need 3 asserts to test those.
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    @Test
    void testCopyBetween() {

        Column column = new Column(COLUMN_NAME);
        LongValue lowerVal = new LongValue(0);
        LongValue upperVal = new LongValue(1);

        Between original = new Between();
        original.setLeftExpression(column);
        original.setBetweenExpressionStart(lowerVal);
        original.setBetweenExpressionEnd(upperVal);

        Between copy = (Between) ExpressionCloner.copy(original);
        assertThat(copy)
                .isNotSameAs(original)
                .hasSameClassAs(original)
                .usingComparatorForType((a, b) -> 0, SimpleNode.class)
                .isEqualToComparingFieldByFieldRecursively(original);

        column.setColumnName(null);
        assertThat(((Column) copy.getLeftExpression()).getColumnName()).isEqualTo(COLUMN_NAME);

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

        Column column = new Column(COLUMN_NAME);

        ExistsExpression original = new ExistsExpression();
        original.setRightExpression(column);

        ExistsExpression copy = (ExistsExpression) ExpressionCloner.copy(original);
        assertThat(copy)
                .isNotSameAs(original)
                .hasSameClassAs(original)
                .usingComparatorForType((a, b) -> 0, SimpleNode.class)
                .isEqualToComparingFieldByFieldRecursively(original);

        column.setColumnName(null);
        assertThat(((Column) copy.getRightExpression()).getColumnName()).isEqualTo(COLUMN_NAME);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of {@link IsNullExpression}s.
     */
    @Test
    void testCopyIsNullExpression() {

        Column column = new Column(COLUMN_NAME);

        IsNullExpression original = new IsNullExpression();
        original.setLeftExpression(column);

        IsNullExpression copy = (IsNullExpression) ExpressionCloner.copy(original);
        assertThat(copy)
                .isNotSameAs(original)
                .hasSameClassAs(original)
                .usingComparatorForType((a, b) -> 0, SimpleNode.class)
                .isEqualToComparingFieldByFieldRecursively(original);

        column.setColumnName(null);
        assertThat(((Column) copy.getLeftExpression()).getColumnName()).isEqualTo(COLUMN_NAME);
    }

    /**
     * Tests whether {@link ExpressionCloner#copy(Expression)} makes deep copies of all {@link BinaryExpression}s.
     *
     * @param original the original binary expression.
     */
    @ParameterizedTest
    @MethodSource("provideBinaryExpressions")
    void testCopyBinaryExpressions(BinaryExpression original) {

        Column column = new Column(COLUMN_NAME);
        LongValue longValue = new LongValue(1);

        original.setLeftExpression(column);
        original.setRightExpression(longValue);

        BinaryExpression copy = (BinaryExpression) ExpressionCloner.copy(original);
        assertThat(copy)
                .isNotSameAs(original)
                .hasSameClassAs(original)
                .usingComparatorForType((a, b) -> 0, SimpleNode.class)
                .isEqualToComparingFieldByFieldRecursively(original);

        column.setColumnName(null);
        assertThat(((Column) copy.getLeftExpression()).getColumnName()).isEqualTo(COLUMN_NAME);

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
                Arguments.of(new RegExpMySQLOperator(RegExpMatchOperatorType.MATCH_CASESENSITIVE)),
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
