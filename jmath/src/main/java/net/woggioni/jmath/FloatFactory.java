package net.woggioni.jmath;

import lombok.Getter;

public class FloatFactory implements NumericTypeFactory<FloatValue> {
    @Override
    public FloatValue getZero() {
        return new FloatValue(0);
    }

    @Override
    public FloatValue getOne() {
        return new FloatValue(1);
    }

    @Override
    public FloatValue[] getArray(int size) {
        return new FloatValue[size];
    }

    @Getter
    private static final NumericTypeFactory<FloatValue> instance = new FloatFactory();
}
