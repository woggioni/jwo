package net.woggioni.jwo;

import lombok.Getter;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LruCache<K, V> implements Map<K, V> {
    private final Map<K, V> linkedHashMap;
    private final Object mtx;
    private final int maxSize;
    private final Function<? super K, ? extends V> loader;
    private final Map<K, V> fallback;

    @Getter
    private Stats stats = new Stats();

    private final Chronometer chronometer = new Chronometer();

    public LruCache(final int maxSize) {
        this(maxSize, false);
    }

    public LruCache(final int maxSize, final boolean threadSafe) {
        this(maxSize, threadSafe, null, null);
    }

    public LruCache(final int maxSize, final boolean threadSafe, final Function<? super K, ? extends V> loader) {
        this(maxSize, threadSafe, loader, null);
    }

    public LruCache(final int maxSize, final boolean threadSafe, final Function<? super K, ? extends V> loader, final Map<K,V> fallback) {
        this.linkedHashMap = new LinkedHashMap<>();
        this.mtx = threadSafe ? linkedHashMap : null;
        this.maxSize = maxSize;
        if (loader != null) {
            if(fallback == null) {
                this.loader = (K key) -> {
                    this.stats.miss++;
                    return loader.apply(key);
                };
            } else {
                this.loader = (K key) -> {
                    this.stats.miss++;
                    final V value = fallback.get(key);
                    if (value == null) {
                        return loader.apply(key);
                    } else {
                        return value;
                    }
                };
            }
        } else {
            this.loader = null;
        }
        this.fallback = fallback;
    }

    private <T> T withMutex(final Supplier<T> supplier) {
        if(mtx != null) {
            synchronized (mtx) {
                return supplier.get();
            }
        } else {
            return supplier.get();
        }
    }

    @Override
    public int size() {
        if(fallback == null) {
            return withMutex(linkedHashMap::size);
        } else {
            return withMutex(fallback::size);
        }
    }

    @Override
    public boolean isEmpty() {
        return withMutex(linkedHashMap::isEmpty);
    }

    @Override
    public boolean containsKey(final Object key) {
        K k;
        try {
            k = (K) key;
        } catch (final ClassCastException cce) {
            return false;
        }
        return withMutex(() -> {
            boolean result = linkedHashMap.containsKey(key);
            if(!result && fallback != null) {
                result = fallback.containsKey(key);
            }
            return result;
        });
    }

    @Override
    public boolean containsValue(final Object value) {
        return withMutex(() -> {
            boolean result = linkedHashMap.containsValue(value);
            if(!result && fallback != null) {
                result = fallback.containsValue(value);
            }
            return result;
        });
    }

    private V getOrFallback(final K key) {
        V value = linkedHashMap.get(key);
        if(value == null) {
            stats.miss++;
            if (fallback != null) {
                value = fallback.get(key);
                if (value != null) {
                    linkedHashMap.put(key, value);
                }
            }
        }
        return value;
    }

    @Override
    public V get(final Object key) {
        K k;
        try {
            k = (K) key;
        } catch (final ClassCastException cce) {
            return null;
        }
        return withMutex(() -> {
            stats.calls++;
            if (loader != null) {
                try {
                    chronometer.reset();
                    final V result = getOrFallback(k);
                    if (result == null) {
                        V newValue;
                        if ((newValue = loader.apply(k)) != null) {
                            put(k, newValue);
                            return newValue;
                        }
                    }
                    stats.loadingTime += chronometer.elapsed();
                    return result;
                } catch (final Exception e) {
                    stats.exceptions++;
                    throw e;
                }
            } else {
                return getOrFallback(k);
            }
        });
    }

    @Override
    public V put(final K k, final V v) {
        return withMutex(() -> {
            if (linkedHashMap.size() == maxSize) {
                final Iterator<Entry<K, V>> it = linkedHashMap.entrySet().iterator();
                if (it.hasNext()) {
                    final Map.Entry<K, V> entry = it.next();
                    remove(entry.getKey());
                    stats.evictions++;
                }
            }
            if(fallback != null) {
                fallback.put(k, v);
            }
            linkedHashMap.put(k, v);
            return v;
        });
    }

    @Override
    public V remove(final Object key) {
        return withMutex( () -> {
            if(fallback != null) {
                final V result = fallback.remove(key);
                if(result != null) {
                    linkedHashMap.remove(key);
                }
                return result;
            } else {
                return linkedHashMap.remove(key);
            }
        });
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        withMutex(() -> {
            linkedHashMap.clear();
            if(fallback != null) {
                fallback.clear();
            }
            return null;
        });
    }

    @Override
    public Set<K> keySet() {
        return withMutex(() -> {
            if(fallback == null) {
                return linkedHashMap.keySet();
            } else {
                return Stream.concat(
                        linkedHashMap.keySet().stream(), fallback.keySet().stream()
                ).collect(CollectionUtils.toUnmodifiableSet());
            }
        });
    }

    @Override
    public Collection<V> values() {
        return withMutex(() -> {
            if(fallback == null) {
                return linkedHashMap.values();
            } else {
                return Stream.concat(
                        linkedHashMap.values().stream(), fallback.values().stream()
                ).collect(CollectionUtils.toUnmodifiableList());
            }
        });
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return withMutex(() -> {
            if(fallback == null) {
                return linkedHashMap.entrySet();
            } else {
                return Stream.concat(
                        linkedHashMap.entrySet().stream(), fallback.entrySet().stream()
                ).collect(CollectionUtils.toUnmodifiableSet());
            }
        });
    }

    public Stats resetStats() {
        return withMutex(() -> {
            stats = new Stats();
            return stats;
        });
    }

    @Getter
    public class Stats {

        private int miss;

        private int calls;

        private int exceptions;

        private int evictions;

        private long loadingTime;

        public int getSize() {
            return LruCache.this.size();
        }

        public float getHitRate() {
            return (calls - miss) / (float) calls;
        }

        public float getAverageLoadingTime(final Chronometer.UnitOfMeasure unitOfMeasure) {
            return (float) stats.loadingTime / calls / unitOfMeasure.nanoseconds;
        }

        public float getTotalLoadingTime(final Chronometer.UnitOfMeasure unitOfMeasure) {
            return (float) stats.loadingTime / unitOfMeasure.nanoseconds;
        }

        public float getAverageLoadingTime() {
            return getAverageLoadingTime(Chronometer.UnitOfMeasure.SECONDS);
        }

        public float getTotalLoadingTime() {
            return getAverageLoadingTime(Chronometer.UnitOfMeasure.SECONDS);
        }
    }
}