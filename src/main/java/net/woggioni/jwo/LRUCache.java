package net.woggioni.jwo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class LRUCache {
    public static <K, V> Map<K, V> of(final long maxSize, final Function<K, V> loader, Class<K> cls) {
        return new LinkedHashMap<K, V>() {
            @Override
            protected boolean removeEldestEntry(final Map.Entry eldest) {
                return size() >= maxSize;
            }

            @Override
            public V get(final Object key) {
                if(cls.isInstance(key)) {
                    return computeIfAbsent((K) key, loader);
                } else {
                    return null;
                }
            }

            @Override
            public void putAll(final Map<? extends K, ? extends V> m) {
                throw new UnsupportedOperationException();
            }

            @Override
            public V put(final K key, final V value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public V putIfAbsent(final K key, final V value) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
