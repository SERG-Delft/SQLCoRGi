package nl.tudelft.st01;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import nl.tudelft.st01.query.JoinWhereItem;
import nl.tudelft.st01.query.OuterIncrementRelation;
import nl.tudelft.st01.visitors.ExpressionTraverserVisitor;
import nl.tudelft.st01.visitors.join.OnExpressionVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * This class allows for mutating a given query such that a set of mutated queries is returned.
 */
public class JoinRulesGenerator {
    private enum JoinType {
        LEFT, RIGHT, INNER
    }

    private List<OuterIncrementRelation> outerIncrementRelations;

    /**
     * Takes in a statement and mutates the joins. Each join will have its own set of mutations added to the results.
     *
     * @param plainSelect The statement for which the joins have to be mutated.
     * @return A set of mutated queries in string format.
     */
    public Set<String> generate(PlainSelect plainSelect) {
        List<Join> joins = plainSelect.getJoins();
        Expression where = plainSelect.getWhere();
        Set<String> result = new TreeSet<>();
        if (joins == null || joins.isEmpty()) {
            return new HashSet<>();
        }

        outerIncrementRelations = generateOIRsForEachJoin(plainSelect.getJoins());

        if (!outerIncrementRelations.isEmpty()) {
            Set<JoinWhereItem> items = handleJoins(plainSelect);
            for (JoinWhereItem j : items) {
                plainSelect.setJoins(j.getJoins());
                plainSelect.setWhere(j.getJoinWhere());
                result.add(plainSelect.toString());
            }
        }

        plainSelect.setJoins(joins);
        plainSelect.setWhere(where);
        return result;
    }

    private List<OuterIncrementRelation> generateOIRsForEachJoin(List<Join> joins) {
        Map<String, List<Column>> map = new HashMap<>();
        List<OuterIncrementRelation> out = new ArrayList<>();
        OnExpressionVisitor onExpressionVisitor = new OnExpressionVisitor(map);
        for (Join join : joins) {
            if (join.getOnExpression() != null) {
                join.getOnExpression().accept(onExpressionVisitor);
                out.add(getOuterIncrementRelation(map, join));
                map.clear();
            } else if (!join.isSimple()){
                throw new IllegalStateException("The ON condition cannot be null");
            }
        }

        return out;
    }

    private Set<JoinWhereItem> handleJoins(PlainSelect plainSelect) {
        List<Join> joins = plainSelect.getJoins();
        Expression whereCondition = plainSelect.getWhere();

        Set<JoinWhereItem> results = new HashSet<>();

        for (int i = 0; i < joins.size(); i++) {
            OuterIncrementRelation oir = outerIncrementRelations.get(i);

            if (oir.getLoiRelColumns() != null && !oir.getLoiRelColumns().isEmpty()) {
                results.addAll(generateLeftJoinRules(joins, i, whereCondition, oir));
            }

            if (oir.getRoiRelColumns() != null && !oir.getRoiRelColumns().isEmpty()) {
                results.addAll(generateRightJoinRules(joins, i, whereCondition, oir));
                results.add(new JoinWhereItem(setAllToInner(joins), wrapInParentheses(whereCondition)));
            }
        }

        plainSelect.setWhere(whereCondition);
        plainSelect.setJoins(joins);


        return results;
    }

    private Set<JoinWhereItem> generateLeftJoinRules(List<Join> joins, int index, Expression where,
                                                     OuterIncrementRelation oir) {
        List<JoinType> labelsLeft = label(JoinType.LEFT, joins, index);
        Set<JoinWhereItem> out = new HashSet<>();
        List<Join> tJoinsLoi = transformJoins(joins, labelsLeft);

        Expression reducedWhereLoi = nullReduction(where, oir, JoinType.LEFT, false);
        Expression reducedWhereLoiNull = nullReduction(where, oir, JoinType.LEFT, true);

        // If the on condition only contains columns from one table, roirels will be empty or null.
        if (oir.getRoiRelColumns() == null || oir.getRoiRelColumns().isEmpty()) {
            Join join = genericCopyOfJoin(tJoinsLoi.get(index));
            join.setRight(true);
            tJoinsLoi.set(index, join);
            Expression not = new NotExpression(new Parenthesis(join.getOnExpression()));
            out.add(new JoinWhereItem(tJoinsLoi, concatenate(concatenate(not, getRightOuterIncrement(oir, false), true), reducedWhereLoi, true)));

        } else {
            Expression loi = getLeftOuterIncrement(oir, false);
            Expression loiNull = getLeftOuterIncrement(oir, true);

            out.add(new JoinWhereItem(tJoinsLoi, concatenate(loi, reducedWhereLoi, true)));
            out.add(new JoinWhereItem(tJoinsLoi, concatenate(loiNull, reducedWhereLoiNull, true)));
        }

        return out;
    }

    private Set<JoinWhereItem> generateRightJoinRules(List<Join> joins, int index, Expression where,
                                                     OuterIncrementRelation oir) {
        List<JoinType> labelsRight = label(JoinType.RIGHT, joins, index);
        Set<JoinWhereItem> out = new HashSet<>();

        List<Join> tJoinsRoi = transformJoins(joins, labelsRight);

        Expression reducedWhereRoi = nullReduction(where, oir, JoinType.RIGHT, false);
        Expression reducedWhereRoiNull = nullReduction(where, oir, JoinType.RIGHT, true);

        if (oir.getLoiRelColumns() == null || oir.getLoiRelColumns().isEmpty()) {
            Join join = genericCopyOfJoin(tJoinsRoi.get(index));
            join.setLeft(true);
            tJoinsRoi.set(index, join);
            Expression not = new NotExpression(new Parenthesis(join.getOnExpression()));
            out.add(new JoinWhereItem(tJoinsRoi, concatenate(concatenate(not, getLeftOuterIncrement(oir, false), true), reducedWhereRoi, true)));

        } else {
            Expression roi = getRightOuterIncrement(oir, false);
            Expression roiNull = getRightOuterIncrement(oir, true);

            out.add(new JoinWhereItem(tJoinsRoi, concatenate(roi, reducedWhereRoi, true)));
            out.add(new JoinWhereItem(tJoinsRoi, concatenate(roiNull, reducedWhereRoiNull, true)));
        }

        return out;
    }

    private List<Join> setAllToInner(List<Join> joins) {
        List<Join> outJoins = new ArrayList<>();
        for (Join join : joins) {
            outJoins.add(setJoinType(genericCopyOfJoin(join), JoinType.INNER));
        }

        return outJoins;
    }

    private static Expression nullReduction(Expression expression, OuterIncrementRelation oir, JoinType joinType, boolean nullable) {
        if (expression != null) {
            Set<String> includeTables;
            List<Column> columns;

            if (joinType == JoinType.LEFT) {
                includeTables = oir.getRoiRelations();
                columns = oir.getRoiRelColumns();
            } else if (joinType == JoinType.RIGHT) {
                includeTables = oir.getLoiRelations();
                columns = oir.getLoiRelColumns();
            } else {
                throw new IllegalArgumentException("Join type must be either LEFT or RIGHT");
            }

            ExpressionTraverserVisitor traverserVisitor = new ExpressionTraverserVisitor();
            traverserVisitor.setTables(includeTables);
            traverserVisitor.setOnColumns(columns);

            if (nullable) {
                traverserVisitor.setNullColumns(columns);
            }

            expression.accept(traverserVisitor);

            return traverserVisitor.getExpression();
        }

        return null;
    }

    private Expression getLeftOuterIncrement(OuterIncrementRelation oiRel, boolean nullable) {
        List<Column> loiColumns = oiRel.getLoiRelColumns();
        List<Column> roiColumns = oiRel.getRoiRelColumns();

        return concatenate(
                createIsNullExpressions(loiColumns, true),
                createIsNullExpressions(roiColumns, nullable), false);
    }

    private Expression getRightOuterIncrement(OuterIncrementRelation oiRel, boolean nullable) {
        List<Column> loiColumns = oiRel.getLoiRelColumns();
        List<Column> roiColumns = oiRel.getRoiRelColumns();
        return concatenate(
                createIsNullExpressions(roiColumns, true),
                createIsNullExpressions(loiColumns, nullable), false);
    }

    private List<Join> transformJoins(List<Join> joins, List<JoinType> labels) {
        if (labels.size() != joins.size()) {
            throw new IllegalStateException("The size of the list of joins must be "
                    + "equal to the size of the list of labels");
        }

        Iterator<Join> joinIterator = joins.iterator();
        Iterator<JoinType> joinTypeIterator = labels.iterator();

        List<Join> transformedJoins = new ArrayList<>();

        while(joinIterator.hasNext() && joinTypeIterator.hasNext()) {
            Join tJoin = transformJoin(joinIterator.next(), joinTypeIterator.next());
            transformedJoins.add(tJoin);
        }

        return transformedJoins;
    }

    private Join transformJoin(Join join, JoinType joinType) {
        return setJoinType(genericCopyOfJoin(join), joinType);
    }

    private Join setJoinType(Join join, JoinType joinType) {
        switch (joinType) {
            case LEFT:
                join.setLeft(true);
                break;
            case RIGHT:
                join.setRight(true);
                break;
            case INNER:
                join.setInner(true);
                break;
            default:
                throw new IllegalArgumentException("Join type must be either LEFT, RIGHT or INNER");
        }

        return join;
    }

    private List<JoinType> label(JoinType joinType, List<Join> joins, int index) {
        if (index >= joins.size()) {
            throw new IllegalArgumentException("The index cannot be larger than the size of the given list of joins.");
        }

        Set<String> mvoi = new HashSet<>();
        List<JoinType> labels = Arrays.asList(new JoinType[joins.size()]);

        OuterIncrementRelation currOiRel = outerIncrementRelations.get(index);

        switch (joinType) {
            case LEFT:  mvoi.addAll(currOiRel.getLoiRelations());
                break;
            case RIGHT: mvoi.addAll(currOiRel.getRoiRelations());
                break;
            default: throw new IllegalArgumentException("The join type must be either LEFT or RIGHT");
        }

        labels.set(index, joinType);

        for (int i = 0; i < joins.size(); i++) {
            if (labels.get(i) == null) {
                OuterIncrementRelation oiRel = outerIncrementRelations.get(i);
                labels.set(i, getLabel(mvoi, oiRel));
            }
        }

        return labels;
    }

    private JoinType getLabel(Set<String> mvoi, OuterIncrementRelation oiRel) {
        if (!intersection(mvoi, oiRel.getRoiRelations()).isEmpty()) {
            mvoi.addAll(oiRel.getLoiRelations());
            return JoinType.LEFT;
        } else if (!intersection(mvoi, oiRel.getLoiRelations()).isEmpty()) {
            mvoi.addAll(oiRel.getRoiRelations());
            return JoinType.RIGHT;
        } else {
            return JoinType.INNER;
        }
    }

    private static<T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set set = new HashSet();
        set.addAll(set1);
        set.retainAll(set2);

        return set;
    }

    private OuterIncrementRelation getOuterIncrementRelation(Map<String, List<Column>> map, Join join) {
        Set<String> loirels = new HashSet<>();
        Set<String> roirels = new HashSet<>();

        List<Column> loiRelColumns = new ArrayList<>();
        List<Column> roiRelColumns = new ArrayList<>();

        for (String key : map.keySet()) {
            if (key.equals(join.getRightItem().toString().toLowerCase())) {
                loirels.add(key);
                loiRelColumns.addAll(map.get(key));
            } else {
                roirels.add(key);
                roiRelColumns.addAll(map.get(key));
            }
        }

        return new OuterIncrementRelation(loirels, roirels, loiRelColumns, roiRelColumns);
    }

    /**
     * Creates a generic shallow copy of the given join.
     * The join type is set to the default: JOIN.
     *
     * @param join The join that should be copied.
     * @return A generic shallow copy of join.
     */
    public static Join genericCopyOfJoin(Join join) {
        Join outJoin = new Join();
        outJoin.setRightItem(join.getRightItem());
        outJoin.setOnExpression(join.getOnExpression());

        return outJoin;
    }

    /**
     * Returns the concatenation of two expression. If either of them is null, the other expression is wrapped
     * in parentheses.
     *
     * @param left The left expression.
     * @param right The right expression.
     * @param wrapBinary True iff both sides should be wrapped in parentheses in case of a binary expression.
     * @return An and expression if both expressions are not null, otherwise, if either is null, the other is returned.
     */
    private Expression concatenate(Expression left, Expression right, boolean wrapBinary) {
        if (left == null) {
            return right;
        } else if (right != null) {
            if (wrapBinary) {
                return new AndExpression(wrapInParentheses(left), wrapInParentheses(right));
            }

            return new AndExpression(left, right);
        }

        return left;
    }

    /**
     * Wraps the given expression in parentheses if it is not wrapped yet.
     *
     * @param expression The expression to wrap.
     * @return Expression wrapped in parentheses.
     */
    private Expression wrapInParentheses(Expression expression) {
        if (expression == null || expression instanceof Parenthesis) {
            return expression;
        }

        return new Parenthesis(expression);
    }

    /**
     * Creates an expression that concatenates {@link IsNullExpression}s
     * containing a {@link Column} using a {@link BinaryExpression}.
     *
     * @param columns The columns that should be used in the concatenation.
     * @param isNull Determines whether the column should be checked for IS NULL or IS NOT NULL.
     * @return A concatenation of IsNull expressions that contains each of the given columns.
     */
    private static Expression createIsNullExpressions(Stack<Column> columns, boolean isNull) {
        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setNot(!isNull);
        Parenthesis parenthesis = new Parenthesis();

        if (columns.size() == 1) {
            isNullExpression.setLeftExpression(columns.pop());
            parenthesis.setExpression(isNullExpression);

            return parenthesis;
        } else if (!columns.isEmpty()) {
            isNullExpression.setLeftExpression(columns.pop());
            parenthesis.setExpression(isNullExpression);

            return new AndExpression(createIsNullExpressions(columns, isNull), parenthesis);
        }
        return null;
    }

    /**
     * Simplified version of the create is null expression method.
     * Rather than taking in a stack and an expression, only the columns are needed.
     *
     * @param columns The columns for which the is null expression have to be created.
     * @param isNull True if the columns should be IS NULL. False if the columns should be IS NOT NULL.
     * @return Returns the concatenated expressions.
     */
    private static Expression createIsNullExpressions(List<Column> columns, boolean isNull) {
        Stack<Column> stack = new Stack<>();
        stack.addAll(columns);
        return createIsNullExpressions(stack, isNull);
    }
}
