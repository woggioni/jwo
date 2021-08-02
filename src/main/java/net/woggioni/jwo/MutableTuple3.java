package net.woggioni.jwo;

import net.woggioni.jwo.internal.MutableTuple3Impl;

public interface MutableTuple3<T, U, V> extends Tuple3<T, U , V> {
    void set_1(T value);
    void set_2(U value);
    void set_3(V value);

    static <T, U, V> MutableTuple3<T, U, V> newInstance(T x, U y, V z) {
        return new MutableTuple3Impl<>(x, y, z);
    }
}

