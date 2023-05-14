package net.woggioni.jmath;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class DecimalValue implements NumericType<DecimalValue> {

    @EqualsAndHashCode.Include
    private final BigDecimal value;
    private final DecimalFactory decimalFactory;

    @Override
    public DecimalValue add(DecimalValue other) {
        return new DecimalValue(value.add(other.value), decimalFactory);
    }

    @Override
    public DecimalValue sub(DecimalValue other) {
        return new DecimalValue(value.subtract(other.value), decimalFactory);
    }

    @Override
    public DecimalValue mul(DecimalValue other) {
        return new DecimalValue(value.multiply(other.value), decimalFactory);
    }

    @Override
    public DecimalValue div(DecimalValue other) {
        return new DecimalValue(value.divide(other.value, decimalFactory.getCtx()), decimalFactory);
    }

    @Override
    public DecimalValue abs() {
        return new DecimalValue(value.abs(), decimalFactory);
    }

    @Override
    public DecimalValue sqrt() {
        return new DecimalValue(value.sqrt(decimalFactory.getCtx()), decimalFactory);
    }

    @Override
    public int compareTo(DecimalValue o) {
        return value.compareTo(o.value);
    }

    public static DecimalValue of(double n, DecimalFactory decimalFactory) {
        return new DecimalValue(BigDecimal.valueOf(n), decimalFactory);
    }

    public static DecimalValue of(double n) {
        return new DecimalValue(BigDecimal.valueOf(n), DecimalFactory.getInstance());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
