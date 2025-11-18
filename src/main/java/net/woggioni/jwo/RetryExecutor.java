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
    private final Executor executor = ForkJoinPool.commonPool();

    public enum ExceptionHandlerOutcome {
        THROW, CONTINUE
    }

    public interface ExceptionHandler {
        ExceptionHandlerOutcome handleError(final Throwable t);
    }

    public CompletableFuture<Void> submit(final Runnable cb) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executor);
    }

    public CompletableFuture<Void> submit(
            final Runnable cb,
        final ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public CompletableFuture<Void> submit(
            final Runnable cb,
            final ExceptionHandler exceptionHandler,
            final ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public CompletableFuture<Void> submit(
            final Runnable cb,
            final Double exp,
            final ExceptionHandler exceptionHandler,
            final ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }

    public CompletableFuture<Void> submit(
            final Runnable cb,
            final Duration initialDelay,
            final Double exp,
            final ExceptionHandler exceptionHandler,
            final ExecutorService executorService) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executorService);
    }
    public static CompletableFuture<Void> submit(
            final Runnable cb,
            final int maxAttempts,
            final Duration initialDelay,
            final Double exp,
            final ExceptionHandler exceptionHandler,
            final Executor executor) {
        return submit(() -> {
            cb.run();
            return null;
        }, maxAttempts, initialDelay, exp, exceptionHandler, executor);
    }

    public <T> CompletableFuture<T> submit(final Callable<T> cb) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executor);
    }

    public <T> CompletableFuture<T> submit(
            final Callable<T> cb,
            final Executor executor) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executor);
    }

    public <T> CompletableFuture<T> submit(
            final Callable<T> cb,
            final ExceptionHandler exceptionHandler,
            final Executor executor) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executor);
    }

    public <T> CompletableFuture<T> submit(
            final Callable<T> cb,
            final Double exp,
            final ExceptionHandler exceptionHandler,
            final ExecutorService executor) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executor);
    }

    public <T> CompletableFuture<T> submit(
            final Callable<T> cb,
            final Duration initialDelay,
            final Double exp,
            final ExceptionHandler exceptionHandler,
            final ExecutorService executor) {
        return submit(cb, maxAttempts, initialDelay, exp, exceptionHandler, executor);
    }

    public static <T> CompletableFuture<T> submit(
            final Callable<T> cb,
            final int maxAttempts,
            final Duration initialDelay,
            final Double exp,
            final ExceptionHandler exceptionHandler,
            final Executor executor) {
        CompletableFuture<T> result = CompletableFuture.supplyAsync((Sup<T>) cb::call, executor);
        double delay = initialDelay.toMillis();
        for(int i = 1; i <= maxAttempts; i++) {
            final int attempt = i;
            final double thisAttemptDelay = delay;
            result = result.handleAsync((BiFun<T, Throwable, CompletableFuture<T>>) (value, err) -> {
                final Optional<Throwable> causeOpt = Optional.ofNullable(err).map(Throwable::getCause);
                if(!causeOpt.isPresent()) {
                    return CompletableFuture.completedFuture(value);
                } else if(attempt == maxAttempts) {
                    throw causeOpt.get();
                } else {
                    final Throwable cause = causeOpt.get();
                    final ExceptionHandlerOutcome eho = exceptionHandler.handleError(cause);
                    switch (eho) {
                        case THROW:
                            throw cause;
                        case CONTINUE:
                            final Executor delayedExecutor = delayedExecutor(
                                (long) thisAttemptDelay,
                                TimeUnit.MILLISECONDS,
                                executor
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
            }, executor).thenComposeAsync(Function.identity(), executor);
            delay *= exp;
        }
        return result;
    }
}