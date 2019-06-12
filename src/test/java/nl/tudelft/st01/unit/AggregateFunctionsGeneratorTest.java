package nl.tudelft.st01.unit;

import manifold.ext.api.Jailbreak;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import nl.tudelft.st01.AggregateFunctionsGenerator;
import nl.tudelft.st01.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link Generator}.
 */
public class AggregateFunctionsGeneratorTest {

    @Jailbreak private AggregateFunctionsGenerator aggregateFunctionsGenerator = new AggregateFunctionsGenerator();
    private PlainSelect plainSelect1;
    private Function function1;
    private static final String DIRECTOR = "Director";

    /**
     * Creates the following query as a PlainSelect object:
     *      "SELECT Director, AVG(NrOfVisitors) FROM Movies GROUP BY Director".
     */
    @BeforeEach
    // Since this setUp method has the @BeforeEach tag, it is definitely used (quite a lot, actually) but
    //      PMD doesn't seem to notice this. Thus, we have to suppress this rule here.
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void setUp() {
        plainSelect1 = new PlainSelect();

        // SELECT Director
        SelectExpressionItem selectExpressionItem1 = new SelectExpressionItem();
        selectExpressionItem1.setExpression(new Column(DIRECTOR));

        // AVG(NrOfVisitors)
        SelectExpressionItem selectExpressionItem2 = new SelectExpressionItem();
        function1 = new Function();
        function1.setName("AVG");
        List<Expression> expressions1 = Arrays.asList(new Column("NrOfVisitors"));
        ExpressionList expressionList = new ExpressionList();
        expressionList.setExpressions(expressions1);
        function1.setParameters(expressionList);
        selectExpressionItem2.setExpression(function1);

        plainSelect1.setSelectItems(Arrays.asList(selectExpressionItem1, selectExpressionItem2));

        // From Movies
        FromItem fromItem = new Table("Movies");
        plainSelect1.setFromItem(fromItem);

        // GROUP BY Director
        GroupByElement groupByElement = new GroupByElement();
        List<Expression> expressions2 = Arrays.asList(new Column(DIRECTOR));
        groupByElement.setGroupByExpressions(expressions2);
        plainSelect1.setGroupByElement(groupByElement);
    }

    /**
     *  First rule is tested here. It should return a simple count(*) with all columns.
     */
    @Test
    public void firstRuleTest() {
        assertThat(aggregateFunctionsGenerator.firstRule(plainSelect1).toString()).isEqualTo("SELECT COUNT(*) FROM "
            + "Movies HAVING COUNT(DISTINCT Director) > 1");
    }

    /**
     *  Second rule is tested here. It should return a query that checks if there are at least some
     *  tuples returned.
     */
    @Test
    public void secondRuleTest() {
        assertThat(aggregateFunctionsGenerator.secondRule(plainSelect1).toString()).isEqualTo("SELECT Director, "
                + "AVG(NrOfVisitors) FROM Movies GROUP BY Director HAVING COUNT(*) > 1");
    }

    /**
     *  Third rule is tested here. It should return a query aimed at the GROUP BY clause.
     */
    @Test
    public void thirdRuleTest() {
        assertThat(aggregateFunctionsGenerator.thirdRule(plainSelect1, function1).toString()).isEqualTo(
                "SELECT Director, AVG(NrOfVisitors) FROM Movies GROUP BY Director"
                + " HAVING COUNT(*) > COUNT(NrOfVisitors) AND COUNT(DISTINCT NrOfVisitors) > 1"
        );
    }

    /**
     *  Fourth rule is tested here. It should also return a query aimed at the GROUP BY clause.
     */
    @Test
    public void fourthRuleTest() {
        assertThat(aggregateFunctionsGenerator.fourthRule(plainSelect1, function1).toString()).isEqualTo(
                "SELECT Director, AVG(NrOfVisitors) FROM Movies GROUP BY Director "
                + "HAVING COUNT(NrOfVisitors) > COUNT(DISTINCT NrOfVisitors) AND COUNT(DISTINCT NrOfVisitors) > 1"
        );
    }
}
