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
 * Custom Visitor for FROM statements and joins.
 */
public class RuleGeneratorFromVisitor extends FromItemVisitorAdapter {

    private List<Table> output = new ArrayList<>();

    public void visit(Table table) {
        System.out.println("Table: " + table.toString());
        output.add(table);

        for (Table s : output) {
            System.out.println(s.toString());
        }
    }

    public void visit(SubJoin subJoin) {

    }

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
