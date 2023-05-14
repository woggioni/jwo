package net.woggioni.jmath;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;

@RequiredArgsConstructor
public class DecimalFactory implements NumericTypeFactory<DecimalValue> {

    @Getter(AccessLevel.PACKAGE)
    private final MathContext ctx;
    @Override
    public DecimalValue getZero() {
        return new DecimalValue(BigDecimal.ZERO, this);
    }

    @Override
    public DecimalValue getOne() {
        return new DecimalValue(BigDecimal.ONE, this);
    }

    @Override
    public DecimalValue[] getArray(int size) {
        return new DecimalValue[size];
    }

    @Getter
    private static final DecimalFactory instance = new DecimalFactory(MathContext.DECIMAL128);
}
