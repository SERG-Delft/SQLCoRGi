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
     * Creates a new {@link JoinWhereItem} linking a given JOIN to a WHERE expression.
     *
     * @param join a {@link Join} that should be linked to {@link JoinWhereItem}.
     * @param joinWhere the WHERE {@link Expression} that should be linked to the {@link Join}.
     */
    public JoinWhereItem(Join join, Expression joinWhere) {
        this.join = join;
        this.joinWhere = joinWhere;
    }

    /**
     * Creates a new {@link JoinWhereItem} linking a given JOIN to a WHERE expression.
     *
     * @param joins a list of {@link Join}s that should be linked to {@link JoinWhereItem}.
     * @param joinWhere the WHERE expression that should be linked to {@link Join}.
     */
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

    public List<Join> getJoins() {
        return joins;
    }
}
