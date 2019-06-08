package in2test.samples.sqlmutationws.java;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SQLjson {

    @SerializedName("entries")
    @Expose
    private List<Rules> entries = new ArrayList<>();

    public List<Rules> getEntries() {
        return entries;
    }

    public void addEntry(Rules rules) {
        entries.add(rules);
    }

    public void setEntries(List<Rules> entries) {
        this.entries = entries;
    }

}