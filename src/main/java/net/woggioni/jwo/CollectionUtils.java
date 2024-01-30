package net.woggioni.jwo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class CollectionUtils {

    public enum MapMergeStrategy {
        THROW, REPLACE, KEEP
    }

    @SafeVarargs
    public static <T> ArrayList<T> newArrayList(T... args) {
        return new ArrayList<>(Arrays.asList(args));
    }

    public static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
        return Collector.of(
                ArrayList::new,
                List::add,
                (ArrayList<T> l1, ArrayList<T> l2) -> {
                    l1.addAll(l2);
                    return l1;
                },
                Collections::unmodifiableList);
    }

    @SafeVarargs
    public static <T> List<T> immutableList(T... elements) {
        return Stream.of(elements).collect(toUnmodifiableList());
    }

    public static <T> Collector<T, ?, Set<T>> toUnmodifiableSet() {
        return Collector.of(
                HashSet::new,
                Set::add,
                (Set<T> s1, Set<T> s2) -> {
                    s1.addAll(s2);
                    return s1;
                },
                Collections::unmodifiableSet);
    }

    @SafeVarargs
    public static <T> Set<T> immutableSet(T... elements) {
        return Stream.of(elements).collect(toUnmodifiableSet());
    }

    private static <T> Collector<T, ?, NavigableSet<T>>
    createTreeSetCollector(Function<NavigableSet<T>, NavigableSet<T>> finalizer, Comparator<? super T> comparator) {
        return Collector.of(
                () -> new TreeSet<>(comparator),
                Set::add,
                (NavigableSet<T> s1, NavigableSet<T> s2) -> {
                    s1.addAll(s2);
                    return s1;
                },
                finalizer);
    }

    private static <T extends Comparable<T>> Collector<T, ?, NavigableSet<T>>
    createTreeSetCollector(Function<NavigableSet<T>, NavigableSet<T>> finalizer) {
        return Collector.of(
                TreeSet::new,
                Set::add,
                (NavigableSet<T> s1, NavigableSet<T> s2) -> {
                    s1.addAll(s2);
                    return s1;
                },
                finalizer);
    }

    public static <T extends Comparable<T>> Collector<T, ?, NavigableSet<T>> toTreeSet() {
        return createTreeSetCollector(Function.identity());
    }

    public static <T> Collector<T, ?, NavigableSet<T>> toTreeSet(Comparator<? super T> comparator) {
        return createTreeSetCollector(Function.identity(), comparator);
    }

    public static <T> Collector<T, ?, NavigableSet<T>> toUnmodifiableTreeSet(Comparator<? super T> comparator) {
        return createTreeSetCollector(Collections::unmodifiableNavigableSet, comparator);
    }

    public static <T extends Comparable<T>> Collector<T, ?, NavigableSet<T>> toUnmodifiableTreeSet() {
        return createTreeSetCollector(Collections::unmodifiableNavigableSet);
    }

    @SafeVarargs
    public static <T> NavigableSet<T> immutableTreeSet(Comparator<? super T> comparator, T... elements) {
        return Stream.of(elements).collect(toUnmodifiableTreeSet(comparator));
    }

    @SafeVarargs
    public static <T extends Comparable<T>> NavigableSet<T> immutableTreeSet(T... elements) {
        return Stream.of(elements).collect(toUnmodifiableTreeSet());
    }

    private static <K, V, M extends Map<K, V>> BinaryOperator<M> mapMerger(BinaryOperator<V> var0) {
        return (m1, m2) -> {
            Iterator<Map.Entry<K, V>> it = m2.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                m1.merge(entry.getKey(), entry.getValue(), var0);
            }

            return m1;
        };
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (v1, v2) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", v1));
        };
    }
    private static <T> BinaryOperator<T> updatingMerger() {
        return (v1, v2) -> v2;
    }

    private static <T> BinaryOperator<T> conservativeMerger() {
        return (v1, v2) -> v1;
    }

    private static <T> BinaryOperator<T> getMerger(MapMergeStrategy mapMergeStrategy) {
        BinaryOperator<T> result;
        switch (mapMergeStrategy) {
            case KEEP:
                result = conservativeMerger();
                break;
            case THROW:
                result = throwingMerger();
                break;
            case REPLACE:
                result = updatingMerger();
                break;
            default:
                throw new NullPointerException();
        }
        return result;
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toUnmodifiableHashMap(
        Function<T, K> keyExtractor,
        Function<T, V> valueExtractor,
        MapMergeStrategy mapMergeStrategy) {
        return toUnmodifiableMap(HashMap::new, keyExtractor, valueExtractor, mapMergeStrategy);
    }
    public static <T, K, V> Collector<T, ?, Map<K, V>> toUnmodifiableHashMap(
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor) {
        return toUnmodifiableMap(HashMap::new, keyExtractor, valueExtractor);
    }

    public static <T, K, V> Collector<T, ?, NavigableMap<K, V>> toUnmodifiableTreeMap(
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor) {
        return toUnmodifiableNavigableMap(TreeMap::new, keyExtractor, valueExtractor);
    }

    public static <T, K, V> Collector<T, ?, NavigableMap<K, V>> toUnmodifiableTreeMap(
        Function<T, K> keyExtractor,
        Function<T, V> valueExtractor,
        MapMergeStrategy mapMergeStrategy) {
        return toUnmodifiableNavigableMap(TreeMap::new, keyExtractor, valueExtractor, mapMergeStrategy);
    }
    public static <T, K, V> Collector<T, ?, NavigableMap<K, V>> toUnmodifiableTreeMap(
        Function<T, K> keyExtractor,
        Function<T, V> valueExtractor,
        Comparator<K> comparator,
        MapMergeStrategy mapMergeStrategy) {
        return toUnmodifiableNavigableMap(() -> new TreeMap<>(comparator), keyExtractor, valueExtractor, mapMergeStrategy);
    }
    public static <T, K, V> Collector<T, ?, NavigableMap<K, V>> toUnmodifiableTreeMap(
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor,
            Comparator<K> comparator) {
        return toUnmodifiableNavigableMap(() -> new TreeMap<>(comparator), keyExtractor, valueExtractor);
    }

    public static <T, K, V> Collector<T, ?, NavigableMap<K, V>> toTreeMap(
        Function<T, K> keyExtractor,
        Function<T, V> valueExtractor,
        MapMergeStrategy mapMergeStrategy) {
        return toNavigableMap(TreeMap::new, keyExtractor, valueExtractor, mapMergeStrategy);
    }
    public static <T, K, V> Collector<T, ?, NavigableMap<K, V>> toTreeMap(
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor) {
        return toNavigableMap(TreeMap::new, keyExtractor, valueExtractor);
    }

    public static <T, K, V> Collector<T, ?, NavigableMap<K, V>> toTreeMap(
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor,
            Comparator<K> comparator) {
        return toNavigableMap(() -> new TreeMap<>(comparator), keyExtractor, valueExtractor);
    }

    public static <T, K, V> Collector<T, ?, NavigableMap<K, V>> toTreeMap(
        Function<T, K> keyExtractor,
        Function<T, V> valueExtractor,
        Comparator<K> comparator,
        MapMergeStrategy mapMergeStrategy) {
        return toNavigableMap(() -> new TreeMap<>(comparator), keyExtractor, valueExtractor, mapMergeStrategy);
    }

    public static <T, K, V, MAP_TYPE extends NavigableMap<K, V>> Collector<T, ?, MAP_TYPE> toNavigableMap(
        Supplier<MAP_TYPE> constructor,
        Function<T, K> keyExtractor,
        Function<T, V> valueExtractor) {
        return toNavigableMap(
            constructor,
            keyExtractor,
            valueExtractor,
            MapMergeStrategy.THROW
        );
    }

    public static <T, K, V, MAP_TYPE extends NavigableMap<K, V>> Collector<T, ?, MAP_TYPE> toNavigableMap(
            Supplier<MAP_TYPE> constructor,
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor,
            MapMergeStrategy mapMergeStrategy) {
        BinaryOperator<V> valueMerger = getMerger(mapMergeStrategy);
        BiConsumer<MAP_TYPE, T> accumulator = (map, streamElement) -> {
            map.merge(keyExtractor.apply(streamElement), valueExtractor.apply(streamElement),
                valueMerger
            );
        };
        return Collector.of(
                constructor,
                accumulator,
                mapMerger(valueMerger)
        );
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toMap(
        Supplier<Map<K, V>> constructor,
        Function<T, K> keyExtractor,
        Function<T, V> valueExtractor) {
        return toMap(constructor, keyExtractor, valueExtractor, MapMergeStrategy.THROW);
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toMap(
            Supplier<Map<K, V>> constructor,
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor,
            MapMergeStrategy mapMergeStrategy) {
        BinaryOperator<V> valueMerger = getMerger(mapMergeStrategy);
        BiConsumer<Map<K, V>, T> accumulator = (map, streamElement) -> {
            map.merge(keyExtractor.apply(streamElement), valueExtractor.apply(streamElement),
                valueMerger
            );
        };
        return Collector.of(
                constructor,
                accumulator,
                mapMerger(valueMerger)
        );
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toUnmodifiableMap(
        Supplier<Map<K, V>> constructor,
        Function<T, K> keyExtractor,
        Function<T, V> valueExtractor) {
        return toUnmodifiableMap(constructor, keyExtractor, valueExtractor, MapMergeStrategy.THROW);
    }
    public static <T, K, V> Collector<T, ?, Map<K, V>> toUnmodifiableMap(
            Supplier<Map<K, V>> constructor,
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor,
            MapMergeStrategy mapMergeStrategy) {
        BinaryOperator<V> valueMerger = getMerger(mapMergeStrategy);
        BiConsumer<Map<K, V>, T> accumulator = (map, streamElement) -> {
            map.merge(keyExtractor.apply(streamElement),
                valueExtractor.apply(streamElement),
                valueMerger
            );
        };
        return Collector.of(
                constructor,
                accumulator,
                mapMerger(valueMerger),
                Collections::unmodifiableMap
        );
    }

    public static <T, K, V> Collector<T, ?, NavigableMap<K, V>> toUnmodifiableNavigableMap(
        Supplier<NavigableMap<K, V>> constructor,
        Function<T, K> keyExtractor,
        Function<T, V> valueExtractor,
        MapMergeStrategy mapMergeStrategy
    ) {
        BinaryOperator<V> valueMerger = getMerger(mapMergeStrategy);
        BiConsumer<NavigableMap<K, V>, T> accumulator = (map, streamElement) -> {
            map.merge(
                keyExtractor.apply(streamElement),
                valueExtractor.apply(streamElement),
                valueMerger
            );
        };
        return Collector.of(
            constructor,
            accumulator,
            mapMerger(valueMerger),
            Collections::unmodifiableNavigableMap
        );
    }
    public static <T, K, V> Collector<T, ?, NavigableMap<K, V>> toUnmodifiableNavigableMap(
            Supplier<NavigableMap<K, V>> constructor,
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor) {
        return toUnmodifiableNavigableMap(constructor, keyExtractor, valueExtractor, MapMergeStrategy.THROW);
    }
    public static <K, V, U> Stream<Map.Entry<K, U>> mapValues(Map<K, V> map, Fun<V, U> xform) {
        return map
                .entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), xform.apply(entry.getValue())));
    }

    public static <T> Iterator<T> reverseIterator(List<T> list) {
        return new Iterator<T>() {
            private final ListIterator<T> it = list.listIterator(list.size());
            @Override
            public boolean hasNext() {
                return it.hasPrevious();
            }

            @Override
            public T next() {
                return it.previous();
            }
        };
    }

    public static <T> Iterator<T> reverseIterator(NavigableSet<T> set) {
        return set.descendingIterator();
    }
    public static <K, V> Iterator<Map.Entry<K, V>> reverseIterator(NavigableMap<K, V> map) {
        return map.descendingMap().entrySet().iterator();
    }
}
