package nl.tudelft.st01.queries;

import nl.tudelft.st01.Generator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests if the coverage targets for queries with aggregators are generated correctly.
 *
 * Suppresses the checkstyle multipleStringLiterals violation, because some output sets have queries in common.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
public class EvoSQLQueriesTest {

    /**
     * hi.
     */
    @Test
    public void testDifficultQuery() {
        Set<String> result = Generator.generateRules("SELECT accounts.*, accounts_cstm.* FROM accounts LEFT JOIN accounts_cstm  ON accounts.id = accounts_cstm.id_c  WHERE accounts.deleted ='0' AND ((accounts_cstm.jjwg_maps_lat_c ='0' AND accounts_cstm.jjwg_maps_lng_c ='0' OR (accounts_cstm.jjwg_maps_lat_c IS NULL AND accounts_cstm.jjwg_maps_lng_c IS NULL)) AND (accounts_cstm.jjwg_maps_geocode_status_c = '' OR accounts_cstm.jjwg_maps_geocode_status_c IS NULL) LIMIT 0,250");

        assertThat(result).isEqualTo(null);
        assertThat(true).isEqualTo(true);
    }


}
