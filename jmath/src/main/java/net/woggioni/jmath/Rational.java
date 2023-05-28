package net.woggioni.jmath;

import java.math.BigInteger;
import java.util.Objects;

import static net.woggioni.jmath.BigIntegerExt.mcm;

public class Rational implements NumericType<Rational> {

    public static final Rational ZERO = new Rational(0, 1);
    public static final Rational ONE = new Rational(1, 1);
    public static final Rational MINUS_ONE = new Rational(-1, 1);
    public static final BigInteger MINUS_ONE_BI = BigInteger.ZERO.subtract(BigInteger.ONE);

    private BigInteger num;
    private BigInteger den;

    @Override
    public Rational add(Rational other) {
        return new Rational(
                this.num.multiply(other.den).add(other.num.multiply(this.den)),
                this.den.multiply(other.den)
        ).simplify();
    }

    @Override
    public Rational sub(Rational other) {
        return new Rational(
                this.num.multiply(other.den).subtract(other.num.multiply(this.den)),
                this.den.multiply(other.den)
        ).simplify();
    }

    @Override
    public Rational mul(Rational other) {
        return new Rational(this.num.multiply(other.num), this.den.multiply(other.den)).simplify();
    }

    @Override
    public Rational div(Rational other) {
        return new Rational(this.num.multiply(other.den), this.den.multiply(other.num)).simplify();
    }

    @Override
    public Rational sqrt() {
        return new Rational(num.sqrt(), den.sqrt());
    }

    @Override
    public Rational abs() {
        return new Rational(num.abs(), den.abs());
    }

    public Rational(BigInteger num, BigInteger den) {
        this.num = num;
        this.den = den;
        simplify();
    }

    public Rational(long num, long den) {
        this(BigInteger.valueOf(num), BigInteger.valueOf(den));
    }

    public static Rational of(long num, long den) {
        return new Rational(num, den);
    }

    public static Rational of(long n) {
        return new Rational(n, 1);
    }
    public static Rational of(BigInteger num) {
        return new Rational(num, BigInteger.ONE);
    }
    public static Rational of(BigInteger num, BigInteger den) {
        return new Rational(num, den);
    }
    private Rational simplify() {
        BigInteger gcd = BigIntegerExt.gcd(num.abs(), den.abs());
        num = num.divide(gcd);
        den = den.divide(gcd);
        if(den.compareTo(BigInteger.ZERO) < 0) {
            num = num.multiply(MINUS_ONE_BI);
            den = den.multiply(MINUS_ONE_BI);
        }
        return this;
    }

    public BigInteger getNum() {
        return num;
    }

    public BigInteger getDen() {
        return den;
    }
    @Override
    public String toString() {
        String result;
        if (Objects.equals(BigInteger.ZERO, num) && !Objects.equals(BigInteger.ZERO, den)) result = "0";
        else if (Objects.equals(BigInteger.ONE, den.abs())) result = num.multiply(den.abs().divide(den)).toString();
        else {
            boolean negative = Objects.equals(num, num.abs()) ^ Objects.equals(den, den.abs());
            result = String.format("%s%d/%d", negative ? "-" : "", num.abs(), den.abs());
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rational rational = (Rational) o;
        BigInteger mcm = mcm(den, rational.den);
        BigInteger v1 = mcm.divide(den).multiply(num);
        BigInteger v2 = mcm.divide(rational.den).multiply(rational.num);
        return Objects.equals(v1, v2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, den);
    }

    @Override
    public int compareTo(Rational o) {
        BigInteger mcm = mcm(getDen(), o.getDen());
        BigInteger n1 = mcm.divide(getDen()).multiply(getNum());
        BigInteger n2 = mcm.divide(o.getDen()).multiply(o.getNum());
        return n1.compareTo(n2);
    }
}