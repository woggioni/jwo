package net.woggioni.jmath;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.IntFunction;

import static net.woggioni.jwo.Requirement.require;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Vector<T extends NumericType<T>> {
    private final NumericTypeFactory<T> numericTypeFactory;
    private final T[] values;

    private Vector(NumericTypeFactory<T> numericTypeFactory, int size) {
        this(numericTypeFactory, numericTypeFactory.getArray(size));
    }

    private Vector(NumericTypeFactory<T> numericTypeFactory, int size, IntFunction<T> valueGenerator) {
        this(numericTypeFactory, size);
        for (int i = 0; i < size; i++)
            set(i, valueGenerator.apply(i));
    }

    public int size() {
        return values.length;
    }

    public T get(int index) {
        return values[index];
    }

    public void set(int index, T value) {
        values[index] = value;
    }

    private void requireSameSize(Vector<T> other) {
        require(() -> other.size() == size()).otherwise(SizeException.class, "Vectors must be of same size");
    }

    public Vector<T> sum(Vector<T> other) {
        requireSameSize(other);
        return new Vector<>(numericTypeFactory, size(), index -> get(index).add(other.get(index)));
    }

    public Vector<T> sub(Vector<T> other) {
        requireSameSize(other);
        return new Vector<>(numericTypeFactory, size(), index -> get(index).sub(other.get(index)));
    }

    public Vector<T> mul(Vector<T> other) {
        requireSameSize(other);
        return new Vector<>(numericTypeFactory, size(), index -> get(index).mul(other.get(index)));
    }

    public Vector<T> div(Vector<T> other) {
        requireSameSize(other);
        return new Vector<>(numericTypeFactory, size(), index -> get(index).div(other.get(index)));
    }

    public Vector<T> mul(Matrix<T> m) {
        return Vector.of(numericTypeFactory, size(), i -> {
            T result = numericTypeFactory.getZero();
            for (int j = 0; j < m.getRows(); j++) {
                result = result.add(get(j).mul(m.get(j, i)));
            }
            return result;
        });
    }

    public T innerProduct(Vector<T> other) {
        requireSameSize(other);
        T result = numericTypeFactory.getZero();
        int sz = size();
        for (int i = 0; i < sz; i++)
            result = result.add(get(i).mul(other.get(i)));
        return result;
    }

    public T norm() {
        T result = numericTypeFactory.getZero();
        int sz = size();
        for (int i = 0; i < sz; i++) {
            T value = get(i);
            result = result.add(value.mul(value));
        }
        return result;
    }

    public T abs() {
        return norm().sqrt();
    }

    @Override
    public Vector<T> clone() {
        int sz = size();
        Vector<T> result = new Vector<>(numericTypeFactory, sz);
        for (int i = 0; i < sz; i++) {
            result.set(i, get(i));
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector vector)) {
            return false;
        }
        if (size() != vector.size()) return false;
        for (int i = 0; i < size(); i++) {
            if (!Objects.equals(get(i), vector.get(i))) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    public static <T extends NumericType<T>> Vector<T> of(NumericTypeFactory<T> numericTypeFactory, T... values) {
        return new Vector<>(numericTypeFactory, values);
    }

    public static <T extends NumericType<T>> Vector<T> of(NumericTypeFactory<T> numericTypeFactory, int size) {
        return new Vector<>(numericTypeFactory, size);
    }

    public static <T extends NumericType<T>> Vector<T> of(NumericTypeFactory<T> numericTypeFactory,
                                                          int size,
                                                          IntFunction<T> generator) {
        return new Vector<>(numericTypeFactory, size, generator);
    }
}
