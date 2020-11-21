package net.woggioni.jwo.cache;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import net.woggioni.jwo.JWO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Function;

@EqualsAndHashCode
class RandomObject implements Serializable {
    @EqualsAndHashCode.Include
    UUID uuid = UUID.randomUUID();

    String name = uuid.toString();

    String md5 = md5(name);

    String md5half1 = md5.substring(0, 16);

    String md5half2 = md5.substring(16);

    @SneakyThrows
    private String md5(String source) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(source.getBytes());
        byte[] digest = md.digest();
        return JWO.bytesToHex(digest);
    }
}

public class LruCacheTest {

    private static final int CACHE_MAX_SIZE = 50;
    private static final int NUMBER_OF_ENTRIES = 100;

    int loaderInvocations = 0;
    List<RandomObject> objects;

    LruCache<String, RandomObject> lruCache;

    @AfterEach
    public void teardown() {
        lruCache.clear();
    }

    @Test
    public void cacheWithoutFallbackAndLoader() {
        lruCache = new LruCache<>(CACHE_MAX_SIZE, true);
        objects = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_ENTRIES; i++) {
            RandomObject randomObject = new RandomObject();
            lruCache.put(randomObject.name, randomObject);
            objects.add(randomObject);
        }

        //Since NUMBER_OF_ENTRIES > CACHE_MAX_SIZE the cache size should have reached its maximum
        Assertions.assertEquals(CACHE_MAX_SIZE, lruCache.size());

        // The cache should not contain any of the first NUMBER_OF_ENTRIES - CACHE_MAX_SIZE elements (they should have been evicted)
        objects.stream().limit(NUMBER_OF_ENTRIES - CACHE_MAX_SIZE)
            .forEach(value -> Assertions.assertFalse(lruCache.containsKey(value.name)));

        // The cache should contain all the CACHE_MAX_SIZE elements that were last inserted
        objects.stream().skip(CACHE_MAX_SIZE)
            .peek(value -> Assertions.assertTrue(lruCache.containsKey(value.name)))
            .forEach(value -> Assertions.assertEquals(value, lruCache.get(value.name)));

        //Removing the first inserted element should be a no-op since it should have been already evicted
        RandomObject randomObject = objects.get(0);
        Assertions.assertNull(lruCache.remove(randomObject.name));
        Assertions.assertFalse(lruCache.containsKey(randomObject.name));
        Assertions.assertEquals(CACHE_MAX_SIZE, lruCache.size());

        //Removing the last inserted element should return it and decrease the cache size by 1
        randomObject = objects.get(objects.size() - 1);
        Assertions.assertEquals(randomObject, lruCache.remove(randomObject.name));
        Assertions.assertFalse(lruCache.containsKey(randomObject.name));
        Assertions.assertEquals(CACHE_MAX_SIZE - 1, lruCache.size());

        //Clearing the cache should reduce its size to 0
        lruCache.clear();
        Assertions.assertEquals(0, lruCache.size());
    }

    @Test
    public void cacheWithFallback() {
        Map<String, RandomObject> fallback = new HashMap<>();
        lruCache = new LruCache<>(CACHE_MAX_SIZE, true, null, fallback);
        objects = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_ENTRIES; i++) {
            RandomObject randomObject = new RandomObject();
            fallback.put(randomObject.name, randomObject);
            objects.add(randomObject);
        }

        //The cache should contain all the elements that were inserted in the fallback map
        objects.forEach(value -> Assertions.assertTrue(lruCache.containsKey(value.name)));

        //The cache size should be equal to the number of inserted elements
        Assertions.assertEquals(NUMBER_OF_ENTRIES, lruCache.size());

        //Removing the first inserted element should return it and decrease the cache size by 1
        RandomObject randomObject = objects.get(0);
        Assertions.assertEquals(randomObject, lruCache.remove(randomObject.name));
        Assertions.assertFalse(lruCache.containsKey(randomObject.name));
        Assertions.assertEquals(NUMBER_OF_ENTRIES - 1, lruCache.size());

        //Clearing the cache should reduce its size to 0
        lruCache.clear();
        Assertions.assertEquals(0, lruCache.size());
    }

    @Test
    public void cacheWithLoader() {
        Function<String, RandomObject> loader = key -> {
            loaderInvocations++;
            return objects.stream().filter(o -> Objects.equals(key, o.name)).findFirst().get();
        };
        lruCache = new LruCache<>(CACHE_MAX_SIZE, true, loader);
        LruCache<String, RandomObject>.Stats stats = lruCache.getStats();
        objects = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_ENTRIES; i++) {
            RandomObject randomObject = new RandomObject();
            objects.add(randomObject);
            lruCache.put(randomObject.name, randomObject);
        }

        //Since NUMBER_OF_ENTRIES > CACHE_MAX_SIZE the cache size should have reached its maximum
        Assertions.assertEquals(CACHE_MAX_SIZE, lruCache.size());

        int evictionsAfterLoad = stats.getEvictions();
        int loaderInvocations = this.loaderInvocations;
        // The cache should contain all the CACHE_MAX_SIZE elements that were last inserted without loading any with the loader
        objects.stream().skip(CACHE_MAX_SIZE)
            .peek(value -> Assertions.assertTrue(lruCache.containsKey(value.name)))
            .forEach(value -> Assertions.assertEquals(value, lruCache.get(value.name)));
        // Since the entries were already present in the cache, no new invocation of the loader should have been performed,
        // nor any new eviction should have occurred
        Assertions.assertEquals(evictionsAfterLoad, stats.getEvictions());
        Assertions.assertEquals(loaderInvocations, this.loaderInvocations);

        // The cache should not contain any of the first NUMBER_OF_ENTRIES - CACHE_MAX_SIZE elements
        int evictedElements = NUMBER_OF_ENTRIES - CACHE_MAX_SIZE;
        objects.stream().limit(evictedElements)
            .forEach(value -> Assertions.assertFalse(lruCache.containsKey(value.name)));

        // Calling "get" for entries that were not present in the cache, should trigger a new invocation of the loader
        // for each of them, and, since the cache was full, an equal number of evictions should occurr
        evictionsAfterLoad = stats.getEvictions();
        loaderInvocations = this.loaderInvocations;
        objects.stream().limit(evictedElements)
            .forEach(value -> lruCache.get(value.name));
        Assertions.assertEquals(evictionsAfterLoad + evictedElements, stats.getEvictions());
        Assertions.assertEquals(loaderInvocations + evictedElements, this.loaderInvocations);

        //Removing the last inserted element should be a no-op since it should have been already evicted
        RandomObject randomObject = objects.get(objects.size() - 1);
        Assertions.assertNull(lruCache.remove(randomObject.name));
        Assertions.assertFalse(lruCache.containsKey(randomObject.name));
        Assertions.assertEquals(CACHE_MAX_SIZE, lruCache.size());

        //Removing the first inserted element should return it and decrease the cache size by 1
        randomObject = objects.get(0);
        Assertions.assertEquals(randomObject, lruCache.remove(randomObject.name));
        Assertions.assertFalse(lruCache.containsKey(randomObject.name));
        Assertions.assertEquals(CACHE_MAX_SIZE - 1, lruCache.size());

        //Clearing the cache should reduce its size to 0
        lruCache.clear();
        Assertions.assertEquals(0, lruCache.size());
    }
}