package net.woggioni.jwo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class MapBuilder<K,V> {
    private final List<Map.Entry<K, V>> entries = new ArrayList<>();

    public MapBuilder<K, V> entry(K key, V value) {
        entries.add(new AbstractMap.SimpleEntry<>(key, value));
        return this;
    }

    public <T> T build(Sup<Map<K,V>> factory, Function<Map<K,V>, T> finalizer) {
        Map<K, V> result = factory.get();
        for(Map.Entry<K, V> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return finalizer.apply(result);
    }

    public Stream<Map.Entry<K,V>> stream() {
        return entries.stream();
    }

    public Map<K,V> build(Sup<Map<K,V>> factory) {
        return build(factory, Function.identity());
    }

    public static <K, V> MapBuilder<K, V> with(K key, V value) {
        return new MapBuilder<K, V>().entry(key, value);
    }
}
