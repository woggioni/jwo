package net.woggioni.jwo;

import lombok.Builder;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static net.woggioni.jwo.JWO.delayedExecutor;
import static net.woggioni.jwo.JWO.newThrowable;

@Builder
public class RetryExecutor {
    @Builder.Default
    private final int maxAttempts = 5;

    @Builder.Default
    private final Duration initialDelay = Duration.ofSeconds(1);

    @Builder.Default
    private final Double exp = 1.0;

    @Builder.Default
    private final ExceptionHandler exceptionHandler = err -> ExceptionHandlerOutcome.CONTINUE;

    @Builder.Default
    private final ExecutorService executorService = ForkJoinPool.commonPool();

    public enum ExceptionHandlerOutcome {
        THROW, CONTINUE
    }

    public interface ExceptionHandler {
        ExceptionHandlerOutcome handleError(Throwable t);
    }

    public CompletableFuture<Void> submit(Runnable cb) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public CompletableFuture<Void> submit(
        Runnable cb,
        ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public CompletableFuture<Void> submit(
        Runnable cb,
        ExceptionHandler exceptionHandler,
        ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public CompletableFuture<Void> submit(
        Runnable cb,
        Double exp,
        ExceptionHandler exceptionHandler,
        ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public CompletableFuture<Void> submit(
        Runnable cb,
        Duration initialDelay,
        Double exp,
        ExceptionHandler exceptionHandler,
        ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }
    public static CompletableFuture<Void> submit(
        Runnable cb,
        int maxAttempts,
        Duration initialDelay,
        Double exp,
        ExceptionHandler exceptionHandler,
        ExecutorService executorService) {
        return submit(() -> {
            cb.run();
            return null;
        }, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public <T> CompletableFuture<T> submit(Callable<T> cb) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public <T> CompletableFuture<T> submit(
        Callable<T> cb,
        ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public <T> CompletableFuture<T> submit(
        Callable<T> cb,
        ExceptionHandler exceptionHandler,
        ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public <T> CompletableFuture<T> submit(
        Callable<T> cb,
        Double exp,
        ExceptionHandler exceptionHandler,
        ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public <T> CompletableFuture<T> submit(
        Callable<T> cb,
        Duration initialDelay,
        Double exp,
        ExceptionHandler exceptionHandler,
        ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public static <T> CompletableFuture<T> submit(
        Callable<T> cb,
        int maxAttempts,
        Duration initialDelay,
        Double exp,
        ExceptionHandler exceptionHandler,
        ExecutorService executorService) {
        CompletableFuture<T> result = CompletableFuture.supplyAsync((Sup<T>) cb::call, executorService);
        double delay = initialDelay.toMillis();
        for(int i = 1; i <= maxAttempts; i++) {
            int attempt = i;
            double thisAttemptDelay = delay;
            result = result.handleAsync((BiFun<T, Throwable, CompletableFuture<T>>) (value, err) -> {
                Optional<Throwable> causeOpt = Optional.ofNullable(err).map(Throwable::getCause);
                if(!causeOpt.isPresent()) {
                    return CompletableFuture.completedFuture(value);
                } else if(attempt == maxAttempts) {
                    throw causeOpt.get();
                } else {
                    Throwable cause = causeOpt.get();
                    ExceptionHandlerOutcome eho = exceptionHandler.handleError(cause);
                    switch (eho) {
                        case THROW:
                            throw cause;
                        case CONTINUE:
                            Executor delayedExecutor = delayedExecutor(
                                (long) thisAttemptDelay,
                                TimeUnit.MILLISECONDS,
                                executorService
                            );
                            return CompletableFuture.supplyAsync((Sup<T>) cb::call, delayedExecutor);
                        default:
                            throw newThrowable(UnsupportedOperationException.class,
                                "Unsupported value for enum %s: '%s'",
                                ExceptionHandlerOutcome.class.getName(),
                                eho
                            );
                    }
                }
            }, executorService).thenComposeAsync(Function.identity(), executorService);
            delay *= exp;
        }
        return result;
    }
}
