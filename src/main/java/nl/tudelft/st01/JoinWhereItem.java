package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Join;

/**
 * This class allows for linking a given join to the corresponding generated where expression.
 */
public class JoinWhereItem {

    private Join join;

    private Expression joinWhere;

    /**
     * JoinWhereItem constructor. Takes in the join and its corresponding where expression.
     * @param join The join related to the expression.
     * @param joinWhere The expression related to the join.
     */
    public JoinWhereItem(Join join, Expression joinWhere) {
        this.join = join;
        this.joinWhere = joinWhere;
    }

    public Join getJoin() {
        return join;
    }

    public Expression getJoinWhere() {
        return joinWhere;
    }
}
