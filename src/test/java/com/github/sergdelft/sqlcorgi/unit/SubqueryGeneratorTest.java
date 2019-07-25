package com.github.sergdelft.sqlcorgi.unit;

import com.github.sergdelft.sqlcorgi.SubqueryGenerator;
import com.github.sergdelft.sqlcorgi.exceptions.CannotBeParsedException;
import com.github.sergdelft.sqlcorgi.schema.TableStructure;
import com.github.sergdelft.sqlcorgi.visitors.SelectStatementVisitor;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.*;

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
     * Tests whether {@link SubqueryGenerator} cannot be instantiated.
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
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect, TableStructure)} generates no coverage targets
     * if the provided query contains no subqueries.
     */
    @Test
    void testCoverQueryWithoutSubqueries() {

        Set<String> rules = SubqueryGenerator.coverSubqueries(select, new TableStructure());
        assertThat(rules).isEmpty();
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect, TableStructure)} generates coverage targets for
     * subqueries found in the FROM clause.
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

        SubqueryGenerator.coverSubqueries(select, new TableStructure());
        verify(selectBody).accept(isA(SelectStatementVisitor.class));
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect, TableStructure)} generates coverage targets if
     * the WHERE clause of the provided query contains a subquery.
     */
    @Test
    void testCoverQueryWhereContainsSubquery() {

        SubSelect subquery = createSimpleSubquery();

        AndExpression andExpression = new AndExpression(subquery, new NullValue());
        OrExpression orExpression = new OrExpression(new NullValue(), andExpression);

        select.setWhere(orExpression);
        Set<String> result = SubqueryGenerator.coverSubqueries(select, new TableStructure());

        Set<String> expected = new HashSet<>(Arrays.asList(
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE a = 'b') AND NULL OR NULL",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE NOT (a = 'b')) AND NULL OR NULL"
        ));

        assertThat(result).isEqualTo(expected);
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect, TableStructure)} generates coverage targets if
     * the HAVING clause of the provided query contains a subquery.
     */
    @Test
    void testCoverQueryHavingContainsSubquery() {

        SubSelect subquery = createSimpleSubquery();

        OrExpression orExpression = new OrExpression(new NullValue(), subquery);
        AndExpression andExpression = new AndExpression(orExpression, new NullValue());

        select.setHaving(andExpression);
        Set<String> result = SubqueryGenerator.coverSubqueries(select, new TableStructure());

        Set<String> expected = new HashSet<>(Arrays.asList(
                "SELECT * FROM t HAVING EXISTS (SELECT * FROM t WHERE a = 'b') AND NULL AND NULL",
                "SELECT * FROM t HAVING EXISTS (SELECT * FROM t WHERE NOT (a = 'b')) AND NULL AND NULL"
        ));

        assertThat(result).isEqualTo(expected);
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect, TableStructure)} generates coverage targets if
     * the WHERE clause of the provided query consists only of the subquery.
     */
    @Test
    void testCoverQueryWhereOnlySubquery() {

        select.setWhere(createSimpleSubquery());
        Set<String> result = SubqueryGenerator.coverSubqueries(select, new TableStructure());

        Set<String> expected = new HashSet<>(Arrays.asList(
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE a = 'b')",
                "SELECT * FROM t WHERE EXISTS (SELECT * FROM t WHERE NOT (a = 'b'))"
        ));

        assertThat(result).isEqualTo(expected);
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect, TableStructure)} generates coverage targets if
     * the HAVING clause of the provided query consists only of the subquery.
     */
    @Test
    void testCoverQueryHavingOnlySubquery() {

        select.setHaving(createSimpleSubquery());
        Set<String> result = SubqueryGenerator.coverSubqueries(select, new TableStructure());

        Set<String> expected = new HashSet<>(Arrays.asList(
                "SELECT * FROM t HAVING EXISTS (SELECT * FROM t WHERE a = 'b')",
                "SELECT * FROM t HAVING EXISTS (SELECT * FROM t WHERE NOT (a = 'b'))"
        ));

        assertThat(result).isEqualTo(expected);
    }

    /**
     * Checks that {@link SubqueryGenerator#coverSubqueries(PlainSelect, TableStructure)} generates coverage targets if
     * the WHERE or HAVING clause of the provided query contains an invalid subquery.
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

        assertThatThrownBy(() -> SubqueryGenerator.coverSubqueries(select, new TableStructure()))
                .isInstanceOf(CannotBeParsedException.class);
    }

    /**
     * Creates a simple subquery object: {@code (SELECT * FROM t WHERE a = 'b')}.
     *
     * @return a subquery object.
     */
    private static SubSelect createSimpleSubquery() {

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

        return subSelect;
    }
}
