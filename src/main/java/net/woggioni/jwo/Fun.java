package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.util.function.Function;

@FunctionalInterface
public interface Fun<T, U> extends Function<T, U> {
    @Override
    @SneakyThrows
    default U apply(T t) {
        return exec(t);
    }

    U exec(T t) throws Throwable;
}
