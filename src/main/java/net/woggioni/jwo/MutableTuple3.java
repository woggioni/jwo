package net.woggioni.jwo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Comparator;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MutableTuple3<T, U, V> {
    public T _1;
    public U _2;
    public V _3;

    public static <X extends Comparable<X>, Y extends Comparable<Y>, Z extends Comparable<Z>>
    Comparator<MutableTuple3<X, Y, Z>> getComparator(Class<X> cls1, Class<Y> cls2, Class<Z> cls3) {
        return Comparator
                .comparing((MutableTuple3<X, Y, Z> t) -> t._1)
                .thenComparing((MutableTuple3<X, Y, Z> t) -> t._2)
                .thenComparing((MutableTuple3<X, Y, Z> t) -> t._3);
    }

    public static <X extends Comparable<X>, Y extends Comparable<Y>, Z extends Comparable<Z>>
    Comparator<MutableTuple3<X, Y, Z>> getComparator(MutableTuple3<X, Y, Z> tuple) {
        return Comparator
                .comparing((MutableTuple3<X, Y, Z> t) -> t._1)
                .thenComparing((MutableTuple3<X, Y, Z> t) -> t._2)
                .thenComparing((MutableTuple3<X, Y, Z> t) -> t._3);
    }
}

