package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.util.function.Predicate;

@FunctionalInterface
public interface Pre<T> extends Predicate<T> {
    @Override
    @SneakyThrows
    default boolean test(T t) {
        return exec(t);
    }

    boolean exec(T t) throws Throwable;
}
