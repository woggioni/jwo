package net.woggioni.jwo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RetryExecutorTest {
    @Test
    @SneakyThrows
    public void test() {
        int expectedValue = 42;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        RetryExecutor.ExceptionHandler exceptionHandler = err -> RetryExecutor.ExceptionHandlerOutcome.CONTINUE;
        CompletableFuture<Integer> n = RetryExecutor.submit(
            new Callable<>() {
                private int counter = 0;

                @Override
                public Integer call() {
                    log.info("Attempt {}", counter);
                    try {
                        if (counter != 5) {
                            throw new RuntimeException();
                        } else {
                            return expectedValue;
                        }
                    } finally {
                        counter++;
                    }
                }
            },
            10,
            Duration.ofMillis(100),
            1.3,
            exceptionHandler,
            executorService
        );
        Assertions.assertEquals(expectedValue, n.get(10, TimeUnit.SECONDS));
    }
}