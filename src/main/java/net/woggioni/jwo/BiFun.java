package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.util.function.BiFunction;

@FunctionalInterface
public interface BiFun<T, U, V> extends BiFunction<T, U, V> {
    @Override
    @SneakyThrows
    default V apply(final T t, final U u) {
        return exec(t, u);
    }

    V exec(final T t, final U u) throws Throwable;
}
