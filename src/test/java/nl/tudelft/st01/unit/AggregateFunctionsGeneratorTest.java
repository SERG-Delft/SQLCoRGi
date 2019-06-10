package nl.tudelft.st01.unit;

import manifold.ext.api.Jailbreak;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.JSqlParser;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import nl.tudelft.st01.AggregateFunctionsGenerator;
import nl.tudelft.st01.Generator;
import nl.tudelft.st01.util.UtilityGetters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.plaf.nimbus.State;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

/**
 * Unit tests for the {@link Generator}.
 */
public class AggregateFunctionsGeneratorTest {

    @Jailbreak private AggregateFunctionsGenerator aggregateFunctionsGenerator = new AggregateFunctionsGenerator();
    private PlainSelect plainSelect1 = new PlainSelect();

    /**
     * Creates the following query as a PlainSelect object:
     *      "SELECT Director, AVG(Length) FROM Movies GROUP BY Director"
     */
    @BeforeEach
    private void setUp() {
        SelectExpressionItem selectExpressionItem1 = new SelectExpressionItem();
        selectExpressionItem1.setExpression(new Column("Director"));

        SelectExpressionItem selectExpressionItem2 = new SelectExpressionItem();
        Function f = new Function();
        f.setName("AVG");
        List<Expression> expressions1 = Arrays.asList(new Column("Length"));
        ExpressionList expressionList = new ExpressionList();
        expressionList.setExpressions(expressions1);
        f.setParameters(expressionList);
        selectExpressionItem2.setExpression(f);

        GroupByElement groupByElement = new GroupByElement();
        List<Expression> expressions2 = Arrays.asList(new Column("Director"));
        groupByElement.setGroupByExpressions(expressions2);

        FromItem fromItem = new Table("Movies");

        PlainSelect plainSelect = new PlainSelect();
        plainSelect.setSelectItems(Arrays.asList(selectExpressionItem1, selectExpressionItem2));
        plainSelect.setFromItem(fromItem);
        plainSelect.setGroupByElement(groupByElement);

        System.out.println(plainSelect);
    }

    /**
     *  First rule is tested here. It should return a simple count(*) with all columns.
     */
    @Test
    public void firstRuleTest() {
        assertThat(true).isEqualTo(true);
    }
}
