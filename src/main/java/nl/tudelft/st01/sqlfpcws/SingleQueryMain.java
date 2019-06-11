package nl.tudelft.st01.sqlfpcws;

import java.util.List;

/**
 * This class contains the main that uses the SQLFpc web service to generate coverage targets for a single query.
 */
public final class SingleQueryMain {

    /**
     * No instance of this class should be created.
     */
    protected SingleQueryMain() {
        throw new UnsupportedOperationException();
    }

    /**
     * Main method for manual invocation of the SQLFpc web service.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        String sqlQuery = "SELECT * FROM staff WHERE staff.name = 'Maurício Aniche'";

        String dbSchema = " <schema dbms=\"MySQL\">\n"
                        + "     <table name=\"staff\">\n"
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
