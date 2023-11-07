package net.woggioni.jwo.internal;

import net.woggioni.jwo.CollectionUtils;
import net.woggioni.jwo.MutableTuple2;
import net.woggioni.jwo.MutableTuple3;
import net.woggioni.jwo.Tuple2;
import net.woggioni.jwo.Tuple3;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TupleTest {

    @Test
    void mutableTuple2Test() {
        final var t1 = MutableTuple2.newInstance(1, "a");
        final var t2 = MutableTuple2.newInstance(1, "a");
        assertEquals(t1, t2);
        final var s = Stream.of(t1, t2).collect(CollectionUtils.toUnmodifiableSet());
        assertEquals(1, s.size());
        t1.set_1(2);
        t1.set_2("b");
        assertEquals(t1, MutableTuple2.newInstance(2, "b"));
    }

    @Test
    void mutableTuple3Test() {
        final var t1 = MutableTuple3.newInstance(1, "a", BigDecimal.ONE);
        final var t2 = MutableTuple3.newInstance(1, "a", BigDecimal.ONE);
        assertEquals(t1, t2);
        final var s = Stream.of(t1, t2).collect(CollectionUtils.toUnmodifiableSet());
        assertEquals(1, s.size());
        t1.set_1(2);
        t1.set_2("b");
        t1.set_3(BigDecimal.ZERO);
        assertEquals(t1, MutableTuple3.newInstance(2, "b", BigDecimal.ZERO));
    }
    @Test
    void tuple2Test() {
        final var t1 = Tuple2.newInstance(1, "a");
        final var t2 = Tuple2.newInstance(1, "a");
        assertEquals(t1, t2);
        final var s = Stream.of(t1, t2).collect(CollectionUtils.toUnmodifiableSet());
        assertEquals(1, s.size());
    }

    @Test
    void tuple3Test() {
        final var t1 = Tuple3.newInstance(1, "a", BigDecimal.ONE);
        final var t2 = Tuple3.newInstance(1, "a", BigDecimal.ONE);
        assertEquals(t1, t2);
        final var s = Stream.of(t1, t2).collect(CollectionUtils.toUnmodifiableSet());
        assertEquals(1, s.size());
    }
}
