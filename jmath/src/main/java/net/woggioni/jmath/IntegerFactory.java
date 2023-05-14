package net.woggioni.jmath;

import lombok.Getter;

import java.math.BigInteger;

public class IntegerFactory implements NumericTypeFactory<IntegerValue> {
    @Override
    public IntegerValue getZero() {
        return new IntegerValue(BigInteger.ZERO);
    }

    @Override
    public IntegerValue getOne() {
        return new IntegerValue(BigInteger.ONE);
    }

    @Override
    public IntegerValue[] getArray(int size) {
        return new IntegerValue[size];
    }

    @Getter
    private static final NumericTypeFactory<IntegerValue> instance = new IntegerFactory();
}
