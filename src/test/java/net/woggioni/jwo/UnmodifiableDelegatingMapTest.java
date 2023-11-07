package net.woggioni.jwo;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static net.woggioni.jwo.CollectionUtils.toMap;
import static net.woggioni.jwo.CollectionUtils.toUnmodifiableMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnmodifiableDelegatingMapTest {

    private static Map<String, Integer> createMap() {
        final var m1 = Stream.of(
            Map.entry("a", 1),
            Map.entry("b", 2),
            Map.entry("c", 3),
            Map.entry("d", 4),
            Map.entry("e", 5),
            Map.entry("f", 6)
        ).collect(toUnmodifiableMap(TreeMap::new, Map.Entry::getKey, Map.Entry::getValue));
        final var m2 = Stream.of(
            Map.entry("a", 7),
            Map.entry("c", 8),
            Map.entry("e", 9),
            Map.entry("g", 10),
            Map.entry("h", 11)
        ).collect(toUnmodifiableMap(TreeMap::new, Map.Entry::getKey, Map.Entry::getValue));
        return UnmodifiableDelegatingMap.of(TreeMap::new, m1, m2);
    }

    private final Map<String, Integer> delegatingMap = createMap();
    @Test
    public void test() {
        assertEquals(1, delegatingMap.get("a"));
        assertEquals(2, delegatingMap.get("b"));
        assertEquals(10, delegatingMap.get("g"));

        for(final var entry : delegatingMap.entrySet()) {
            assertEquals(delegatingMap.get(entry.getKey()), entry.getValue());
        }
    }

    @Test
    public void test2() {
        assertFalse(delegatingMap.isEmpty());
        assertEquals(1, delegatingMap.get("a"));
        assertEquals(2, delegatingMap.get("b"));
        assertEquals(10, delegatingMap.get("g"));

        for(final var entry : delegatingMap.entrySet()) {
            assertTrue(delegatingMap.containsKey(entry.getKey()),
                String.format("Expected key '%s' was not found in map", entry.getKey()));
            assertTrue(delegatingMap.containsValue(entry.getValue()),
                String.format("Expected value '%d' was not found in map", entry.getValue()));
            assertEquals(delegatingMap.get(entry.getKey()), entry.getValue());
        }
        assertThrows(UnsupportedOperationException.class, () -> {
            delegatingMap.put("key", 42);
        });
    }

    @Test
    public void checkPutNotAllowed() {
        assertThrows(UnsupportedOperationException.class, () -> {
            delegatingMap.put("key", 42);
        });
    }
    @Test
    public void checkClearNotAllowed() {
        assertThrows(UnsupportedOperationException.class, () -> {
            delegatingMap.clear();
        });
    }
    @Test
    public void checkRemoveNotAllowed() {
        assertThrows(UnsupportedOperationException.class, () -> {
            delegatingMap.remove("a");
        });
    }
    @Test
    public void checkPutAllNotAllowed() {
        assertThrows(UnsupportedOperationException.class, () -> {
            delegatingMap.putAll(Stream.of(Map.entry("c", 42))
                .collect(toMap(HashMap::new, Map.Entry::getKey, Map.Entry::getValue)));
        });
    }
}
