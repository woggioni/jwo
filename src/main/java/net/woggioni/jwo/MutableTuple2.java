package net.woggioni.jwo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Comparator;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MutableTuple2<T, U> {
    public T _1;
    public U _2;

    public static <X extends Comparable<X>, Y extends Comparable<Y>>
    Comparator<MutableTuple2<X, Y>> getComparator(Class<X> cls1, Class<Y> cls2) {
        return Comparator
                .comparing((MutableTuple2<X, Y> t) -> t._1)
                .thenComparing((MutableTuple2<X, Y> t) -> t._2);
    }

    public static <X extends Comparable<X>, Y extends Comparable<Y>>
    Comparator<MutableTuple2<X, Y>> getComparator(MutableTuple2<X, Y> tuple) {
        return Comparator
                .comparing((MutableTuple2<X, Y> t) -> t._1)
                .thenComparing((MutableTuple2<X, Y> t) -> t._2);
    }
}
