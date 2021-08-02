package net.woggioni.jwo.internal;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.woggioni.jwo.MutableTuple2;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MutableTuple2Impl<T, U> implements MutableTuple2<T, U> {
    @Getter
    @Setter
    private T _1;



    @Getter
    @Setter
    private U _2;
}