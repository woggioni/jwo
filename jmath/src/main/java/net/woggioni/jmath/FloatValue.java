package net.woggioni.jmath;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.Objects;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class FloatValue implements NumericType<FloatValue> {
    @EqualsAndHashCode.Include
    private final float value;

    @Override
    public FloatValue add(FloatValue other) {
        return new FloatValue(value + other.value);
    }

    @Override
    public FloatValue sub(FloatValue other) {
        return new FloatValue(value - other.value);
    }

    @Override
    public FloatValue mul(FloatValue other) {
        return new FloatValue(value * other.value);
    }

    @Override
    public FloatValue div(FloatValue other) {
        return new FloatValue(value / other.value);
    }

    @Override
    public FloatValue abs() {
        return new FloatValue(Math.abs(value));
    }

    @Override
    public FloatValue sqrt() {
        return new FloatValue((float) Math.sqrt(value));
    }

    @Override
    public int compareTo(FloatValue o) {
        return Comparator.<Float>naturalOrder().compare(value, o.value);
    }

    public static FloatValue of(float n) {
        return new FloatValue(n);
    }

    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
