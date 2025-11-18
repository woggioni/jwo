package net.woggioni.jwo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntegerMath {

    public static int ceilDiv(final int a, final int b) {
        if(a <= 0) throw new IllegalArgumentException("a must be positive");
        return 1 + (a - 1) / b;
    }
}
