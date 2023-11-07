package net.woggioni.jwo.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.woggioni.jwo.MutableTuple2;

@Data
@AllArgsConstructor
public class MutableTuple2Impl<T, U> implements MutableTuple2<T, U> {
    private T _1;
    private U _2;
}