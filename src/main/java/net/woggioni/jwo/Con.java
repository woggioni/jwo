package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.util.function.Consumer;

@FunctionalInterface
public interface Con<T> extends Consumer<T> {
    @Override
    @SneakyThrows
    default void accept(final T t) {
        exec(t);
    }

    void exec(final T t) throws Throwable;
}
