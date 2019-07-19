package com.github.sergdelft.sqlcorgi.visitors.select;

import com.github.sergdelft.sqlcorgi.query.NumericValue;
import com.github.sergdelft.sqlcorgi.schema.TableStructure;
import com.github.sergdelft.sqlcorgi.schema.TypeChecker;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import com.github.sergdelft.sqlcorgi.query.NumericDoubleValue;
import com.github.sergdelft.sqlcorgi.query.NumericLongValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.sergdelft.sqlcorgi.util.Expressions.createEqualsTo;
import static com.github.sergdelft.sqlcorgi.util.cloner.ExpressionCloner.copy;

/**
 * A visitor for select expressions, i.e. {@code WHERE} and {@code HAVING} clauses in {@code SELECT} statements.
 */
public class SelectExpressionVisitor extends ExpressionVisitorAdapter {
    private List<Expression> output;

    /**
     * Creates a new visitor which can be used to generate mutations of select operators. Any rules that are
     * generated will be written to {@code output}.
     *
     * @param output the set to which generated rules should be written. This set must not be null, and must be empty.
     */
    public SelectExpressionVisitor(List<Expression> output) {
        if (output == null || !output.isEmpty()) {
            throw new IllegalArgumentException(
                "A SelectExpressionVisitor requires an empty, non-null set to which it can write generated expressions."
            );
        }

        this.output = output;
    }

//    /**
//     * Generates mutations for relational operators.
//     *
//     * @param comparisonOperator the relational operator to be mutated.
//     */
//    private void generateRelationalMutations(ComparisonOperator comparisonOperator) {
//
//        ArrayList<Expression> cases = new ArrayList<>();
//        Column column = (Column) comparisonOperator.getLeftExpression();
//        SelectValueVisitor valueVisitor = new SelectValueVisitor(column, cases);
//
//        comparisonOperator.getRightExpression().accept(valueVisitor);
//
//        output.addAll(cases);
//    }

    /**
     * Generates mutations for relational operators.
     *
     * @param comparisonOperator the relational operator to be mutated.
     */
    private void generateRelationalMutations(ComparisonOperator comparisonOperator) {
        List<Expression> output = new ArrayList<>();

        ColumnExtractor columnExtractor = new ColumnExtractor();
        comparisonOperator.getLeftExpression().accept(columnExtractor);
        Set<Column> columns = new HashSet<>(columnExtractor.getColumns());

        // TODO: check schema for nullable
        for (Column c : columns) {
            IsNullExpression isNullExpression = new IsNullExpression();
            isNullExpression.setLeftExpression(copy(c));
        }

        com.github.sergdelft.sqlcorgi.schema.Column.DataType type = checkTypes(comparisonOperator);

        switch (type) {
            case NUM: output.addAll(generateNumericCases(comparisonOperator));
                break;
            case STRING: output.addAll(generateStringCases(comparisonOperator));
                break;
            default: break;
        }

        this.output.addAll(output);

    }

    private List<Expression> generateNumericCases(BinaryExpression expression) {
        Expression right = expression.getRightExpression();
        Expression left = expression.getLeftExpression();

        List<Expression> rightExpressions = new ArrayList<>();
        List<Expression> output = new ArrayList<>();

        if (right instanceof NumericValue) {
            for (int i = -1; i <= 1; i++) {
                NumericValue e = (NumericValue) copy(right);
                e.add(i);
                rightExpressions.add(e);
            }
        } else {
            rightExpressions.add(copy(expression));
            rightExpressions.add(generateAddOffByOne(right));
            rightExpressions.add(generateSubOffByOne(right));
        }

        for (Expression e : rightExpressions) {
            EqualsTo eq = new EqualsTo();
            eq.setLeftExpression(left);
            eq.setRightExpression(e);
            output.add(eq);
        }

        return output;
    }

    private Expression generateAddOffByOne(Expression expression) {
        Addition addition = new Addition();
        addition.setLeftExpression(copy(expression));
        addition.setRightExpression(new NumericDoubleValue("1"));

        return addition;
    }

    private Expression generateSubOffByOne(Expression expression) {
        Subtraction subtraction = new Subtraction();
        subtraction.setLeftExpression(copy(expression));
        subtraction.setRightExpression(new NumericDoubleValue("1"));

        return subtraction;
    }


    // TODO: Check if not expression works well.
    private List<Expression> generateStringCases(BinaryExpression expression) {
        List<Expression> output = new ArrayList<>();
        output.add(new NotExpression(copy(expression)));
        output.add(copy(expression));

        return output;
    }


    private com.github.sergdelft.sqlcorgi.schema.Column.DataType checkTypes(Expression expression) {
        TypeChecker typeChecker = new TypeChecker(new TableStructure());
        expression.accept(typeChecker);

        return typeChecker.getType();
    }

    /**
     * Generates mutations for conditions containing {@link OrExpression}s and {@link AndExpression}s.
     *
     * @param expression an {@link OrExpression} or {@link AndExpression}.
     */
    private void generateCompoundMutations(BinaryExpression expression) {

        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        List<Expression> leftOut = new ArrayList<>();
        List<Expression> rightOut = new ArrayList<>();
        List<Expression> temp = this.output;

        this.output = leftOut;
        left.accept(this);

        this.output = rightOut;
        right.accept(this);

        this.output = temp;

        Expression neutralExpression = right instanceof Parenthesis ? right : new Parenthesis(right);
        if (expression instanceof OrExpression) {
            neutralExpression = new NotExpression(neutralExpression);
        }
        for (Expression decisionExpression : leftOut) {
            this.output.add(copy(new AndExpression(new Parenthesis(decisionExpression), neutralExpression)));
        }

        neutralExpression = left instanceof Parenthesis ? left : new Parenthesis(left);
        if (expression instanceof OrExpression) {
            neutralExpression = new NotExpression(neutralExpression);
        }
        for (Expression decisionExpression : rightOut) {
            this.output.add(copy(new AndExpression(neutralExpression, new Parenthesis(decisionExpression))));
        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        generateCompoundMutations(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        generateCompoundMutations(orExpression);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        generateRelationalMutations(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        generateRelationalMutations(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        generateRelationalMutations(greaterThanEquals);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        output.add(copy(isNullExpression));

        IsNullExpression isNullExpressionToggled = (IsNullExpression) copy(isNullExpression);
        isNullExpressionToggled.setNot(!isNullExpressionToggled.isNot());
        output.add(isNullExpressionToggled);
    }

    @Override
    public void visit(MinorThan minorThan) {
        generateRelationalMutations(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        generateRelationalMutations(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        generateRelationalMutations(notEqualsTo);
    }

    /**
     * Generates test queries for 'BETWEEN' expressions.
     *
     * @param between a 'BETWEEN' expression.
     */
    @Override
    public void visit(Between between) {

        Expression left = between.getLeftExpression();
        Expression start = between.getBetweenExpressionStart();
        Expression end = between.getBetweenExpressionEnd();

        output.add(createEqualsTo(left, start));

        output.add(createEqualsTo(left, end));

        if (start instanceof LongValue) {
            NumericLongValue longValue = new NumericLongValue(start.toString());
            EqualsTo leftOffPoint = createEqualsTo(left, longValue.add(-1));
            output.add(leftOffPoint);
        } else if (start instanceof DoubleValue) {
            NumericDoubleValue doubleValue = new NumericDoubleValue(start.toString());
            EqualsTo leftOffPoint = createEqualsTo(left, doubleValue.add(-1));
            output.add(leftOffPoint);
        }

        if (end instanceof LongValue) {
            NumericLongValue longValue = new NumericLongValue(end.toString());
            EqualsTo rightOffPoint = createEqualsTo(left, longValue.add(1));
            output.add(rightOffPoint);
        } else if (end instanceof DoubleValue) {
            NumericDoubleValue doubleValue = new NumericDoubleValue(end.toString());
            EqualsTo rightOffPoint = createEqualsTo(left, doubleValue.add(1));
            output.add(rightOffPoint);
        }

        Between betweenNormal = (Between) copy(between);
        betweenNormal.setNot(false);
        output.add(betweenNormal);

        Between betweenFlipped = (Between) copy(between);
        betweenFlipped.setNot(true);
        output.add(betweenFlipped);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(copy(left));
        output.add(isNullExpression);
    }

    /**
     * Generates test queries for 'IN' expressions.
     *
     * @param inExpression an 'IN' expression.
     */
    @Override
    public void visit(InExpression inExpression) {

        output.add(copy(inExpression));

        InExpression inExpressionFlipped = (InExpression) copy(inExpression);
        inExpressionFlipped.setNot(!inExpressionFlipped.isNot());
        output.add(inExpressionFlipped);

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(copy(inExpression.getLeftExpression()));
        output.add(isNullExpression);
    }

    /**
     * Generates test queries for 'LIKE' expressions.
     *
     * @param likeExpression a LIKE expression.
     */
    @Override
    public void visit(LikeExpression likeExpression) {

        LikeExpression likeExpressionCopy = (LikeExpression) copy(likeExpression);
        likeExpressionCopy.setNot(false);
        output.add(likeExpressionCopy);

        LikeExpression notLikeExpression = (LikeExpression) copy(likeExpression);
        notLikeExpression.setNot(true);
        output.add(notLikeExpression);

        IsNullExpression isNullExpressionOut = new IsNullExpression();
        isNullExpressionOut.setLeftExpression(copy(likeExpression.getLeftExpression()));
        output.add(isNullExpressionOut);
    }

    @Override
    public void visit(SubSelect subSelect) {
        // Don't do anything here.
    }

}
