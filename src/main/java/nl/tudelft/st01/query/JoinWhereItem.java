package nl.tudelft.st01.query;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Join;

import java.util.List;


/**
 * Wrapper class that can be used to link a {@link Join} to a generated WHERE expression.
 */
public class JoinWhereItem {

    private List<Join> joins;
    private Join join;


    private Expression joinWhere;

    /**
     * Creates a new {@code JoinWhereItem} linking a given JOIN to a WHERE expression.
     *
     * @param join a {@code Join} that should be linked to {@code joinWhere}.
     * @param joinWhere the WHERE expression that should be linked to {@code join}.
     */
    public JoinWhereItem(Join join, Expression joinWhere) {
        this.join = join;
        this.joinWhere = joinWhere;
    }

    public JoinWhereItem(List<Join> joins, Expression joinWhere) {
        this.joins = joins;
        this.joinWhere = joinWhere;
    }

    public Join getJoin() {
        return join;
    }

    public Expression getJoinWhere() {
        return joinWhere;
    }
}
