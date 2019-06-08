package nl.tudelft.st01.sqlfpcws.json;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.StringReader;

import java.util.ArrayList;
import java.util.List;

public class SQLFpcWS {

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

        Node error = xmlReponse.selectSingleNode("/sqlfpc/error");
        if(error != null) {
            System.err.println("SQL Query could not be parsed: " + error.getText());
            return result;
        }

        List<Element> sqlRules = (List<Element>)xmlReponse.selectNodes("/sqlfpc/fpcrules/fpcrule/sql");
        for (Element sqlRule : sqlRules) {
            result.add(sqlRule.getText());
        }

        return result;
    }
}
