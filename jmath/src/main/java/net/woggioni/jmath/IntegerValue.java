package net.woggioni.jmath;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class IntegerValue implements NumericType<IntegerValue> {
    @EqualsAndHashCode.Include
    private final BigInteger value;

    @Override
    public IntegerValue add(IntegerValue other) {
        return new IntegerValue(value.add(other.value));
    }

    @Override
    public IntegerValue sub(IntegerValue other) {
        return new IntegerValue(value.subtract(other.value));
    }

    @Override
    public IntegerValue mul(IntegerValue other) {
        return new IntegerValue(value.multiply(other.value));
    }

    @Override
    public IntegerValue div(IntegerValue other) {
        return new IntegerValue(value.divide(other.value));
    }

    @Override
    public IntegerValue abs() {
        return new IntegerValue(value.abs());
    }

    @Override
    public IntegerValue sqrt() {
        return new IntegerValue(value.sqrt());
    }

    @Override
    public int compareTo(IntegerValue o) {
        return value.compareTo(o.value);
    }

    public static IntegerValue of(long n) {
        return new IntegerValue(BigInteger.valueOf(n));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
