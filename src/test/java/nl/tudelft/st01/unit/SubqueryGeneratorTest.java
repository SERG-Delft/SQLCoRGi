package nl.tudelft.st01.unit;

import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import nl.tudelft.st01.SubqueryGenerator;
import nl.tudelft.st01.util.exceptions.CannotBeParsedException;
import nl.tudelft.st01.visitors.SelectStatementVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.*;

import static nl.tudelft.st01.SubqueryGenerator.coverSubqueries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Contains tests for {@link SubqueryGenerator}.
 */
class SubqueryGeneratorTest {

    private static final String TABLE_T = "t";
    private static final String COLUMN_A = "a";
    private static final String STRING_B = "b";

    private PlainSelect select;

    /**
     * Sets up a brand new {@link PlainSelect} for each test case.
     */
    @BeforeEach
    void setUp() {
        select = new PlainSelect();
        ArrayList<SelectItem> selectItems = new ArrayList<>(1);
        selectItems.add(new AllColumns());
        select.setSelectItems(selectItems);
        select.setFromItem(new Table(TABLE_T));
    }

    /**
     * Tests whether {@link SubqueryGenerator}s cannot be instantiated.
     *
     * @throws NoSuchMethodException if the {@code SubqueryGenerator} constructor cannot be found.
     */
    @Test
    void testConstructorThrowsException() throws NoSuchMethodException {

        Constructor<SubqueryGenerator> generatorConstructor = SubqueryGenerator.class.getDeclaredConstructor();
        generatorConstructor.setAccessible(true);

        assertThatThrownBy(generatorConstructor::newInstance)
                .hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect)} generates no coverage targets if the
     * provided query contains no subqueries.
     */
    @Test
    void testCoverQueryWithoutSubqueries() {

        Set<String> rules = coverSubqueries(select);
        assertThat(rules).isEmpty();
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect)} generates coverage targets for subqueries
     * found in the FROM clause.
     */
    @Test
    void testCoverQueryFromSubquery() {

        SelectBody selectBody = mock(SelectBody.class);

        SubSelect subSelect = new SubSelect();
        subSelect.setSelectBody(selectBody);

        Table table = new Table(TABLE_T);

        SubJoin subJoin = new SubJoin();
        subJoin.setLeft(table);
        List<Join> subJoinsList = new ArrayList<>(1);
        subJoin.setJoinList(subJoinsList);

        Join subJoinJoin = new Join();
        subJoinJoin.setRightItem(subSelect);
        subJoinsList.add(subJoinJoin);

        Join join = new Join();
        join.setRightItem(subJoin);

        List<Join> joinList = new ArrayList<>(1);
        joinList.add(join);

        select.setFromItem(table);
        select.setJoins(joinList);

        coverSubqueries(select);
        verify(selectBody).accept(isA(SelectStatementVisitor.class));
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect)} generates coverage targets if the
     * WHERE clause of the provided query consists only of the subquery.
     */
    @Test
    void testCoverQueryWhereOnlySubquery() {

        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(COLUMN_A));
        equalsTo.setRightExpression(new StringValue(STRING_B));

        PlainSelect plainSelect = new PlainSelect();
        plainSelect.setWhere(equalsTo);

        ArrayList<SelectItem> selectItems = new ArrayList<>();
        selectItems.add(new AllColumns());
        plainSelect.setSelectItems(selectItems);

        plainSelect.setFromItem(new Table(TABLE_T));

        SubSelect subSelect = new SubSelect();
        subSelect.setSelectBody(plainSelect);
        select.setWhere(subSelect);

        Set<String> result = coverSubqueries(select);

        Set<String> expected = new HashSet<>(Arrays.asList(
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE a = 'b')",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE a <> 'b')",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE a IS NULL)"
        ));

        assertThat(result).isEqualTo(expected);
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect)} generates coverage targets if the
     * HAVING clause of the provided query consists only of the subquery.
     */
    @Test
    void testCoverQueryHavingOnlySubquery() {

        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(COLUMN_A));
        equalsTo.setRightExpression(new StringValue(STRING_B));

        PlainSelect plainSelect = new PlainSelect();
        plainSelect.setWhere(equalsTo);

        ArrayList<SelectItem> selectItems = new ArrayList<>();
        selectItems.add(new AllColumns());
        plainSelect.setSelectItems(selectItems);

        plainSelect.setFromItem(new Table(TABLE_T));

        SubSelect subSelect = new SubSelect();
        subSelect.setSelectBody(plainSelect);
        select.setHaving(subSelect);

        Set<String> result = coverSubqueries(select);

        Set<String> expected = new HashSet<>(Arrays.asList(
                "SELECT * FROM t HAVING EXISTS (SELECT * FROM t WHERE a = 'b')",
                "SELECT * FROM t HAVING EXISTS (SELECT * FROM t WHERE a <> 'b')",
                "SELECT * FROM t HAVING EXISTS (SELECT * FROM t WHERE a IS NULL)"
        ));

        assertThat(result).isEqualTo(expected);
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect)} generates coverage targets if the
     * WHERE or HAVING clause of the provided query contains an invalid subquery.
     */
    @Test
    void testCoverQuerySelectOpInvalidSubquery() {

        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column());
        equalsTo.setRightExpression(new LongValue(0));

        PlainSelect plainSelect = new PlainSelect();
        plainSelect.setWhere(equalsTo);

        ArrayList<SelectItem> selectItems = new ArrayList<>(1);
        selectItems.add(new AllColumns());
        plainSelect.setSelectItems(selectItems);

        SubSelect subSelect = new SubSelect();
        subSelect.setSelectBody(plainSelect);
        select.setWhere(subSelect);

        assertThatThrownBy(() -> coverSubqueries(select)).isInstanceOf(CannotBeParsedException.class);
    }
}
