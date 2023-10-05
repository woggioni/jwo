package net.woggioni.jwo;

import net.woggioni.jwo.internal.Tuple3Impl;

import java.util.Comparator;

public interface Tuple3<T, U, V> {
    T get_1();
    U get_2();
    V get_3();

    static <T, U, V> Tuple3<T, U, V> newInstance(T x, U y, V z) {
        return new Tuple3Impl<>(x, y, z);
    }

    static <X extends Comparable<X>, Y extends Comparable<Y>, Z extends Comparable<Z>>
    Comparator<Tuple3<X, Y, Z>> getComparator(Class<X> cls1, Class<Y> cls2, Class<Z> cls3) {
        return Comparator
                .comparing((Tuple3<X, Y, Z> t) -> t.get_1())
                .thenComparing((Tuple3<X, Y, Z> t) -> t.get_2())
                .thenComparing((Tuple3<X, Y, Z> t) -> t.get_3());
    }

    static <X extends Comparable<X>, Y extends Comparable<Y>, Z extends Comparable<Z>>
    Comparator<Tuple3<X, Y, Z>> getComparator(Tuple3<X, Y, Z> tuple) {
        return Comparator
                .comparing((Tuple3<X, Y, Z> t) -> t.get_1())
                .thenComparing((Tuple3<X, Y, Z> t) -> t.get_2())
                .thenComparing((Tuple3<X, Y, Z> t) -> t.get_3());
    }
}

