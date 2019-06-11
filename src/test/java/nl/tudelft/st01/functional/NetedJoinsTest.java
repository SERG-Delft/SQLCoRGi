package nl.tudelft.st01.functional;

import nl.tudelft.st01.Generator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static nl.tudelft.st01.functional.AssertUtils.containsAtLeast;
import static nl.tudelft.st01.functional.AssertUtils.verify;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test class is related to {@link JoinTest}.
 * The functionality of nested joins is tested and evaluated.
 */
public class NetedJoinsTest {
    @Test
    public void testNestedJoinCorrectJoinConfiguration() {
        verify("SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                    + "WHERE (b.id IS NULL) AND (a.id IS NOT NULL)",
                "SELECT * FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                + "WHERE (b.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id INNER JOIN c ON c.id = b.id "
                        + "WHERE (a.id IS NULL) AND (b.id IS NULL)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (c.id IS NULL) AND (b.id IS NOT NULL)",
                "SELECT * FROM a INNER JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.id "
                        + "WHERE (c.id IS NULL) AND (b.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (c.id IS NOT NULL)",
                "SELECT * FROM a RIGHT JOIN b ON b.id = a.id RIGHT JOIN c ON c.id = b.id "
                        + "WHERE (b.id IS NULL) AND (c.id IS NULL)");
    }


}
