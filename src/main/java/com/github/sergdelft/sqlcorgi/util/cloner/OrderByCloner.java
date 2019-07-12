package com.github.sergdelft.sqlcorgi.util.cloner;

import net.sf.jsqlparser.statement.select.OrderByElement;

import java.util.ArrayList;
import java.util.List;

/**
 * This cloner can be used to create a deep copy of an {@link OrderByElement}.
 */
class OrderByCloner {

    private ExpressionCloner expressionCloner;

    /**
     * Creates a new instance of this class, which uses the provided {@link ExpressionCloner}.
     *
     * @param expressionCloner the {@code ExpressionCloner} to use.
     */
    OrderByCloner(ExpressionCloner expressionCloner) {
        this.expressionCloner = expressionCloner;
    }

    /**
     * Creates a deep copy of a list of {@link OrderByElement}s.
     *
     * @param orderByElements the list to copy.
     * @return a copy of the list.
     */
    List<OrderByElement> copy(List<OrderByElement> orderByElements) {

        if (orderByElements == null) {
            return null;
        }

        List<OrderByElement> copy = new ArrayList<>(orderByElements.size());
        for (OrderByElement orderByElement : orderByElements) {
            copy.add(copy(orderByElement));
        }

        return copy;
    }

    /**
     * Makes a deep copy of a given {@link OrderByElement}.
     *
     * @param orderByElement the {@code ORDER BY} to copy.
     * @return a copy of the {@code ORDER BY}.
     */
    private OrderByElement copy(OrderByElement orderByElement) {

        OrderByElement copy = new OrderByElement();

        copy.setAsc(orderByElement.isAsc());
        copy.setAscDescPresent(orderByElement.isAscDescPresent());
        copy.setNullOrdering(orderByElement.getNullOrdering());

        orderByElement.getExpression().accept(this.expressionCloner);
        copy.setExpression(this.expressionCloner.getCopy());

        return copy;
    }

    void setExpressionCloner(ExpressionCloner expressionCloner) {
        this.expressionCloner = expressionCloner;
    }
}
