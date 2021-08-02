package net.woggioni.jwo.internal;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.woggioni.jwo.MutableTuple3;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MutableTuple3Impl<T, U, V> implements MutableTuple3<T, U, V> {
    @Getter
    @Setter
    private T _1;

    @Getter
    @Setter
    private U _2;

    @Getter
    @Setter
    private V _3;
}