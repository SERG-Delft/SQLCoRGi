package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Join;

public class JoinWhereItem {
    Join join;
    Expression joinWhere;

    public JoinWhereItem(Join join, Expression joinWhere) {
        this.join = join;
        this.joinWhere = joinWhere;
    }

    public void setJoin(Join join) {
        this.join = join;
    }

    public void setJoinWhere(Expression joinWhere) {
        this.joinWhere = joinWhere;
    }

    public Join getJoin() {
        return join;
    }

    public Expression getJoinWhere() {
        return joinWhere;
    }
}
