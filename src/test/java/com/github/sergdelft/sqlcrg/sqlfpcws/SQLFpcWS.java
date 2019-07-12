package com.github.sergdelft.sqlcrg.sqlfpcws;

import es.uniovi.lsi.in2test.sqlfpcws.SQLFpcWSSoapProxy;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that can be used to invoke the SQLFpc web service.
 *
 * @see <a href="https://in2test.lsi.uniovi.es/sqlfpcws/helpfpcws.aspx#specification">Web service specification</a>
 */
public final class SQLFpcWS {

    private static final String ERROR_XPATH = "/sqlfpc/error";
    private static final String SQL_TARGET_XPATH = "/sqlfpc/fpcrules/fpcrule/sql";

    /**
     * No instance of this class should be created.
     */
    private SQLFpcWS() {
        throw new UnsupportedOperationException();
    }

    /**
     * Calls the {@code getRules} method of the SQLFpc web service with a specified query, the schema and optional
     * parameters.
     *
     * @param sqlQuery  the query for which coverage targets need to be generated.
     * @param schema    the schema (in XML format) of the database on which the query operates.
     * @param options   the optional parameters (in XML format)
     * @return A list of coverage targets.
     */
    public static List<String> getCoverageTargets(String sqlQuery, String schema, String options) {
        SQLFpcWSSoapProxy proxy = new SQLFpcWSSoapProxy();
        List<String> result;

        String xmlResponseString;
        try {
            xmlResponseString = proxy.getRules(sqlQuery, schema, options);
        } catch (RemoteException e) {
            throw new SQLFpcException("Cannot reach SQLFpc: " + e.getMessage());
        }

        result = extractSQLTargetsFromXMLResponse(xmlResponseString);

        return result;
    }

    /**
     * Parses the XML response from the {@code getRules} method of the SQLFpc web service and extracts the SQL targets.
     *
     * @param xmlResponseString the XML-formatted response from SQLFpc.
     * @return A list of the coverage targets.
     */
    private static List<String> extractSQLTargetsFromXMLResponse(String xmlResponseString) {
        ArrayList<String> result = new ArrayList<>();
        SAXReader reader = new SAXReader();

        Document xmlResponse;
        try {
            xmlResponse = reader.read(new StringReader(xmlResponseString));
        } catch (DocumentException e) {
            throw new SQLFpcException("XML Response could not be parsed: " + e.getMessage());
        }

        Node error = xmlResponse.selectSingleNode(ERROR_XPATH);
        if (error != null) {
            throw new SQLFpcParseException(error.getText());
        }

        List<Node> sqlRules = xmlResponse.selectNodes(SQL_TARGET_XPATH);
        for (Node sqlRule : sqlRules) {
            result.add(sqlRule.getText());
        }

        return result;
    }
}
