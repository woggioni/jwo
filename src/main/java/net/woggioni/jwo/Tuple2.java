package net.woggioni.jwo;

import net.woggioni.jwo.internal.Tuple2Impl;

import java.util.Comparator;
import java.util.function.Function;

public interface Tuple2<T, U> {
    T get_1();
    U get_2();

    static <T,U> Tuple2<T, U> newInstance(T x, U y) {
        return new Tuple2Impl<>(x, y);
    }

    static <X extends Comparable<X>, Y extends Comparable<Y>> Comparator<Tuple2<X, Y>> getComparator(Class<X> cls1, Class<Y> cls2) {
        return Comparator
                .comparing((Function<Tuple2<X, Y>, X>) Tuple2::get_1)
                .thenComparing(Tuple2::get_2);
    }

    static <X extends Comparable<X>, Y extends Comparable<Y>> Comparator<Tuple2<X, Y>> getComparator(Tuple2<X, Y> tuple) {
        return Comparator
                .comparing((Function<Tuple2<X, Y>, X>) Tuple2::get_1)
                .thenComparing(Tuple2::get_2);
    }
}

