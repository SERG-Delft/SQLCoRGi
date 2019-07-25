package com.github.sergdelft.sqlcorgi;

import com.github.sergdelft.sqlcorgi.query.JoinWhereItem;
import com.github.sergdelft.sqlcorgi.query.OuterIncrementRelation;
import com.github.sergdelft.sqlcorgi.schema.Schema;
import com.github.sergdelft.sqlcorgi.schema.TableStructure;
import com.github.sergdelft.sqlcorgi.visitors.ExpressionTraverserVisitor;
import com.github.sergdelft.sqlcorgi.visitors.join.ImplicitInnerJoinDeducer;
import com.github.sergdelft.sqlcorgi.visitors.join.OnExpressionVisitor;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import static com.github.sergdelft.sqlcorgi.util.cloner.SelectCloner.copy;

/**
 * This class allows for mutating a given query such that a set of mutated queries is returned.
 */
public class JoinRulesGenerator {
    private enum JoinType {
        LEFT, RIGHT, SIMPLE, INNER
    }

    private enum OIREmpty {
        LEFT, RIGHT, NONE
    }

    private Map<Integer, OuterIncrementRelation> outerIncrementRelations;
    private List<OIREmpty> tablesInOnExpression = new ArrayList<>();
    private FromItem fromItem;
    private PlainSelect plainSelect;
    private Set<String> simple;
    private PlainSelect sanitized;
    private TableStructure tableStructure;

    /**
     * Takes in a statement and mutates the joins. Each join will have its own set of mutations added to the results.
     *
     * @param plainSelect The statement for which the joins have to be mutated.
     * @param schema The schema related to the input query.
     * @return A set of mutated queries in string format.
     */
    public Set<String> generate(PlainSelect plainSelect, Schema schema) {
        List<Join> joins = plainSelect.getJoins();
        tableStructure = new TableStructure();
        Set<String> result = new TreeSet<>();
        simple = new HashSet<>();
        fromItem = plainSelect.getFromItem();
        this.plainSelect = plainSelect;

        if (joins == null || joins.isEmpty()) {
            return new HashSet<>();
        }


        for (Join j : joins) {
            if (j.isSimple()) {
                simple.add(j.getRightItem().toString().toLowerCase());
                simple.add(fromItem.toString().toLowerCase());
            }
        }

        Expression where = plainSelect.getWhere();

        implicitInnerJoinDeduction(joins, where);
        sanitized = (PlainSelect) copy(plainSelect);

        outerIncrementRelations = generateOIRsForEachJoin(plainSelect.getJoins());

        if (!outerIncrementRelations.isEmpty()) {
            Set<JoinWhereItem> items = handleJoins(this.plainSelect);
            for (JoinWhereItem j : items) {
                plainSelect.setJoins(j.getJoins());
                plainSelect.setWhere(j.getJoinWhere());
                result.add(plainSelect.toString());
            }
        }

        return result;
    }

    /**
     * Deduces whether an implicit inner join is present in the input. The where and joins list are
     * updated accordingly.
     * @param joins The list of joins in the from clause.
     * @param where The expression in the where clause.
     */
    private void implicitInnerJoinDeduction(List<Join> joins, Expression where) {
        Expression expression = where;

        LinkedList<String> order = new LinkedList<>();
        order.add(fromItem.toString().toLowerCase());
        for (Join j : joins) {
            order.add(j.getRightItem().toString().toLowerCase());
        }

        if (where != null) {
            for (Join j : joins) {
                if (j.isSimple()) {
                    ImplicitInnerJoinDeducer deducer = new ImplicitInnerJoinDeducer(j, fromItem, joins, order, simple);
                    if (expression != null) {
                        expression.accept(deducer);
                        expression = deducer.getExpression();
                    }
                }
            }

            List<Join> orderedJoins = new LinkedList<>();
            for (String s : order) {
                for (Join j : joins) {
                    if (s.equals(j.getRightItem().toString().toLowerCase())) {
                        orderedJoins.add(j);
                    }
                }

            }

            plainSelect.setWhere(expression);
            plainSelect.setJoins(orderedJoins);

        }
    }


    /**
     * Takes in the list of joins and determines the outer increment relations (OIR) for each of them,
     * this is stored in an {@link OuterIncrementRelation} object.
     *
     * @param joins The joins for which the OIRs have to be determined.
     * @return A list of OIRs. The order of the input joins is the same as the order of the list of OIRs.
     */
    private Map<Integer, OuterIncrementRelation> generateOIRsForEachJoin(List<Join> joins) {
        Map<String, List<Column>> map = new HashMap<>();

        if (outerIncrementRelations == null) {
            outerIncrementRelations = new HashMap<>();
        }

        OnExpressionVisitor onExpressionVisitor = new OnExpressionVisitor(map);
        for (int i = 0; i < joins.size(); i++) {
            Join join = joins.get(i);
            if (joins.get(i).getOnExpression() != null) {
                join.getOnExpression().accept(onExpressionVisitor);
                outerIncrementRelations.put(i, getOuterIncrementRelation(map, join));
                map.clear();
            } else if (!join.isSimple()) {
                throw new IllegalStateException("The ON condition cannot be null");
            } else {
                tablesInOnExpression.add(OIREmpty.NONE);
            }
        }

        return outerIncrementRelations;
    }

    /**
     * Takes in a plainselect and derives the correct list of joins and its respective outer increment.
     * These are stored in {@link JoinWhereItem}s.
     *
     * @param plainSelect The plainselect for which the rules should be derived from.
     * @return A set of {@link JoinWhereItem}s from which the actual rules can be derived.
     */
    // This warning is suppressed, because new objects have to be instantiated in the for loop.
    // Each object should be instantiated individually and should not have the same reference.
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
    private Set<JoinWhereItem> handleJoins(PlainSelect plainSelect) {
        List<Join> joins = plainSelect.getJoins();
        Expression whereCondition = plainSelect.getWhere();

        Set<JoinWhereItem> results = new HashSet<>();

        for (int i = 0; i < joins.size(); i++) {
            OuterIncrementRelation oir = outerIncrementRelations.get(i);
            if (oir != null) {
                if (oir.getLoiRelColumns() != null && !oir.getLoiRelColumns().isEmpty()) {
                    results.addAll(generateLeftJoinRules(joins, i, whereCondition, oir));
                }

                if (oir.getRoiRelColumns() != null && !oir.getRoiRelColumns().isEmpty()) {
                    results.addAll(generateRightJoinRules(joins, i, whereCondition, oir));
                    results.add(new JoinWhereItem(setAllToInner(joins), wrapInParentheses(whereCondition)));
                }
            }
        }

        plainSelect.setWhere(whereCondition);
        plainSelect.setJoins(joins);

        return results;
    }

    /**
     * Generates the {@link JoinWhereItem}s for the given list of transformed joins in case the
     * transformed join in question is a left join.
     *
     * @param joins The list of transformed joins.
     * @param index The index of the transformed join in question.
     * @param where The where expression of the original {@link PlainSelect}
     * @param oir The {@link OuterIncrementRelation} corresponding to the join in question.
     * @return A set of {@link JoinWhereItem}s.
     */
    private Set<JoinWhereItem> generateLeftJoinRules(List<Join> joins, int index, Expression where,
                                                     OuterIncrementRelation oir) {
        List<JoinType> labelsLeft = label(JoinType.LEFT, joins, index, outerIncrementRelations);
        Set<JoinWhereItem> out = new HashSet<>();
        List<Join> tJoinsLoi = transformJoins(joins, labelsLeft);

        List<OuterIncrementRelation> includes = getAllIncludesInner(labelsLeft, outerIncrementRelations);

        Expression reducedWhereLoi = nullReduction(where, JoinType.LEFT, oir, includes, false);
        Expression reducedWhereLoiNull = nullReduction(where,  JoinType.LEFT, oir, includes, true);

        if (tablesInOnExpression.get(index) == OIREmpty.RIGHT) {
            Join join = genericCopyOfJoin(tJoinsLoi.get(index));
            join.setRight(true);

            labelsLeft = label(JoinType.RIGHT, joins, index, outerIncrementRelations);
            tJoinsLoi = transformJoins(joins, labelsLeft);
            tJoinsLoi.set(index, join);

            Expression not = new NotExpression(new Parenthesis(join.getOnExpression()));
            out.add(new JoinWhereItem(tJoinsLoi, concatenate(concatenate(not,
                    getRightOuterIncrement(oir, false), true), reducedWhereLoi, true)));
        } else {
            Expression loi = getLeftOuterIncrement(oir, false);
            Expression loiNull = getLeftOuterIncrement(oir, true);

            out.add(new JoinWhereItem(tJoinsLoi, concatenate(loi, reducedWhereLoi, true)));
            // TODO check if key columns are nullable.
            if (!getNullableColumns(oir.getLoiRelColumns()).isEmpty()) {
                out.add(new JoinWhereItem(tJoinsLoi, concatenate(loiNull, reducedWhereLoiNull, true)));
            }

        }

        return out;
    }

    private List<Column> getNullableColumns(List<Column> columns) {
        List<Column> res = new ArrayList<>();
        for (Column c : columns) {
            if (tableStructure.getColumn(c.getName(false)).isNullable()) {
                res.add(c);
            }
        }

        return res;
    }

    /**
     * Generates the {@link JoinWhereItem}s for the given list of transformed joins in case the
     * transformed join in question is a right join.
     *
     * @param joins The list of transformed joins.
     * @param index The index of the transformed join in question.
     * @param where The where expression of the original {@link PlainSelect}
     * @param oir The {@link OuterIncrementRelation} corresponding to the join in question.
     * @return A set of {@link JoinWhereItem}s.
     */
    private Set<JoinWhereItem> generateRightJoinRules(List<Join> joins, int index, Expression where,
                                                     OuterIncrementRelation oir) {
        List<JoinType> labelsRight = label(JoinType.RIGHT, joins, index, outerIncrementRelations);
        Set<JoinWhereItem> out = new HashSet<>();

        List<Join> tJoinsRoi = transformJoins(joins, labelsRight);

        List<OuterIncrementRelation> includes = getAllIncludesInner(labelsRight, outerIncrementRelations);

        Expression reducedWhereRoi = nullReduction(where, JoinType.RIGHT, oir, includes, false);
        Expression reducedWhereRoiNull = nullReduction(where,  JoinType.RIGHT, oir, includes, true);

        if (tablesInOnExpression.get(index) == OIREmpty.LEFT) {
            Join join = genericCopyOfJoin(tJoinsRoi.get(index));
            join.setLeft(true);

            labelsRight = label(JoinType.LEFT, joins, index, outerIncrementRelations);
            tJoinsRoi = transformJoins(joins, labelsRight);
            tJoinsRoi.set(index, join);

            Expression not = new NotExpression(new Parenthesis(join.getOnExpression()));
            out.add(new JoinWhereItem(tJoinsRoi, concatenate(concatenate(not,
                    getLeftOuterIncrement(oir, false), true), reducedWhereRoi, true)));
        } else {
            Expression roi = getRightOuterIncrement(oir, false);
            Expression roiNull = getRightOuterIncrement(oir, true);

            out.add(new JoinWhereItem(tJoinsRoi, concatenate(roi, reducedWhereRoi, true)));
            // TODO check if key columns are nullable.
            if (!getNullableColumns(oir.getRoiRelColumns()).isEmpty()) {
                out.add(new JoinWhereItem(tJoinsRoi, concatenate(roiNull, reducedWhereRoiNull, true)));
            }
        }

        return out;
    }

    /**
     * In case the labelled join type is INNER, the {@link OuterIncrementRelation} must be included.
     *
     * @param labels The label for each join.
     * @param oirs The list of {@link OuterIncrementRelation}s corresponding to the joins.
     * @return A list of OIRs that must be included.
     */
    private List<OuterIncrementRelation> getAllIncludesInner(List<JoinType> labels, Map<Integer,
            OuterIncrementRelation> oirs) {
        List<OuterIncrementRelation> includes = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i) == JoinType.INNER) {
                includes.add(oirs.get(i));
            }
        }

        return includes;
    }

    /**
     * Takes in a list of joins and sets each join's type to inner only.
     *
     * @param joins The joins for which the type has to be set to inner.
     * @return A list of joins set to inner.
     */
    private List<Join> setAllToInner(List<Join> joins) {
        List<Join> outJoins = new ArrayList<>();
        for (Join join : joins) {
            if (!join.isSimple()) {
                outJoins.add(setJoinType(genericCopyOfJoin(join), JoinType.INNER));
            } else {
                outJoins.add(setJoinType(genericCopyOfJoin(join), JoinType.SIMPLE));
            }
        }

        return outJoins;
    }

    /**
     * Reduced the given expression such that it no longer contains any columns that should be excluded.
     * (E.g. in most cases of an IS NULL expression)
     *
     * @param expression The expression to reduce.
     * @param joinType The type of the join.
     * @param oir The {@link OuterIncrementRelation} corresponding to the join that has to be evaluated.
     * @param oirs The list of {@link OuterIncrementRelation}s that should be included when the expression is reduced.
     * @param nullable True if the outer increment relations are nullable, false otherwise.
     * @return The reduced expression.
     */

    private Expression nullReduction(Expression expression, JoinType joinType, OuterIncrementRelation oir,
                                            List<OuterIncrementRelation> oirs, boolean nullable) {
        if (expression != null) {
            Set<String> includeTables = new HashSet<>();
            List<Column> columns = new ArrayList<>();

            includeTables.addAll(simple);
            for (OuterIncrementRelation o : oirs) {
                includeTables.addAll(o.getLoiRelations());
                includeTables.addAll(o.getRoiRelations());
            }

            if (joinType == JoinType.LEFT) {
                includeTables.addAll(oir.getRoiRelations());
                columns.addAll(oir.getRoiRelColumns());
            } else if (joinType == JoinType.RIGHT) {
                includeTables.addAll(oir.getLoiRelations());
                columns.addAll(oir.getLoiRelColumns());
            } else {
                throw new IllegalArgumentException("Join type must be either LEFT or RIGHT");
            }

            ExpressionTraverserVisitor traverserVisitor = new ExpressionTraverserVisitor();
            traverserVisitor.setTables(includeTables);
            traverserVisitor.setOnColumns(columns);


            if (nullable) {
                // TODO: Check schema for all null columns
                traverserVisitor.setNullColumns(getNullableColumns(columns));
            }

            expression.accept(traverserVisitor);

            return traverserVisitor.getExpression();
        }

        return null;
    }

    /**
     * Takes in the outer increment relation and creates its left outer increment.
     *
     * @param oiRel The outer increment relation from which the outer increment should be derived.
     * @param nullable True if the right outer increment relations are nullable, false otherwise.
     * @return The left outer increment.
     */
    // TODO: boolean nullable should only be replaced by the nullable columns in the roicolumns.
    private Expression getLeftOuterIncrement(OuterIncrementRelation oiRel, boolean nullable) {
        List<Column> loiColumns = oiRel.getLoiRelColumns();
        List<Column> roiColumns;
        if (nullable) {
            roiColumns = getNullableColumns(oiRel.getRoiRelColumns());
        } else {
            roiColumns = oiRel.getRoiRelColumns();
        }
        return concatenate(
                createIsNullExpressions(loiColumns, true),
                createIsNullExpressions(roiColumns, nullable), false);
    }

    /**
     * Takes in the outer increment relation and creates its right outer increment.
     *
     * @param oiRel The outer increment relation from which the outer increment should be derived.
     * @param nullable True if the left outer increment relations are nullable, false otherwise.
     * @return The right outer increment.
     */
    // TODO: boolean nullable should only be replaced by the nullable columns in the loicolumns.
    private Expression getRightOuterIncrement(OuterIncrementRelation oiRel, boolean nullable) {
        List<Column> loiColumns;
        List<Column> roiColumns = oiRel.getRoiRelColumns();

        if (nullable) {
            loiColumns = getNullableColumns(oiRel.getLoiRelColumns());
        } else {
            loiColumns = oiRel.getLoiRelColumns();
        }
        return concatenate(
                createIsNullExpressions(getNullableColumns(roiColumns), true),
                createIsNullExpressions(loiColumns, nullable), false);
    }

    /**
     * Transform the given list of joins such that they are transformed into the join types in label.
     *
     * @param joins The joins to be transformed.
     * @param labels The types the joins should be transformed into.
     * @return The list of transformed joins.
     */
    private List<Join> transformJoins(List<Join> joins, List<JoinType> labels) {
        if (labels.size() != joins.size()) {
            throw new IllegalStateException("The size of the list of joins must be "
                    + "equal to the size of the list of labels");
        }

        Iterator<Join> joinIterator = joins.iterator();
        Iterator<JoinType> joinTypeIterator = labels.iterator();

        List<Join> transformedJoins = new ArrayList<>();

        while (joinIterator.hasNext() && joinTypeIterator.hasNext()) {
            Join tJoin = transformJoin(joinIterator.next(), joinTypeIterator.next());
            transformedJoins.add(tJoin);
        }

        return transformedJoins;
    }

    /**
     * Transforms a single join into the given type.
     *
     * @param join The join to transform.
     * @param joinType The type the join should be transformed into.
     * @return The transformed join.
     */
    private Join transformJoin(Join join, JoinType joinType) {
        return setJoinType(genericCopyOfJoin(join), joinType);
    }

    /**
     * Set the join to the given type.
     *
     * @param join The join for which the type should be set.
     * @param joinType The join type to which the join should be set.
     * @return A join with the given type.
     */
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
            case SIMPLE:
                join.setSimple(true);
                break;
            default:
                throw new IllegalArgumentException("Invalid Join type");
        }

        return join;
    }

    /**
     * Labels all joins in the list. The types are set such that the impossible combinations are excluded.
     *
     * @param joinType The join type of the current join to inspect.
     * @param joins The list of all joins.
     * @param index The index of the current join.
     * @param oirs The list of {@link OuterIncrementRelation}s used to label the joins.
     * @return A list of join types, which are used to make sure that all joins are configured correctly.
     */
    private List<JoinType> label(JoinType joinType, List<Join> joins, int index, Map<Integer,
            OuterIncrementRelation> oirs) {
        if (index >= joins.size()) {
            throw new IllegalArgumentException("The index cannot be larger than the size of the given list of joins.");
        }

        Set<String> mvoi = new HashSet<>();
        List<JoinType> labels = Arrays.asList(new JoinType[joins.size()]);

        OuterIncrementRelation currOiRel = oirs.get(index);

        switch (joinType) {
            case LEFT:  mvoi.addAll(currOiRel.getLoiRelations());
                break;
            case RIGHT: mvoi.addAll(currOiRel.getRoiRelations());
                break;
            default: throw new IllegalArgumentException("The join type must be either LEFT or RIGHT");
        }

        labels.set(index, joinType);

        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i) == null) {
                OuterIncrementRelation oiRel = outerIncrementRelations.get(i);
                labels.set(i, determineLabel(mvoi, oiRel, joinType));
            }
        }

        return labels;
    }

    /**
     * Determines the label of the join based on its outer increment relations.
     *
     * @param mvoi The list, MissingValues outer increments.
     * @param oiRel The outer increment corresponding to the join for the label should be set.
     * @param joinType RIGHT if the roirel should be set, LEFT if the loirel should be set.
     * @return The correct join type.
     */
    private JoinType determineLabel(Set<String> mvoi, OuterIncrementRelation oiRel, JoinType joinType) {
        if (oiRel != null) {
            if (joinType == JoinType.RIGHT) {
                if (!intersection(mvoi, oiRel.getRoiRelations()).isEmpty()) {
                    mvoi.addAll(oiRel.getLoiRelations());
                    return JoinType.LEFT;
                } else if (!intersection(mvoi, oiRel.getLoiRelations()).isEmpty()) {
                    mvoi.addAll(oiRel.getRoiRelations());
                    return JoinType.RIGHT;
                }
            } else if (joinType == JoinType.LEFT) {
                if (!intersection(mvoi, oiRel.getLoiRelations()).isEmpty()) {
                    mvoi.addAll(oiRel.getRoiRelations());
                    return JoinType.RIGHT;
                } else if (!intersection(mvoi, oiRel.getRoiRelations()).isEmpty()) {
                    mvoi.addAll(oiRel.getLoiRelations());
                    return JoinType.LEFT;
                }
            }
            return JoinType.INNER;
        }

        return JoinType.SIMPLE;

    }

    /**
     * Determines the intersection of the two given set and returns it without altering any of the given sets.
     *
     * @param set1 The first set.
     * @param set2 Yhe second set.
     * @param <T> Generic type to ensure that both sets contain elements of the same type.
     * @return The intersection of the sets.
     */
    // This warning is suppressed due to contradicting checkstyle warnings.
    // Neither with nor without a white space is accepted between <T> and static.
    @SuppressWarnings("checkstyle:nowhitespacebefore")
    private static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> set = new HashSet<>(set1);
        set.retainAll(set2);

        return set;
    }

    /**
     * Determines the outer increment relation given the join and the tables and columns extracted from it.
     *
     * @param map The map containing the tables mapped to the columns.
     * @param join The join from which the map is derived.
     * @return The outer increment relation derived from the map and join.
     */
    private OuterIncrementRelation getOuterIncrementRelation(Map<String, List<Column>> map, Join join) {
        Set<String> loirels = new HashSet<>();
        Set<String> roirels = new HashSet<>();

        List<Column> loiRelColumns = new ArrayList<>();
        List<Column> roiRelColumns = new ArrayList<>();

        for (Map.Entry<String, List<Column>> entry : map.entrySet()) {
            if (entry.getKey().equals(join.getRightItem().toString().toLowerCase())) {
                loirels.add(entry.getKey());
                loiRelColumns.addAll(entry.getValue());
            } else {
                roirels.add(entry.getKey());
                roiRelColumns.addAll(entry.getValue());
            }
        }

        if (roirels.isEmpty()) {
            roirels.add(fromItem.toString());

            roirels.addAll(oirsUnion());
            roirels.removeAll(intersection(roirels, loirels));
            tablesInOnExpression.add(OIREmpty.RIGHT);
        } else if (loirels.isEmpty()) {
            loirels.add(join.getRightItem().toString());

            loirels.addAll(oirsUnion());
            loirels.removeAll(intersection(roirels, loirels));
            tablesInOnExpression.add(OIREmpty.LEFT);
        } else {
            tablesInOnExpression.add(OIREmpty.NONE);
        }

        return new OuterIncrementRelation(loirels, roirels, loiRelColumns, roiRelColumns);
    }

    private Set<String> oirsUnion() {
        Set<String> out = new HashSet<>();
        if (outerIncrementRelations != null && !outerIncrementRelations.isEmpty()) {
            for (Map.Entry<Integer, OuterIncrementRelation> entry : outerIncrementRelations.entrySet()) {
                out.addAll(entry.getValue().getLoiRelations());
                out.addAll(entry.getValue().getRoiRelations());
            }
        }

        return out;
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

    public PlainSelect getSanitized() {
        return sanitized;
    }
}
