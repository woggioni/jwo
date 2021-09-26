package net.woggioni.jwo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class DelegatingMap<K,V> implements Map<K, V> {
    private final Supplier<Map<K,V>> mapFactory;
    private final List<Map<K,V>> delegates;
    private final Map<K,V> thisMap;

    public DelegatingMap(Supplier<Map<K,V>> mapFactory, List<Map<K,V>> delegates) {
        this.mapFactory = mapFactory;
        this.delegates = delegates;
        thisMap = mapFactory.get();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        if(!thisMap.isEmpty()) return false;
        for(Map<K,V> delegate : delegates) {
            if(!delegate.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        if(thisMap.containsKey(key)) return true;
        for(Map<K,V> delegate : delegates) {
            if(!delegate.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if(thisMap.containsValue(value)) return true;
        for(Map<K,V> delegate : delegates) {
            if(!delegate.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        V result = thisMap.get(key);
        if(result != null) return result;
        for(Map<K,V> delegate : delegates) {
            result = delegate.get(key);
            if(result != null) break;
        }
        return result;
    }

    @Override
    public V put(K key, V value) {
        V result = thisMap.put(key, value);
        if(result != null) return result;
        for(Map<K,V> delegate : delegates) {
            result = delegate.put(key, value);
            if(result != null) break;
        }
        return result;
    }

    @Override
    public V remove(Object key) {
        V result = thisMap.remove(key);
        if(result != null) return result;
        for(Map<K,V> delegate : delegates) {
            result = delegate.remove(key);
            if(result != null) break;
        }
        return result;
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

    private Map<K, V> flatten() {
        Map<K, V> result = mapFactory.get();
        int i = delegates.size();
        while(i-->0) {
            Map<K, V> delegate = delegates.get(i);
            result.putAll(delegate);
        }
        result.putAll(thisMap);
        return Collections.unmodifiableMap(result);
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
