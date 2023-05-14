package net.woggioni.jmath;

public interface NumericTypeFactory<T extends NumericType<T>> {
    T getZero();
    T getOne();
    default T getMinusOne() {
        return getZero().sub(getOne());
    }
    T[] getArray(int size);
}
