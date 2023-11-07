package net.woggioni.jwo.internal;

import net.woggioni.jwo.LazyValue;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LazyValueTest {
    @Test
    void unsynchronizedLazyValueTest() {
        final var bool = new boolean[1];
        final Supplier<Void> supplier = () -> {
            bool[0] = true;
            return null;
        };
        assertFalse(bool[0]);
        final var lazy = LazyValue.of(supplier, LazyValue.ThreadSafetyMode.NONE);
        assertFalse(bool[0]);
        lazy.get();
        assertTrue(bool[0]);
        final Supplier<Void> throwingSupplier = () -> {
            throw new RuntimeException();
        };

        final var throwingLazy = LazyValue.of(throwingSupplier, LazyValue.ThreadSafetyMode.NONE);
        throwingLazy.handle((v, ex) -> {
            assertNotNull(ex);
            assertEquals(RuntimeException.class, ex.getClass());
            return null;
        });
    }

}
