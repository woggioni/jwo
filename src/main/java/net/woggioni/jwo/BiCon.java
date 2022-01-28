package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface BiCon<T, U> extends BiConsumer<T, U> {
    @Override
    @SneakyThrows
    default void accept(T t, U u) {
        exec(t, u);
    }

    void exec(T t, U u) throws Exception;
}
