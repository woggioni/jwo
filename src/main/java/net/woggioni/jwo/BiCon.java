package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface BiCon<T, U> extends BiConsumer<T, U> {
    @Override
    @SneakyThrows
    default void accept(final T t, final U u) {
        exec(t, u);
    }

    void exec(final T t, final U u) throws Throwable;
}
