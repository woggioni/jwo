package net.woggioni.jwo;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static net.woggioni.jwo.JWO.newThrowable;

@RequiredArgsConstructor
public class UnmodifiableDelegatingMap<K, V> implements Map<K, V> {
    protected final Supplier<Map<K, V>> mapFactory;
    private final List<Map<K, V>> delegates;

    public static <K, V> UnmodifiableDelegatingMap<K, V> of(
            final Supplier<Map<K, V>> mapFactory,
            final Map<K, V>... delegates
    ) {
        return new UnmodifiableDelegatingMap<>(mapFactory, Arrays.asList(delegates));
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        for (final Map<K, V> delegate : delegates) {
            if (!delegate.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsKey(final Object key) {
        for (final Map<K, V> delegate : delegates) {
            if (delegate.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(final Object value) {
        for (final Map<K, V> delegate : delegates) {
            if (delegate.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(final Object key) {
        V result = null;
        for (final Map<K, V> delegate : delegates) {
            result = delegate.get(key);
            if (result != null) break;
        }
        return result;
    }

    @Override
    public V put(final K key, final V value) {
        throw newThrowable(UnsupportedOperationException.class);
    }

    @Override
    public V remove(final Object key) {
        throw newThrowable(UnsupportedOperationException.class);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        throw newThrowable(UnsupportedOperationException.class);
    }

    @Override
    public void clear() {
        throw newThrowable(UnsupportedOperationException.class);
    }

    @Override
    public Set<K> keySet() {
        return flatten().keySet();
    }

    private Map<K, V> flatten() {
        return stream().collect(
            CollectionUtils.toUnmodifiableMap(
                mapFactory,
                Map.Entry::getKey,
                Map.Entry::getValue,
                CollectionUtils.MapMergeStrategy.REPLACE
            )
        );
    }

    protected Stream<Entry<K, V>> stream() {
        return JWO.iterator2Stream(CollectionUtils.reverseIterator(delegates)).flatMap(
            it -> it.entrySet().stream()
        );
    }

    @Override
    public Collection<V> values() {
        return flatten().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return flatten().entrySet();
    }
}
