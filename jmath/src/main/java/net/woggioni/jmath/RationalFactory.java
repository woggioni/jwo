package net.woggioni.jmath;

import lombok.Getter;

public class RationalFactory implements NumericTypeFactory<Rational> {
    @Override
    public Rational getZero() {
        return Rational.ZERO;
    }

    @Override
    public Rational getOne() {
        return Rational.ONE;
    }

    @Override
    public Rational[] getArray(int size) {
        return new Rational[size];
    }

    @Getter
    private static final NumericTypeFactory<Rational> instance = new RationalFactory();
}
