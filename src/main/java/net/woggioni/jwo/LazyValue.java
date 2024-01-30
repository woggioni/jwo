package net.woggioni.jwo;

import lombok.RequiredArgsConstructor;
import net.woggioni.jwo.internal.SynchronizedLazyValue;
import net.woggioni.jwo.internal.UnsynchronizedLazyValue;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface LazyValue<T> {

    enum ThreadSafetyMode {
        SYNCHRONIZED, NONE
    }

    T get();

    <U> LazyValue<U> handle(BiFunction<T, Throwable, U> bifun);

    <U> LazyValue<U> map(Function<T, U> fun);

    Stream<T> stream();

    /**
     * Execute the finalizer on the wrapped object, if it has been initialized, and then returns it.
     * It does nothing if {@link LazyValue#get()} has never been invoked.
     * @return the wrapped value if initialized, otherwise an empty {@link Optional}
     */
    Optional<T> close();

    static <T> LazyValue<T> of(Supplier<T> supplier, ThreadSafetyMode locking, Consumer<T> finalizer) {
        LazyValue<T> result;
        switch (locking) {
            case SYNCHRONIZED:
                result = new SynchronizedLazyValue<>(supplier, finalizer);
                break;
            case NONE:
                result = new UnsynchronizedLazyValue<>(supplier, finalizer);
                break;
            default:
                throw new RuntimeException("This should never happen");
        }
        return result;
    }

    static <T> LazyValue<T> of(Supplier<T> supplier, ThreadSafetyMode locking) {
        return of(supplier, locking, null);
    }
}
