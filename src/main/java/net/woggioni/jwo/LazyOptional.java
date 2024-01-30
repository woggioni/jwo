package net.woggioni.jwo;


import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class LazyOptional<T> {
    private static final LazyOptional empty = new LazyOptional<>(() -> null);

    private final Supplier<T> producer;
    private final MutableTuple2<T, Boolean> instance = MutableTuple2.newInstance(null, false);

    public static <U> LazyOptional<U> of(Supplier<U> producer) {
        return new LazyOptional<>(producer);
    }

    public static <U> LazyOptional<U> or(LazyOptional<U>... opts) {
        return LazyOptional.of(() -> {
                for (LazyOptional<U> opt : opts) {
                    U value = opt.get();
                    if (value != null) return value;
                }
                return null;
            }
        );
    }

    public static <U> LazyOptional<U> empty() {
        return (LazyOptional<U>) empty;
    }

    public <U> LazyOptional<U> map(Function<T, U> mapping) {
        return LazyOptional.of(() -> {
            T prevValue = producer.get();
            if (prevValue == null) return null;
            else return mapping.apply(prevValue);
        });
    }

    public LazyOptional<T> filter(Predicate<T> predicate) {
        return LazyOptional.of(() -> {
            T prevValue = producer.get();
            if (predicate.test(prevValue)) return prevValue;
            else return null;
        });
    }

    public T get() {
        if (instance.get_2()) return instance.get_1();
        synchronized (instance) {
            if (instance.get_2()) return instance.get_1();
            else {
                T value = producer.get();
                instance.set_1(value);
                instance.set_2(true);
                return value;
            }
        }
    }

    public <U> LazyOptional<U> flatMap(Function<T, LazyOptional<U>> mapping) {
        return new LazyOptional<>(() -> {
            T prevValue = producer.get();
            if (prevValue == null) return null;
            else return mapping.apply(prevValue).get();
        });
    }

    public Optional<T> getOptional() {
        return Optional.ofNullable(get());
    }
}
