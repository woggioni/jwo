package net.woggioni.jwo;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LazyValue<T> {
    private final Supplier<T> valueSupplier;
    private final MutableTuple2<T, Boolean> instance = MutableTuple2.newInstance(null, false);

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
}
