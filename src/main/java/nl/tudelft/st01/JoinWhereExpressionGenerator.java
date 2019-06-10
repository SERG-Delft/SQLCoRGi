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
import nl.tudelft.st01.query.JoinOnConditionColumns;
import nl.tudelft.st01.query.JoinWhereItem;
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
public class JoinWhereExpressionGenerator {
    private enum JoinType {
        LEFT, RIGHT, INNER
    }

    private Map<String, List<Column>> map;

    private Expression whereExpression;

    private List<OuterIncrementRelation> outerIncrementRelations;

    /**
     * Takes in a statement and mutates the joins. Each join will have its own set of mutations added to the results.
     *
     * @param plainSelect The statement for which the joins have to be mutated.
     * @return A set of mutated queries in string format.
     */
    public Set<String> generateJoinWhereExpressions(PlainSelect plainSelect) {
//        generate(plainSelect);
//        this.whereExpression = plainSelect.getWhere();
        Set<String> result = generate(plainSelect);

//
//        List<Join> joins = plainSelect.getJoins();
//        List<JoinWhereItem> joinWhereItems;
//        Join join;
//
//        PlainSelect out = plainSelect;
//        Expression whereCondition = plainSelect.getWhere();
//
//        if (joins != null && !joins.isEmpty()) {
//            map = new HashMap<>();
//            OnExpressionVisitor onExpressionVisitor = new OnExpressionVisitor(map);
//
//            List<Join> temp = new ArrayList<>();
//            for (int i = 0; i < joins.size(); i++) {
//                join = joins.get(i);
//
//                if (join.isSimple()) {
//                    continue;
//                } else if (join.getOnExpression() == null) {
//                    throw new IllegalStateException("The ON condition cannot be null");
//                }
//
//                join.getOnExpression().accept(onExpressionVisitor);
//                joinWhereItems = generateJoinMutations(join);
//                for (JoinWhereItem joinWhereItem : joinWhereItems) {
//                    temp.addAll(joins);
//                    temp.set(i, joinWhereItem.getJoin());
//
//                    out.setJoins(temp);
//                    out.setWhere(joinWhereItem.getJoinWhere());
//
//                    result.add(out.toString());
//                    out.setWhere(whereCondition);
//                    out.setJoins(joins);
//                    temp.clear();
//                }
//                map.clear();
//            }
//        }
//
//        plainSelect.setWhere(whereCondition);
//        plainSelect.setJoins(joins);
        return result;
    }

    public Set<String> generate(PlainSelect plainSelect) {
        List<Join> joins = plainSelect.getJoins();
        Expression where = plainSelect.getWhere();
        Set<String> result = new TreeSet<>();
        if (joins == null || joins.isEmpty()) {
            return new HashSet<>();
        }

        outerIncrementRelations = generateOIRsForEachJoin(plainSelect.getJoins());

        if (!outerIncrementRelations.isEmpty()) {
            List<JoinWhereItem> items = handleJoins(plainSelect);
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

    private List<JoinWhereItem> handleJoins(PlainSelect plainSelect) {
        List<Join> joins = plainSelect.getJoins();
        Expression whereCondition = plainSelect.getWhere();

        List<JoinWhereItem> results = new ArrayList<>();

        List<JoinType> labels;
        for (int i = 0; i < joins.size(); i++) {
            // TODO: differentiate between multitable and singletable on expressions.
            OuterIncrementRelation oir = outerIncrementRelations.get(i);

            labels = label(JoinType.LEFT, joins, i);
            List<Join> tJoinsLoi = transformJoins(joins, labels);

            Expression loi = getLeftOuterIncrement(oir, false);
            Expression loiNull = getLeftOuterIncrement(oir, true);
            Expression reducedWhereLoi = nullReduction(whereCondition, oir, JoinType.LEFT, false);
            Expression reducedWhereLoiNull = nullReduction(whereCondition, oir, JoinType.LEFT, true);

            results.add(new JoinWhereItem(tJoinsLoi, concatenate(loi, reducedWhereLoi, true)));
            results.add(new JoinWhereItem(tJoinsLoi, concatenate(loiNull, reducedWhereLoiNull, true)));

            labels = label(JoinType.RIGHT, joins, i);
            List<Join> tJoinsRoi = transformJoins(joins, labels);

            Expression roi = getRightOuterIncrement(oir, false);
            Expression roiNull = getRightOuterIncrement(oir, true);
            Expression reducedWhereRoi = nullReduction(whereCondition, oir, JoinType.RIGHT, false);
            Expression reducedWhereRoiNull = nullReduction(whereCondition, oir, JoinType.RIGHT, true);

            results.add(new JoinWhereItem(tJoinsRoi, concatenate(roi, reducedWhereRoi, true)));
            results.add(new JoinWhereItem(tJoinsRoi, concatenate(roiNull, reducedWhereRoiNull, true)));
        }

        plainSelect.setWhere(whereCondition);
        plainSelect.setJoins(joins);

        results.add(new JoinWhereItem(setAllToInner(joins), wrapInParentheses(whereCondition)));

        return results;
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

        return new AndExpression(
                createIsNullExpressions(loiColumns, true),
                createIsNullExpressions(roiColumns, nullable));
    }

    private Expression getRightOuterIncrement(OuterIncrementRelation oiRel, boolean nullable) {
        List<Column> loiColumns = oiRel.getLoiRelColumns();
        List<Column> roiColumns = oiRel.getRoiRelColumns();
        return new AndExpression(
                createIsNullExpressions(roiColumns, true),
                createIsNullExpressions(loiColumns, nullable));
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
     * Mutates the given {@link Join} such that it returns a list of {@link JoinWhereItem}s.
     *
     * @param join The join that should be mutated.
     * @return A list of mutated joins and their corresponding where expressions.
     */
    private List<JoinWhereItem> generateJoinMutations(Join join) {
        List<JoinWhereItem> result;

        if (map.size() == 1) {
            result = handleJoinSingleTableOnCondition(join, map);
        } else {
            result = handleJoinMultipleTablesOnCondition(join, map);
        }
        return result;
    }

    /**
     * When the map contains more than one table, then generate to corresponding mutations.
     *
     * @param join The join that has to be mutated.
     * @param map The map where each table is mapped to its columns.
     * @return List of JoinWhere items with the mutated results.
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
    private List<JoinWhereItem> handleJoinMultipleTablesOnCondition(Join join, Map<String, List<Column>> map) {
        JoinOnConditionColumns joinOnConditionColumns = new JoinOnConditionColumns();

        for (Map.Entry<String, List<Column>> s : map.entrySet()) {
            List<Column> columns = map.get(s.getKey());

            if (!s.getKey().equals(join.getRightItem().toString().toLowerCase())) {
                joinOnConditionColumns.addToLeftColumns(columns);
            } else {
                joinOnConditionColumns.addToRightColumns(columns);
            }
        }

        return generateJoinWhereItems(joinOnConditionColumns, join);
    }

    /**
     * Generates the join where items for the join on condition columns provided.
     *
     * @param joinOnConditionColumns The object containing the columns.
     * @param join The join used.
     * @return List of a generated JoinWhereItem for each mutation
     */
    private List<JoinWhereItem> generateJoinWhereItems(JoinOnConditionColumns joinOnConditionColumns, Join join) {
        Expression where = whereExpression;

        Join leftJoin = genericCopyOfJoin(join);
        leftJoin.setLeft(true);
        Join rightJoin = genericCopyOfJoin(join);
        rightJoin.setRight(true);
        Join innerJoin = genericCopyOfJoin(join);
        innerJoin.setInner(true);

        List<Column> left = joinOnConditionColumns.getLeftColumns();
        Set<String> leftTables = joinOnConditionColumns.getLeftTables();

        List<Column> right = joinOnConditionColumns.getRightColumns();
        Set<String> rightTables = joinOnConditionColumns.getRightTables();

        Expression leftColumnsIsNull = createIsNullExpressions(left, true);
        Expression leftColumnsIsNotNull = createIsNullExpressions(left, false);

        Expression rightColumnsIsNull = createIsNullExpressions(right, true);
        Expression rightColumnsIsNotNull = createIsNullExpressions(right, false);

        Expression roi = concatenate(leftColumnsIsNull, rightColumnsIsNotNull, false);
        Expression roiNull = concatenate(leftColumnsIsNull, rightColumnsIsNull, false);

        Expression loi = concatenate(rightColumnsIsNull, leftColumnsIsNotNull, false);
        Expression loiNull = concatenate(rightColumnsIsNull, leftColumnsIsNull, false);

        Expression rightIsNull = excludeInExpression(right, leftTables, where);
        Expression rightIsNotNull = excludeInExpression(leftTables, where);

        Expression leftIsNull = excludeInExpression(left, rightTables, where);
        Expression leftIsNotNull = excludeInExpression(rightTables, where);

        Expression roiJoinNull = concatenate(roiNull, rightIsNull, true);
        Expression roiJoin = concatenate(roi, rightIsNotNull, true);

        Expression loiJoinNull = concatenate(loiNull, leftIsNull, true);
        Expression loiJoin = concatenate(loi, leftIsNotNull, true);

        List<JoinWhereItem> result = new ArrayList<>();
        result.add(new JoinWhereItem(innerJoin, wrapInParentheses(whereExpression)));
        result.add(new JoinWhereItem(leftJoin, loiJoinNull));
        result.add(new JoinWhereItem(leftJoin, loiJoin));
        result.add(new JoinWhereItem(rightJoin, roiJoinNull));
        result.add(new JoinWhereItem(rightJoin, roiJoin));

        return result;
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
     * Modifies input expression such that it no longer contains any columns part of the table.
     *
     * @param tables The tables from which the columns have to be excluded.
     * @param expression The expression that should be modified.
     * @return The modified expression.
     */
    private static Expression excludeInExpression(Set<String> tables, Expression expression) {
        if (expression != null) {
            ExpressionTraverserVisitor traverserVisitor = new ExpressionTraverserVisitor();
            traverserVisitor.setTables(tables);
            expression.accept(traverserVisitor);

            return traverserVisitor.getExpression();
        }

        return null;
    }

    /**
     * Modifies input expression such that it no longer contains any columns part of the table.
     *
     * @param columns The columns that should be excluded.
     * @param tables The table from which the columns should be excluded.
     * @param expression The expression that should be modified.
     * @return The modified expression.
     */
    private static Expression excludeInExpression(List<Column> columns, Set<String> tables, Expression expression) {
        if (expression != null) {
            ExpressionTraverserVisitor traverserVisitor = new ExpressionTraverserVisitor();
            traverserVisitor.setTables(tables);
            traverserVisitor.setNullColumns(columns);
            expression.accept(traverserVisitor);

            return traverserVisitor.getExpression();
        }

        return null;
    }


    /**
     * In case the two tables are joined and the on condition only contains tables from one of the two tables,
     * then this method will generate the JoinWhereItems corresponding to the test cases that should be generated.
     *
     * @param join The join from which the JoinWhereitems have to be generated.
     * @param map The map where each table is mapped to its columns.
     * @return List of JoinWhereItems that contain the mutations.
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
    private static List<JoinWhereItem> handleJoinSingleTableOnCondition(Join join, Map<String, List<Column>> map) {
        if (map.size() > 1) {
            throw new IllegalArgumentException("Map may only contain columns from one table");
        }

        Expression isNotNulls;

        List<JoinWhereItem> result = new ArrayList<>();
        List<Column> columns;

        Join leftJoin = genericCopyOfJoin(join);
        leftJoin.setLeft(true);
        Join rightJoin = genericCopyOfJoin(join);
        rightJoin.setRight(true);
        Join innerJoin = genericCopyOfJoin(join);
        innerJoin.setInner(true);

        Expression coreExpression;
        AndExpression andExpression;
        Parenthesis left;
        NotExpression notExpression;
        Parenthesis parenthesis;

        for (Map.Entry<String, List<Column>> s : map.entrySet()) {
            columns = map.get(s.getKey());

            isNotNulls = createIsNullExpressions(columns, false);
            parenthesis = new Parenthesis();
            parenthesis.setExpression(join.getOnExpression());

            notExpression = new NotExpression(parenthesis);

            left = new Parenthesis();
            left.setExpression(notExpression);

            andExpression = new AndExpression(left, isNotNulls);

            if (s.getKey().equals(join.getRightItem().toString().toLowerCase())) {
                result.add(new JoinWhereItem(rightJoin, andExpression));
            } else {
                coreExpression = getCoreExpression(join.getOnExpression());

                Join useJoin;
                if (coreExpression instanceof IsNullExpression) {
                    useJoin = innerJoin;
                } else {
                    useJoin = leftJoin;
                }

                result.add(new JoinWhereItem(innerJoin, null));
                result.add(new JoinWhereItem(useJoin, andExpression));
            }
        }

        return result;
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

        throw new IllegalStateException("The columns list cannot be empty.");
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

    /**
     * If an expression is nested in parentheses or in a not expression, retrieve the innermost expression that is
     * not either of these.
     *
     * @param expression The expression to evaluate.
     * @return The innermost expression that is not nested in parenthesis or in a not.
     */
    public static Expression getCoreExpression(Expression expression) {
        if (expression instanceof Parenthesis) {
            return getCoreExpression(((Parenthesis) expression).getExpression());
        } else if (expression instanceof NotExpression) {
            return getCoreExpression(((NotExpression) expression).getExpression());
        } else {
            return expression;
        }
    }

}
