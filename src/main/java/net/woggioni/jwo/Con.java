package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.util.function.Consumer;

@FunctionalInterface
public interface Con<T> extends Consumer<T> {
    @Override
    @SneakyThrows
    default void accept(T t) {
        exec(t);
    }

    void exec(T t) throws Exception;
}
