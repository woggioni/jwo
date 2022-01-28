package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.util.function.Supplier;

@FunctionalInterface
public interface Sup<T> extends Supplier<T> {
    @Override
    @SneakyThrows
    default T get() {
        return exec();
    }

    T exec() throws Exception;
}
