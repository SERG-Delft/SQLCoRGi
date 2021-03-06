package com.github.sergdelft.sqlcorgi.unit.util;

import com.github.sergdelft.sqlcorgi.util.Expressions;
import net.sf.jsqlparser.statement.select.Join;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests the {@link Expressions} utility class.
 */
class ExpressionsTest {

    /**
     * Verifies that {@link Expressions} cannot be instantiated.
     *
     * @throws NoSuchMethodException should not happen.
     */
    @Test
    void testInstantiationForbidden() throws NoSuchMethodException {

        Constructor<Expressions> constructor = Expressions.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        Throwable thrown = catchThrowable(constructor::newInstance);

        assertThat(thrown).hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Tests whether {@link Expressions#setJoinToInner(Join)} sets a {@link Join} to {@code INNER}.
     */
    @Test
    void testSetJoinToInner() {

        Join innerJoin = new Join();
        innerJoin.setInner(true);

        Join join = new Join();
        join.setLeft(true);

        Expressions.setJoinToInner(join);

        assertThat(join).isEqualToComparingFieldByField(innerJoin);
    }

}
