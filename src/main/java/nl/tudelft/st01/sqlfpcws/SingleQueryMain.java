package nl.tudelft.st01.sqlfpcws;

import java.util.List;

public final class SingleQueryMain {

    protected SingleQueryMain() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        String sqlQuery = "SELECT * FROM staff WHERE staff.name = 'Maurício Aniche'";

        String dbSchema = " <schema dbms=\"MySQL\">\n"
                        + "     <table staff=\"tableA\">\n"
                        + "         <column name=\"id\" type=\"VARCHAR\" size=\"50\" key=\"true\" notnull=\"true\"/>\n"
                        + "         <column name=\"name\" type=\"VARCHAR\" size=\"255\" default=\"NULL\"/>\n"
                        + "     </table>\n"
                        + " </schema>";

        String options = "";

        List<String> result = SQLFpcWS.getCoverageTargets(sqlQuery, dbSchema, options);

        System.out.println("Coverage targets (" + result.size() + "):");
        for (String target : result) {
            System.out.println(target);
        }
    }
}
