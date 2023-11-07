package net.woggioni.jwo.internal;

import lombok.Data;
import net.woggioni.jwo.Tuple2;

@Data
public class Tuple2Impl<T, U> implements Tuple2<T, U> {
    private final T _1;

    private  final U _2;
}

