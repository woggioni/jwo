package net.woggioni.jwo.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.woggioni.jwo.MutableTuple3;

@Data
@AllArgsConstructor
public class MutableTuple3Impl<T, U, V> implements MutableTuple3<T, U, V> {
    private T _1;

    private U _2;

    private V _3;
}