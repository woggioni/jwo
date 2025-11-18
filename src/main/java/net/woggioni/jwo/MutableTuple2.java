package net.woggioni.jwo;

import net.woggioni.jwo.internal.MutableTuple2Impl;

public interface MutableTuple2<T, U> extends Tuple2<T, U> {
    void set_1(final T value);
    void set_2(final U value);

    static <T,U> MutableTuple2<T, U> newInstance(final T x, final U y) {
        return new MutableTuple2Impl<T, U>(x, y);
    }
}
