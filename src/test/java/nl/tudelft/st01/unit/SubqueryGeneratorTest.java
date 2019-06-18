package nl.tudelft.st01.unit;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import nl.tudelft.st01.SubqueryGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static nl.tudelft.st01.SubqueryGenerator.coverSubqueries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Contains tests for {@link SubqueryGenerator}.
 */
class SubqueryGeneratorTest {

    private PlainSelect select;

    /**
     * Sets up a brand new {@link PlainSelect} for each test case.
     */
    @BeforeEach
    void setUp() {
        select = new PlainSelect();
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

        Table table = new Table("t");

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
        verify(selectBody).accept(any());
    }
}
