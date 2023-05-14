package net.woggioni.jmath;

public interface NumericType<T> extends Comparable<T> {
    T add(T other);
    T sub(T other);
    T mul(T other);
    T div(T other);
    T abs();
    T sqrt();
}
