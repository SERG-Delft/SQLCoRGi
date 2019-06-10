package nl.tudelft.st01.query;

import net.sf.jsqlparser.schema.Column;

import java.util.List;
import java.util.Set;

public class OuterIncrementRelation {
    private Set<String> loiRelations;
    private Set<String> roiRelations;
    private List<Column> loiRelColumns;
    private List<Column> roiRelColumns;

    public OuterIncrementRelation(Set<String> leftOuterIncrementRelation, Set<String> rightOuterIncrementRelation,
                                  List<Column> loiRelColumns, List<Column> roiRelColumns) {
        this.loiRelations = leftOuterIncrementRelation;
        this.roiRelations = rightOuterIncrementRelation;
        this.loiRelColumns = loiRelColumns;
        this.roiRelColumns = roiRelColumns;
    }

    public List<Column> getLoiRelColumns() {
        return loiRelColumns;
    }

    public List<Column> getRoiRelColumns() {
        return roiRelColumns;
    }

    public Set<String> getLoiRelations() {
        return loiRelations;
    }

    public Set<String> getRoiRelations() {
        return roiRelations;
    }

}
