package net.woggioni.jwo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static net.woggioni.jwo.JWO.streamCat;

public class DelegatingMap<K,V> extends UnmodifiableDelegatingMap<K, V> {
    private final Map<K,V> thisMap;
    public DelegatingMap(final Supplier<Map<K,V>> mapFactory, final List<Map<K,V>> delegates) {
        super(mapFactory, delegates);
        thisMap = mapFactory.get();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        if(!thisMap.isEmpty()) return false;
        return super.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        if(thisMap.containsKey(key)) return true;
        return super.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        if(thisMap.containsValue(value)) return true;
        return super.containsValue(value);
    }

    @Override
    public V get(final Object key) {
        return Optional.ofNullable(thisMap.get(key)).orElseGet(
            () -> super.get(key)
        );
    }

    @Override
    public V put(final K key, final V value) {
        return thisMap.put(key, value);
    }

    @Override
    public V remove(final Object key) {
        return thisMap.remove(key);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        this.thisMap.putAll(m);
    }

    @Override
    public void clear() {
        thisMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return flatten().keySet();
    }

    protected Map<K, V> flatten() {
        return streamCat(super.stream(), thisMap.entrySet().stream())
            .collect(CollectionUtils.toUnmodifiableMap(this.mapFactory, Map.Entry::getKey, Map.Entry::getValue));
    }
}
