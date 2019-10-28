package net.woggioni.jwo.tuple;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Tuple3<T, U, V> {
    public final T _1;
    public final U _2;
    public final V _3;

    public static <X extends Comparable<X>, Y extends Comparable<Y>, Z extends Comparable<Z>> Comparator<Tuple3<X, Y, Z>> getComparator(Class<X> cls1, Class<Y> cls2, Class<Z> cls3) {
        return Comparator
            .comparing((Tuple3<X, Y, Z> t) -> t._1)
            .thenComparing((Tuple3<X, Y, Z> t) -> t._2)
            .thenComparing((Tuple3<X, Y, Z> t) -> t._3);
    }
}
