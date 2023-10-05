package net.woggioni.jmath;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BigIntegerExt {

    static public int gcd(int a, int b) {
        int tmp;
        while (b != 0) {
            tmp = a;
            a = b;
            b = tmp % b;
        }
        return a;
    }

    static public BigInteger gcd(BigInteger a, BigInteger b) {
        BigInteger tmp;
        while (!BigInteger.ZERO.equals(b)) {
            tmp = a;
            a = b;
            b = tmp.mod(b);
        }
        return a;
    }

    public static BigInteger mcm(BigInteger n1, BigInteger n2) {
        return n1.multiply(n2).divide(gcd(n1, n2));
    }
}
