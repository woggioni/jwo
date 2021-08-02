package net.woggioni.jwo.internal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.woggioni.jwo.Tuple2;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Tuple2Impl<T, U> implements Tuple2<T, U> {
    @Getter
    private final T _1;

    @Getter
    private  final U _2;
}

