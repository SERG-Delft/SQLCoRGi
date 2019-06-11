package nl.tudelft.st01.query;

import net.sf.jsqlparser.schema.Column;

import java.util.List;
import java.util.Set;

/**
 * This class is used to store the outer increment relations and each of the corresponding columns.
 */
public class OuterIncrementRelation {
    private Set<String> loiRelations;
    private Set<String> roiRelations;
    private List<Column> loiRelColumns;
    private List<Column> roiRelColumns;

    /**
     * Constructor. Generates the object.
     * @param leftOuterIncrementRelation The left outer increment relations (loirels).
     * @param rightOuterIncrementRelation The right outer increment relations (roirels).
     * @param loiRelColumns The columns corresponding to the loirels.
     * @param roiRelColumns The columns corresponding to the roirels/
     */
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

    public void setLoiRelations(Set<String> loiRelations) {
        this.loiRelations = loiRelations;
    }

    public void setRoiRelations(Set<String> roiRelations) {
        this.roiRelations = roiRelations;
    }
}
