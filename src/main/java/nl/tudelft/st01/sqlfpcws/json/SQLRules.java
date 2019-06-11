package nl.tudelft.st01.sqlfpcws.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class represents the rules in the JSON output of the {@code BulkMainGenerator}.
 *
 * {@link com.google.gson.Gson} is used to enable easy JSON (de)serialization.
 */
// Suppresses PMD warning UnusedPrivateField and Spotbugs warning URF_UNREAD_FIELD, because the fields are in fact used,
// but not by the class, but for JSON (de)serialization.
@SuppressWarnings("PMD.UnusedPrivateField")
@SuppressFBWarnings(
        value = "URF_UNREAD_FIELD",
        justification = "Field is used for JSON (de)serialization")
public class SQLRules {

    @SerializedName("queryNo")
    @Expose
    private Integer queryNo;

    @SerializedName("pathList")
    @Expose
    private List<String> pathList;

    /**
     * Creates an {@link SQLRules} object.
     *
     * @param queryNo  The index of the query (as related to the order in the SQL input file).
     * @param pathList The list of coverage targets.
     */
    public SQLRules(Integer queryNo, List<String> pathList) {
        this.queryNo = queryNo;
        this.pathList = pathList;
    }
}
