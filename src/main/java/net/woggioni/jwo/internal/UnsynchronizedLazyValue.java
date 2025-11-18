package net.woggioni.jwo.internal;

import lombok.RequiredArgsConstructor;
import net.woggioni.jwo.LazyValue;
import net.woggioni.jwo.MutableTuple2;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class UnsynchronizedLazyValue<T> implements LazyValue<T> {
    private final Supplier<T> valueSupplier;

    private final Consumer<T> finalizer;
    private final MutableTuple2<T, Boolean> instance = MutableTuple2.newInstance(null, false);

    public UnsynchronizedLazyValue(final Supplier<T> valueSupplier) {
        this(valueSupplier, null);
    }

    @Override
    public <U> LazyValue<U> map(final Function<T, U> fun) {
        return new UnsynchronizedLazyValue<>(() -> fun.apply(get()));
    }

    public Stream<T> stream() {
        return Stream.generate(this::get).limit(1);
    }

    @Override
    public T get() {
        if(instance.get_2()) return instance.get_1();
        else {
            final T value = valueSupplier.get();
            instance.set_1(value);
            instance.set_2(true);
            return value;
        }
    }

    @Override
    public <U> UnsynchronizedLazyValue<U> handle(final BiFunction<T, Throwable, U> bicon) {
        return new UnsynchronizedLazyValue<>(() -> {
            try {
                return bicon.apply(get(), null);
            } catch (final Throwable t) {
                return bicon.apply(null, t);
            }
        });
    }

    /**
     * Execute the finalizer on the wrapped object, if it has been initialized, and then returns it.
     * It does nothing if {@link UnsynchronizedLazyValue#get()} has never been invoked.
     * @return the wrapped value if initialized, otherwise an empty {@link Optional}
     */
    @Override
    public Optional<T> close() {
        T result = null;
        if(instance.get_2()) {
            instance.set_2(false);
            result = instance.get_1();
            instance.set_1(null);
        }
        if(result != null) finalizer.accept(result);
        return Optional.ofNullable(result);
    }
}
