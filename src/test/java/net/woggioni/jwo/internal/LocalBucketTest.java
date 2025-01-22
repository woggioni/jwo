package net.woggioni.jwo.internal;

import lombok.SneakyThrows;
import net.woggioni.jwo.Bucket;
import net.woggioni.jwo.Con;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class LocalBucketTest {
    @Test
    @SneakyThrows
    void testRemoveTokens() {
        try(final ExecutorService executorService = Executors.newFixedThreadPool(16)) {
            final Bucket bucket = new LocalBucket(100, 1, Duration.of(62500, ChronoUnit.MICROS).toNanos(), 0);
            final long now = System.nanoTime();
            IntStream.range(0, 16).mapToObj(n -> executorService.submit(() -> {
                while (!bucket.removeTokens(1)) {
                }
            })).forEach((Con<Future<?>>) Future::get);
            final double elapsedTime = System.nanoTime() - now;
            System.out.printf("Elapsed time: %.3f s\n", elapsedTime / 1e9);
            Assertions.assertTrue(0.1 > (1_000_000_000 - elapsedTime));
            executorService.shutdown();
            Assertions.assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    @SneakyThrows
    void testRemoveTokensWithEstimate() {
        final long now = System.nanoTime();
        final long maxCapacity = 10000;
        final long fillAmount = 1;
        final long fillPeriod = Duration.of(62500, ChronoUnit.MICROS).toNanos();
        final long initialAmount = 0;
        final Bucket bucket = new LocalBucket(maxCapacity, fillAmount, fillPeriod, initialAmount, now);
        Assertions.assertThrows(IllegalArgumentException.class, () -> bucket.removeTokensWithEstimate(maxCapacity + 1, now));
        final long requestedTokens = 1000;
        final long result = bucket.removeTokensWithEstimate(requestedTokens, now);
        Assertions.assertEquals(result, (requestedTokens - initialAmount) * fillPeriod / fillAmount);
        final long elapsedTime = Duration.ofSeconds(30).toNanos();
        final long result2 = bucket.removeTokensWithEstimate(requestedTokens, now + elapsedTime);
        Assertions.assertEquals(result2, (requestedTokens - initialAmount) * fillPeriod / fillAmount - elapsedTime);
    }
}
