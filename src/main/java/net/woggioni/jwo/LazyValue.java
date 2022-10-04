package net.woggioni.jwo;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class LazyValue<T> {
    private final Supplier<T> valueSupplier;

    private final Consumer<T> finalizer;
    private final MutableTuple2<T, Boolean> instance = MutableTuple2.newInstance(null, false);

    public LazyValue(Supplier<T> valueSupplier) {
        this(valueSupplier, null);
    }

    public T get() {
        if(instance.get_2()) return instance.get_1();
        synchronized (instance) {
            if(instance.get_2()) return instance.get_1();
            else {
                T value = valueSupplier.get();
                instance.set_1(value);
                instance.set_2(true);
                return value;
            }
        }
    }

    /**
     * Execute the finalized on the wrapped object, if it has been initialized, and then returns it.
     * It does nothing if {@link LazyValue#get()} has never been invoked.
     * @return the wrapped value if initialized, otherwise an empty {@link Optional}
     */
    public Optional<T> close() {
        T result = null;
        synchronized (instance) {
            if(instance.get_2()) {
                instance.set_2(false);
                result = instance.get_1();
                instance.set_1(null);
            }
        }
        if(result != null) finalizer.accept(result);
        return Optional.ofNullable(result);
    }
}
