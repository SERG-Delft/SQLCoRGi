package nl.tudelft.st01;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesisFromItem;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Visitor for FromItems.
 */
public class RuleGeneratorFromVisitor extends FromItemVisitorAdapter {

    private List<Table> output = new ArrayList<>();

    @Override
    public void visit(Table table) {
        output.add(table);

    }

    @Override
    public void visit(SubJoin subJoin) {

    }

    @Override
    public void visit(SubSelect subSelect) {

    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {

    }

    @Override
    public void visit(ValuesList valuesList) {

    }

    @Override
    public void visit(TableFunction tableFunction) {

    }

    @Override
    public void visit(ParenthesisFromItem parenthesisFromItem) {

    }


}
