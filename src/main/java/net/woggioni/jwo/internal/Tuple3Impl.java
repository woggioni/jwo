package net.woggioni.jwo.internal;

import lombok.Data;
import net.woggioni.jwo.Tuple3;

@Data
public class Tuple3Impl<T, U, V> implements Tuple3<T, U, V> {
    private final T _1;
    private final U _2;
    private final V _3;
}
