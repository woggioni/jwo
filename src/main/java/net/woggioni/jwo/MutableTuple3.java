package net.woggioni.jwo;

import net.woggioni.jwo.internal.MutableTuple3Impl;

public interface MutableTuple3<T, U, V> extends Tuple3<T, U , V> {
    void set_1(final T value);
    void set_2(final U value);
    void set_3(final V value);

    static <T, U, V> MutableTuple3<T, U, V> newInstance(final T x, final U y, final V z) {
        return new MutableTuple3Impl<>(x, y, z);
    }
}

