package net.woggioni.jwo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LongMath {

    public static long ceilDiv(final long a, final long b) {
        if(a <= 0) throw new IllegalArgumentException("a must be positive");
        return 1 + (a - 1) / b;
    }
}
