package nl.tudelft.st01.unit;

import nl.tudelft.st01.sqlfpcws.SQLFpcWS;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.Assumptions.assumeThatCode;


public class SQLFpcWSTest {

    /**
     * Tests whether {@code getCoverageTargets} returns the correct queries if it is able to connect to the SQLFpc web
     * service.
     */
    @Test
    public void testGetCoverageTargetsNormalQuery() {
        String sqlQuery = "SELECT * FROM tableA WHERE TableA.var = 0 OR TableA.Id = 1";

        String dbSchema = " <schema dbms=\"MySQL\">\n"
                + "     <table name=\"TableA\">\n"
                + "         <column name=\"id\" type=\"VARCHAR\" size=\"50\" key=\"true\" notnull=\"true\"/>\n"
                + "         <column name=\"var\" type=\"VARCHAR\" size=\"255\" default=\"NULL\"/>\n"
                + "     </table>\n"
                + " </schema>";

        String options = "";

        assumeThatCode(
                () -> SQLFpcWS.getCoverageTargets(sqlQuery, dbSchema, options)
        ).doesNotThrowAnyException();

        List<String> result = SQLFpcWS.getCoverageTargets(sqlQuery, dbSchema, options);

        assumeThat(result.size()).isGreaterThan(0);

        assertThat(result).containsExactlyInAnyOrder(
                "SELECT * FROM tableA WHERE (TableA.var = 0) AND NOT(TableA.Id = 1)",
                "SELECT * FROM tableA WHERE (TableA.var = 1) AND NOT(TableA.Id = 1)",
                "SELECT * FROM tableA WHERE (TableA.var = -1) AND NOT(TableA.Id = 1)",
                "SELECT * FROM tableA WHERE (TableA.var IS NULL) AND NOT(TableA.Id = 1)",
                "SELECT * FROM tableA WHERE NOT(TableA.Id = 1) AND NOT(TableA.var = 0)",
                "SELECT * FROM tableA WHERE (TableA.Id = 1) AND NOT(TableA.var = 0)"
        );
    }

    /**
     * Tests whether {@code getCoverageTargets} correctly returns an empty list for a query with no coverage targets,
     * if it is able to connect to the SQLFpc.
     */
    @Test
    public void testGetCoverageNoResults() {
        String sqlQuery = "SELECT * FROM tableB";

        String dbSchema = " <schema dbms=\"MySQL\">\n"
                + "     <table name=\"TableB\">\n"
                + "         <column name=\"id\" type=\"VARCHAR\" size=\"25\" key=\"true\" notnull=\"true\"/>\n"
                + "     </table>\n"
                + " </schema>";

        String options = "";

        assumeThatCode(
                () -> SQLFpcWS.getCoverageTargets(sqlQuery, dbSchema, options)
        ).doesNotThrowAnyException();

        List<String> result = SQLFpcWS.getCoverageTargets(sqlQuery, dbSchema, options);

        assertThat(result).hasSize(0);
    }

}
