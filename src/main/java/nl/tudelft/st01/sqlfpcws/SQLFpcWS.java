package nl.tudelft.st01.sqlfpcws;

import es.uniovi.lsi.in2test.sqlfpcws.SQLFpcWSSoapProxy;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public final class SQLFpcWS {

    private SQLFpcWS() {
        throw new UnsupportedOperationException();
    }

    private static final String ERROR_XPATH = "/sqlfpc/error";
    private static final String SQL_TARGET_XPATH = "/sqlfpc/fpcrules/fpcrule/sql";

    public List<String> getCoverageTargets(String sqlQuery, String schema, String options) {
        SQLFpcWSSoapProxy proxy = new SQLFpcWSSoapProxy();
        List<String> result;

        String xmlResponseString;
        try {
            xmlResponseString = proxy.getRules(sqlQuery, schema, options);
        } catch (RemoteException e) {
            System.err.println("Server error: " + e.getMessage());
            return new ArrayList<>();
        }

        result = extractSQLTargetsFromXMLResponse(xmlResponseString);

        return result;
    }

    private List<String> extractSQLTargetsFromXMLResponse(String xmlResponseString) {
        ArrayList<String> result = new ArrayList<>();
        SAXReader reader = new SAXReader();

        Document xmlReponse;
        try {
            xmlReponse = reader.read(new StringReader(xmlResponseString));
        } catch (DocumentException e) {
            System.err.println("Server response was invalid");
            return result;
        }

        Node error = xmlReponse.selectSingleNode(ERROR_XPATH);
        if(error != null) {
            System.err.println("SQL Query could not be parsed: " + error.getText());
            return result;
        }

        List<Element> sqlRules = (List<Element>)xmlReponse.selectNodes(SQL_TARGET_XPATH);
        for (Element sqlRule : sqlRules) {
            result.add(sqlRule.getText());
        }

        return result;
    }
}
