package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.util.function.Predicate;

@FunctionalInterface
public interface Pre<T> extends Predicate<T> {
    @Override
    @SneakyThrows
    default boolean test(final T t) {
        return exec(t);
    }

    boolean exec(final T t) throws Throwable;
}
