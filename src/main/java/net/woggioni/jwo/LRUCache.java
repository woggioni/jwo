package net.woggioni.jwo;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@RequiredArgsConstructor
public class LRUCache<K, V> implements Map<K, V> {
    private final Map<K, V> delegate;

    private LRUCache(final long maxSize, final Function<K, V> loader, Class<K> cls) {
        delegate = new LinkedHashMap<K, V>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() >= maxSize;
            }

            @Override
            public V get(Object key) {
                if(cls.isInstance(key)) {
                    return computeIfAbsent((K) key, loader);
                } else {
                    return null;
                }
            }
        };
    }

    @Override
    public V get(Object key) {
        return delegate.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public V put(K key, V value) {
        return null;
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof LRUCache) {
            return delegate.equals(o);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    public static <K, V> LRUCache<K, V> of(final long maxSize, final Function<K, V> loader, Class<K> cls) {
        return new LRUCache<>(maxSize, loader, cls);
    }
}
