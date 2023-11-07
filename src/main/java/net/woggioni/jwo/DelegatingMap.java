package net.woggioni.jwo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static net.woggioni.jwo.JWO.streamCat;

public class DelegatingMap<K,V> extends UnmodifiableDelegatingMap<K, V> {
    private final Map<K,V> thisMap;
    public DelegatingMap(Supplier<Map<K,V>> mapFactory, List<Map<K,V>> delegates) {
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
    public boolean containsKey(Object key) {
        if(thisMap.containsKey(key)) return true;
        return super.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if(thisMap.containsValue(value)) return true;
        return super.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return Optional.ofNullable(thisMap.get(key)).orElseGet(
            () -> super.get(key)
        );
    }

    @Override
    public V put(K key, V value) {
        return thisMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return thisMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
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
