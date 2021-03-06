package com.github.sergdelft.sqlcorgi.unit.query;

import com.github.sergdelft.sqlcorgi.query.JoinWhereItem;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.statement.select.Join;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests the functions of the {@link JoinWhereItemTest} class.
 */
class JoinWhereItemTest {

    private static final Join JOIN = new Join();
    private static final GreaterThan WHERE = new GreaterThan();

    /**
     * Tests whether {@link JoinWhereItem#JoinWhereItem(Join, Expression)} does not return {@code null}.
     */
    @Test
    void testConstructorNotNull() {
        JoinWhereItem joinWhereItem = new JoinWhereItem(JOIN, WHERE);

        assertThat(joinWhereItem).isNotNull();
    }

    /**
     * Tests whether the constructor correctly sets the join, and returns it with {@link JoinWhereItem#getJoin()}.
     */
    @Test
    void testGetJoin() {
        JoinWhereItem joinWhereItem = new JoinWhereItem(JOIN, WHERE);

        assertThat(joinWhereItem.getJoin()).isSameAs(JOIN);
    }

    /**
     * Tests whether the constructor correctly sets the join, and returns it with {@link JoinWhereItem#getJoinWhere()}.
     */
    @Test
    void testGetJoinWhere() {
        JoinWhereItem joinWhereItem = new JoinWhereItem(JOIN, WHERE);

        assertThat(joinWhereItem.getJoinWhere()).isSameAs(WHERE);
    }

}
