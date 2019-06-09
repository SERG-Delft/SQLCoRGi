package nl.tudelft.st01;

import net.sf.jsqlparser.schema.Column;

import java.util.List;
import java.util.Set;

public class OuterIncrementRelation {
    private Set<String> loiRelations;
    private Set<String> roiRelations;
    private List<Column> loiRelColumns;
    private List<Column> roiRelColumns;

    public OuterIncrementRelation(Set<String> leftOuterIncrementRelation, Set<String> rightOuterIncrementRelation) {
        this.loiRelations = leftOuterIncrementRelation;
        this.roiRelations = rightOuterIncrementRelation;
        this.loiRelColumns = null;
        this.roiRelColumns = null;
    }

    public void setLoiRelations(Set<String> loiRelations) {
        this.loiRelations = loiRelations;
    }

    public void setRoiRelations(Set<String> roiRelations) {
        this.roiRelations = roiRelations;
    }

    public List<Column> getLoiRelColumns() {
        return loiRelColumns;
    }

    public void setLoiRelColumns(List<Column> loiRelColumns) {
        this.loiRelColumns = loiRelColumns;
    }

    public List<Column> getRoiRelColumns() {
        return roiRelColumns;
    }

    public void setRoiRelColumns(List<Column> roiRelColumns) {
        this.roiRelColumns = roiRelColumns;
    }

    public Set<String> getLoiRelations() {
        return loiRelations;
    }

    public Set<String> getRoiRelations() {
        return roiRelations;
    }

}
