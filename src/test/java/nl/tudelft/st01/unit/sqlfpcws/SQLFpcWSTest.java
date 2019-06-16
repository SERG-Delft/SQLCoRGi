package nl.tudelft.st01.unit.sqlfpcws;

import es.uniovi.lsi.in2test.sqlfpcws.SQLFpcWSSoapProxy;

import nl.tudelft.st01.sqlfpcws.SQLFpcWS;

import nl.tudelft.st01.util.exceptions.SQLFpcException;
import nl.tudelft.st01.util.exceptions.SQLFpcParseException;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class that tests {@link SQLFpcWS}.
 */
// Suppresses the Checkstyle MultipleStringLiterals warning as these duplicate strings are necessary for readability.
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@PrepareForTest(SQLFpcWS.class)
@RunWith(PowerMockRunner.class)
public class SQLFpcWSTest {

    private SQLFpcWSSoapProxy mockWebService;

    public static final String NO_OPTIONAL_ARGS = "";

    private static final String DATABASE_SCHEMA =
            " <schema dbms=\"MySQL\">\n"
                    + "     <table name=\"TableB\">\n"
                    + "         <column name=\"id\" type=\"VARCHAR\" size=\"25\" key=\"true\" notnull=\"true\"/>\n"
                    + "     </table>\n"
                    + " </schema>";

    private static final String SERVER_XML_REPLY =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<sqlfpc>\n"
                    + "   <version>1.3.180.91</version>\n"
                    + "   <sql>SELECT * FROM tableA WHERE TableA.var = 0 OR TableA.Id = 1</sql>\n"
                    + "   <fpcrules>\n"
                    + "      <fpcrule>\n"
                    + "         <id>1</id>\n"
                    + "         <category>S</category>\n"
                    + "         <type>B</type>\n"
                    + "         <subtype>B+F</subtype>\n"
                    + "         <location>1.1.[TableA.var = 0]</location>\n"
                    + "         <sql>SELECT * FROM tableA WHERE (TableA.var = 1) AND NOT(TableA.Id = 1)</sql>\n"
                    + "         <description>--Some row in the table such that:\n"
                    + "--The WHERE condition fulfills:\n"
                    + "  --(B+) TableA.var = 1\n"
                    + "  --(F) TableA.Id = 1 is FALSE</description>\n"
                    + "      </fpcrule>\n"
                    + "      <fpcrule>\n"
                    + "         <id>2</id>\n"
                    + "         <category>S</category>\n"
                    + "         <type>B</type>\n"
                    + "         <subtype>B=F</subtype>\n"
                    + "         <location>1.1.[TableA.var = 0]</location>\n"
                    + "         <sql>SELECT * FROM tableA WHERE (TableA.var = 0) AND NOT(TableA.Id = 1)</sql>\n"
                    + "         <description>--Some row in the table such that:\n"
                    + "--The WHERE condition fulfills:\n"
                    + "  --(B=) TableA.var = 0\n"
                    + "  --(F) TableA.Id = 1 is FALSE</description>\n"
                    + "      </fpcrule>\n"
                    + "      <fpcrule>\n"
                    + "         <id>3</id>\n"
                    + "         <category>S</category>\n"
                    + "         <type>B</type>\n"
                    + "         <subtype>B-F</subtype>\n"
                    + "         <location>1.1.[TableA.var = 0]</location>\n"
                    + "         <sql>SELECT * FROM tableA WHERE (TableA.var = -1) AND NOT(TableA.Id = 1)</sql>\n"
                    + "         <description>--Some row in the table such that:\n"
                    + "--The WHERE condition fulfills:\n"
                    + "  --(B-) TableA.var = -1\n"
                    + "  --(F) TableA.Id = 1 is FALSE</description>\n"
                    + "      </fpcrule>\n"
                    + "      <fpcrule>\n"
                    + "         <id>4</id>\n"
                    + "         <category>S</category>\n"
                    + "         <type>N</type>\n"
                    + "         <subtype>NF</subtype>\n"
                    + "         <location>1.1.[TableA.var]</location>\n"
                    + "         <sql>SELECT * FROM tableA WHERE (TableA.var IS NULL) AND NOT(TableA.Id = 1)</sql>\n"
                    + "         <description>--Some row in the table such that:\n"
                    + "--The WHERE condition fulfills:\n"
                    + "  --(N) TableA.var is NULL\n"
                    + "  --(F) TableA.Id = 1 is FALSE</description>\n"
                    + "      </fpcrule>\n"
                    + "      <fpcrule>\n"
                    + "         <id>5</id>\n"
                    + "         <category>S</category>\n"
                    + "         <type>T</type>\n"
                    + "         <subtype>FF</subtype>\n"
                    + "         <location>1.2.[TableA.Id = 1]</location>\n"
                    + "         <sql>SELECT * FROM tableA WHERE NOT(TableA.Id = 1) AND NOT(TableA.var = 0)</sql>\n"
                    + "         <description>--Some row in the table such that:\n"
                    + "--The WHERE condition fulfills:\n"
                    + "  --(F) TableA.Id = 1 is FALSE\n"
                    + "  --(F) TableA.var = 0 is FALSE</description>\n"
                    + "      </fpcrule>\n"
                    + "      <fpcrule>\n"
                    + "         <id>6</id>\n"
                    + "         <category>S</category>\n"
                    + "         <type>T</type>\n"
                    + "         <subtype>TF</subtype>\n"
                    + "         <location>1.2.[TableA.Id = 1]</location>\n"
                    + "         <sql>SELECT * FROM tableA WHERE (TableA.Id = 1) AND NOT(TableA.var = 0)</sql>\n"
                    + "         <description>--Some row in the table such that:\n"
                    + "--The WHERE condition fulfills:\n"
                    + "  --(T) TableA.Id = 1 is TRUE\n"
                    + "  --(F) TableA.var = 0 is FALSE</description>\n"
                    + "      </fpcrule>\n"
                    + "   </fpcrules>\n"
                    + "</sqlfpc>";

    /**
     * Sets up the {@link SQLFpcWSSoapProxy} mock object that will be used to mock the response from the SQLFpc web
     * service.
     */
    @Before
    public void setUpMock() {
        mockWebService = mock(SQLFpcWSSoapProxy.class);
    }

    /**
     * Trying to invoke the {@link SQLFpcWS} constructor should throw an {@link UnsupportedOperationException}.
     * <p>
     * Java Reflection is used because the {@link SQLFpcWS} constructor is private.
     *
     * @throws NoSuchMethodException if the {@link SQLFpcWS} constructor is not found - this cannot happen.
     */
    @org.junit.jupiter.api.Test
    public void testConstructorThrowsException() throws NoSuchMethodException {
        Constructor<SQLFpcWS> sqlFpcWSConstructorConstructor = SQLFpcWS.class.getDeclaredConstructor();
        sqlFpcWSConstructorConstructor.setAccessible(true);

        assertThatThrownBy(
            () -> sqlFpcWSConstructorConstructor.newInstance()
        ).hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Tests whether {@code getCoverageTargets} returns the correct queries. The XML response from the SQLFpc web
     * service is mocked in order for the test to work independently of a working internet connection.
     *
     * @throws Exception because of the use of {@link PowerMockito}.
     */
    @Test
    public void testGetCoverageTargetsNormalQuery() throws Exception {
        String sqlQuery = "SELECT * FROM tableA WHERE TableA.var = 0 OR TableA.Id = 1";

        String dbSchema =
                " <schema dbms=\"MySQL\">\n"
                        + "     <table name=\"TableA\">\n"
                        + "         <column name=\"id\" type=\"VARCHAR\" size=\"50\" key=\"true\" notnull=\"true\"/>\n"
                        + "         <column name=\"var\" type=\"VARCHAR\" size=\"255\" default=\"NULL\"/>\n"
                        + "     </table>\n"
                        + " </schema>";

        when(mockWebService.getRules(any(), any(), any())).thenReturn(SERVER_XML_REPLY);
        PowerMockito.whenNew(SQLFpcWSSoapProxy.class).withAnyArguments().thenReturn(mockWebService);

        List<String> result = SQLFpcWS.getCoverageTargets(sqlQuery, dbSchema, NO_OPTIONAL_ARGS);

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
     * Tests whether {@code getCoverageTargets} correctly returns an empty list for a query that generates no targets.
     * The XML response from the SQLFpc web service is mocked in order for the test to work independently of a working
     * internet connection.
     *
     * @throws Exception because of the use of {@link PowerMockito}.
     */
    @Test
    public void testGetCoverageNoResults() throws Exception {
        String sqlQuery = "SELECT * FROM tableB";

        String serverXMLReply =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<sqlfpc>\n"
                        + "   <version>1.3.180.91</version>\n"
                        + "   <sql>SELECT * FROM tableB</sql>\n"
                        + "   <fpcrules />\n"
                        + "</sqlfpc>";

        when(mockWebService.getRules(any(), any(), any())).thenReturn(serverXMLReply);
        PowerMockito.whenNew(SQLFpcWSSoapProxy.class).withAnyArguments().thenReturn(mockWebService);

        List<String> result = SQLFpcWS.getCoverageTargets(sqlQuery, DATABASE_SCHEMA, NO_OPTIONAL_ARGS);

        assertThat(result).hasSize(0);
    }

    /**
     * Tests whether {@code getCoverageTargets} throws the proper exception when the XML response from the server is
     * not syntactically valid.
     * The XML response from the SQLFpc web service is mocked in order for the test to work independently of a working
     * internet connection.
     *
     * @throws Exception because of the use of {@link PowerMockito}.
     */
    @Test
    public void testCorrectExceptionIsThrownWhenSQLFpcReturnsInvalidXML() throws Exception {
        String sqlQuery = "SELECT * FROM tableB";

        String invalidXMLServerReply =
                         "   <version>1.3.180.91</version>\n"
                        + "   <sql>SELECT * FROM tableB</sql>\n"
                        + "   <fpcrules>\n"
                        + "</sqlfpc>";

        when(mockWebService.getRules(any(), any(), any())).thenReturn(invalidXMLServerReply);
        PowerMockito.whenNew(SQLFpcWSSoapProxy.class).withAnyArguments().thenReturn(mockWebService);

        assertThatExceptionOfType(SQLFpcException.class).isThrownBy(
            () -> SQLFpcWS.getCoverageTargets(sqlQuery, DATABASE_SCHEMA, NO_OPTIONAL_ARGS)
        );
    }

    /**
     * Tests whether {@code getCoverageTargets} throws the correct exception when the SQLFpc server is not reachable.
     * The SQLFpc web service is mocked in order to fake a non-reachable server - i.e. the {@code getRules} method will
     * always return a {@link RemoteException}.
     *
     * @throws Exception because of the use of {@link PowerMockito}.
     */
    @Test
    public void testCorrectExceptionIsThrownWhenServerIsNotReachable() throws Exception {
        String sqlQuery = "SELECT * FROM tableA WHERE TableA.id = 'value'";

        when(mockWebService.getRules(any(), any(), any())).thenThrow(RemoteException.class);
        PowerMockito.whenNew(SQLFpcWSSoapProxy.class).withAnyArguments().thenReturn(mockWebService);

        assertThatExceptionOfType(SQLFpcException.class).isThrownBy(
            () -> SQLFpcWS.getCoverageTargets(sqlQuery, DATABASE_SCHEMA, NO_OPTIONAL_ARGS)
        );
    }

    /**
     * Assert that the correct exception is thrown when SQLFpc returns an error about the syntax of the SQL query.
     *
     * The XML response from the SQLFpc web service is mocked in order for the test to work independently of a working
     * internet connection.
     *
     * @throws Exception because of the use of {@link PowerMockito}.
     */
    @org.junit.jupiter.api.Test
    @Disabled("Couldn't implement as SQLFpc is down...")
    public void testCorrectExceptionIsThrownWhenSQLQueryIsNotValidForSQLFpc() throws Exception {
        String sqlQuery = "SELECT * FROM tableA LIMIT 0, 1";

        String invalidXMLServerReply =
                          "<sqlfpc>\n"
                        + "   <version>1.3.180.91</version>\n"
                        + "   <sql>SELECT * FROM tableB</sql>\n"
                        + "   <fpcrules>\n"
                        + "</sqlfpc>";

        when(mockWebService.getRules(any(), any(), any())).thenReturn(invalidXMLServerReply);
        PowerMockito.whenNew(SQLFpcWSSoapProxy.class).withAnyArguments().thenReturn(mockWebService);

        assertThatExceptionOfType(SQLFpcParseException.class).isThrownBy(
            () -> SQLFpcWS.getCoverageTargets(sqlQuery, DATABASE_SCHEMA, NO_OPTIONAL_ARGS)
        );
    }
}
