package nl.tudelft.st01;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Custom Visitor for SELECT statements.
 */
public class RuleGeneratorSelectVisitor extends SelectVisitorAdapter {

    //private List<PlainSelect> output;
    private Set<String> output;

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void visit(PlainSelect plainSelect) {

        if (output == null || !output.isEmpty()) {
            throw new IllegalArgumentException(
                "To use this visitor, you must first give it an empty list so it can pass along the generated queries."
            );
        }

        Expression where = plainSelect.getWhere();

        handleJoins(plainSelect);

        if (where != null) {
            RuleGeneratorExpressionVisitor ruleGeneratorExpressionVisitor = new RuleGeneratorExpressionVisitor();
            ArrayList<Expression> expressions = new ArrayList<>();
            ruleGeneratorExpressionVisitor.setOutput(expressions);
            where.accept(ruleGeneratorExpressionVisitor);

            for (Expression expression : expressions) {
                PlainSelect plainSelectOut = new PlainSelect();
                plainSelectOut.setSelectItems(plainSelect.getSelectItems());
                plainSelectOut.setFromItem(plainSelect.getFromItem());
                plainSelectOut.setWhere(expression);

                output.add(plainSelectOut.toString());
            }
        }

      // output = null;
    }

    public void setOutput(Set<String> output) {
        this.output = output;
    }

    //public void setOutputStrings(Set<String> outputStrings) { this.outputStrings = outputStrings; }

    /**
     *
     * @param plainSelect
     */
    public void handleJoins(PlainSelect plainSelect) {
        List<Join> joins = plainSelect.getJoins();
     //   Set<String> outJoins = new TreeSet<>();
        if (!(joins == null || joins.isEmpty())) {
            GenJoinWhereExpression genJoinWhereExpression = new GenJoinWhereExpression();
            Set<String> out = genJoinWhereExpression.generateJoinWhereExpressions(plainSelect);

            output.addAll(out);
        }


       // System.out.println(genJoinWhereExpression.generateJoinWhereExpressions(plainSelect));
        //return out;

       // return result;
//        RuleGeneratorFromVisitor ruleGeneratorFromVisitor = new RuleGeneratorFromVisitor();
//        fromItem.accept(ruleGeneratorFromVisitor);
//
//        for (Join j : joins) {
//            j.getRightItem().accept(ruleGeneratorFromVisitor);
//        }
//
//        Expression on = joins.get(0).getOnExpression();
//
//        RuleGeneratorOnExpressionVisitor ruleGeneratorOnExpressionVisitor = new RuleGeneratorOnExpressionVisitor();
//        HashMap<String, Expression> output = new HashMap<>();
//        ruleGeneratorOnExpressionVisitor.setOutput(output);
//
//        on.accept(ruleGeneratorOnExpressionVisitor);

    }
}
