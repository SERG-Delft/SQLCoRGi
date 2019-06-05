package nl.tudelft.st01.unit.util.cloner;

import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import nl.tudelft.st01.util.cloner.SelectCloner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link SelectCloner} utility class.
 */
// Justification: Some objects have multiple object fields, which we want to verify are copied as well.
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class SelectClonerTest {

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link PlainSelect}.
     */
    @Test
    void testCopyPlainSelect() {
        // TODO
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link SetOperationList}.
     */
    @Test
    void testCopySetOperationList() {
        // TODO
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link WithItem}.
     */
    @Test
    void testCopyWithItem() {

        WithItem original = new WithItem();

        PlainSelect selectBody = new PlainSelect();
        original.setSelectBody(selectBody);

        List<SelectItem> withItemList = new ArrayList<>();
        AllTableColumns allTableColumns = new AllTableColumns();
        withItemList.add(allTableColumns);

        original.setWithItemList(withItemList);

        WithItem copy = (WithItem) SelectCloner.copy(original);
        assertCopyEquals(original, copy);

        assertThat(copy.getSelectBody()).isNotSameAs(selectBody);
        assertThat(copy.getWithItemList()).isNotSameAs(withItemList);
        assertThat(copy.getWithItemList().get(0)).isNotSameAs(allTableColumns);
    }

    /**
     * Tests whether {@link SelectCloner#copy(SelectBody)} makes a deep copy of a {@link ValuesStatement}.
     */
    @Test
    void testCopyValuesStatement() {
        // TODO
    }

    // TODO: Tests for SelectItems

    // TODO: Tests for FromItems

    /**
     * Tests whether {@code copy} is equivalent to {@code original}.
     *
     * @param original the original {@code SelectBody}.
     * @param copy the copy of the original {@code SelectBody}.
     */
    private static void assertCopyEquals(SelectBody original, SelectBody copy) {
        assertThat(copy)
                .isNotSameAs(original)
                .hasSameClassAs(original)
                .isEqualToComparingFieldByFieldRecursively(original);
    }

}
