package nl.tudelft.st01.util.cloner;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.GroupByElement;

import java.util.ArrayList;
import java.util.List;

/**
 * This cloner can be used to create a deep copy of {@link GroupByElement}s.
 */
class GroupByCloner {

    private ExpressionCloner expressionCloner;

    /**
     * Creates a new instance of this class, which uses the provided {@link ExpressionCloner}.
     *
     * @param expressionCloner the {@code ExpressionCloner} to use.
     */
    GroupByCloner(ExpressionCloner expressionCloner) {
        this.expressionCloner = expressionCloner;
    }

    /**
     * Makes a deep copy of a given {@link GroupByElement}.
     *
     * @param groupByElement the {@code GROUP BY} to copy.
     * @return a copy of the {@code GROUP BY}.
     */
    GroupByElement copy(GroupByElement groupByElement) {

        if (groupByElement == null) {
            return null;
        }

        GroupByElement copy = new GroupByElement();

        List<Expression> groupByExpressions = groupByElement.getGroupByExpressions();
        if (groupByExpressions != null) {

            List<Expression> expressionsCopy = new ArrayList<>(groupByExpressions.size());
            for (Expression expression : groupByExpressions) {

                expression.accept(this.expressionCloner);
                expressionsCopy.add(this.expressionCloner.getCopy());
            }
            copy.setGroupByExpressions(expressionsCopy);
        }

        List groupingSets = groupByElement.getGroupingSets();
        if (groupingSets != null) {

            List<Object> groupingSetsCopy = new ArrayList<>(groupingSets.size());
            for (Object o : groupingSets) {

                if (o instanceof Expression) {
                    ((Expression) o).accept(this.expressionCloner);
                    groupingSetsCopy.add(this.expressionCloner.getCopy());
                } else if (o instanceof ExpressionList) {
                    ((ExpressionList) o).accept(this.expressionCloner);
                    groupingSetsCopy.add(this.expressionCloner.getItemsList());
                }
            }
            copy.setGroupingSets(groupingSetsCopy);
        }

        return copy;
    }

}
