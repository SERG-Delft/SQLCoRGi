package nl.tudelft.st01;

import java.util.Set;

public class OuterIncrementRelation {
    private Set<String> loiRelations;
    private Set<String> roiRelations;

    public OuterIncrementRelation(Set<String> leftOuterIncrementRelation, Set<String> rightOuterIncrementRelation) {
        this.loiRelations = leftOuterIncrementRelation;
        this.roiRelations = rightOuterIncrementRelation;
    }

    public Set<String> getLoiRelations() {
        return loiRelations;
    }

    public Set<String> getRoiRelations() {
        return roiRelations;
    }

}
