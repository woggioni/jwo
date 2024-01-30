package net.woggioni.jmath.benchmark;

import net.woggioni.jmath.Matrix;
import net.woggioni.jmath.NumericTypeFactory;
import net.woggioni.jmath.Rational;
import net.woggioni.jmath.RationalFactory;
import net.woggioni.jmath.Vector;

import java.util.Optional;
import java.util.Random;
import java.util.function.IntFunction;

public class Main {
    public static void main(String[] args) {
        int size = Optional.of(args)
                .filter(it -> it.length > 0)
                .map(it -> it[0])
                .map(Integer::parseInt)
                .orElse(3);
        Random rnd = new Random(101325);
        NumericTypeFactory<Rational> numericTypeFactory = RationalFactory.getInstance();
        Matrix.ValueGenerator<Rational> init = (i, j) ->
                Rational.of(rnd.nextInt(-1000 * size, 1000 * size), (long) 1000 * size);
        Matrix<Rational> mtx  = Matrix.of(numericTypeFactory, size, size, init);
        Matrix<Rational> lu = mtx.clone();
        Matrix.Pivot pivot = lu.lup();
        for(int i = 0; i < size; i++) {
            IntFunction<Rational> initVector = (j) -> Rational.of(rnd.nextInt(0, size), size);
            Vector<Rational> b = Vector.of(numericTypeFactory, size, initVector);
            Vector<Rational> x = lu.luSolve(b, pivot);
            Vector<Rational> error = mtx.mmul(x).sub(b);
            Rational norm = error.norm();
            if(norm.compareTo(Rational.ZERO) != 0) {
                throw new RuntimeException(String.format("Error is %s", norm));
            }
        }
    }
}
