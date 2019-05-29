package nl.tudelft.st01.unit.query;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.statement.select.Join;
import nl.tudelft.st01.query.JoinWhereItem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests the functions of the {@link JoinWhereItemTest} class.
 */
public class JoinWhereItemTest {
    private static final Join JOIN = new Join();

    // For now it does not matter what is in the expression, just that it is an expression object
    private static final Expression WHERE = new GreaterThan();

    /**
     * Test for JoinWhereItem.
     */
    @Test
    public void constructorNotNull() {
        JoinWhereItem joinWhereItem = new JoinWhereItem(JOIN, WHERE);

        assertThat(joinWhereItem).isNotNull();
    }

    /**
     * Test for JoinWhereItem.
     */
    @Test
    public void getJoinTest() {
        JoinWhereItem joinWhereItem = new JoinWhereItem(JOIN, WHERE);

        assertThat(joinWhereItem.getJoin()).isEqualTo(JOIN);
    }

    /**
     * Test for JoinWhereItem.
     */
    @Test
    public void getJoinWhereTest() {
        JoinWhereItem joinWhereItem = new JoinWhereItem(JOIN, WHERE);

        assertThat(joinWhereItem.getJoinWhere()).isEqualTo(WHERE);
    }

}
