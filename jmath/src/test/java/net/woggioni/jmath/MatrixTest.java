package net.woggioni.jmath;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class MatrixTest {

    private static Matrix<Rational> testMatrix;
    private static Matrix<IntegerValue> integerTestMatrix;

    @BeforeAll
    public static void setup() {
        testMatrix = Matrix.of(RationalFactory.getInstance(), new Rational[][]{
                new Rational[]{
                        Rational.ONE, Rational.ZERO, Rational.of(-1, 2), Rational.ZERO, Rational.ZERO
                },
                new Rational[]{
                        Rational.ZERO, Rational.ONE, Rational.of(-1, 2), Rational.ZERO, Rational.ZERO
                },
                new Rational[]{
                        Rational.of(-4, 9), Rational.ZERO, Rational.ONE, Rational.ZERO, Rational.ZERO
                },
                new Rational[]{
                        Rational.of(-1, 3), Rational.ZERO, Rational.ZERO, Rational.ONE, Rational.ZERO
                },
                new Rational[]{
                        Rational.of(-2, 9), Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ONE
                },
        });

        NumericTypeFactory<IntegerValue> integerFactory = IntegerFactory.getInstance();
        integerTestMatrix = Matrix.of(integerFactory, 3, 3,
                IntStream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
    }

    @Test
    void trilTest() {
        NumericTypeFactory<IntegerValue> rationalFactory = IntegerFactory.getInstance();
        Matrix<IntegerValue> expected = Matrix.of(rationalFactory, 3, 3,
                IntStream.of(1, 0, 0, 4, 5, 0, 7, 8, 9).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Assertions.assertEquals(expected, integerTestMatrix.tril());
        Matrix<IntegerValue> expected2 = Matrix.of(rationalFactory, 3, 3,
                IntStream.of(-3, 0, 0, 4, -3, 0, 7, 8, -3).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Assertions.assertEquals(expected2, integerTestMatrix.tril(IntegerValue.of(-3)));
    }

    @Test
    void triuTest() {
        NumericTypeFactory<IntegerValue> rationalFactory = IntegerFactory.getInstance();
        Matrix<IntegerValue> expected = Matrix.of(rationalFactory, 3, 3,
                IntStream.of(1, 2, 3, 0, 5, 6, 0, 0, 9).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Assertions.assertEquals(expected, integerTestMatrix.triu());
        Matrix<IntegerValue> expected2 = Matrix.of(rationalFactory, 3, 3,
                IntStream.of(-3, 2, 3, 0, -3, 6, 0, 0, -3).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Assertions.assertEquals(expected2, integerTestMatrix.triu(IntegerValue.of(-3)));
    }

    @Test
    void transposeTest() {
        NumericTypeFactory<IntegerValue> integerFactory = IntegerFactory.getInstance();
        Matrix<IntegerValue> m = Matrix.of(integerFactory, 3, 3,
                (i, j) -> IntegerValue.of(1 + i * 3L + j));
        Matrix<IntegerValue> transpose = Matrix.of(integerFactory, 3, 3,
                (i, j) -> m.get(j, i));
        Assertions.assertEquals(transpose, m.transpose());
    }

    @Test
    void testInverse() {
        NumericTypeFactory<Rational> rationalFactory = RationalFactory.getInstance();
        int[] nums = new int[]{1, 2, 3, 4, 5, 6, 8, 7, 9};
        Matrix<Rational> m = Matrix.of(rationalFactory, 3, 3,
                (i, j) -> Rational.of(nums[i * 3 + j], 1));
        Matrix<Rational> inverse = m.invert();
        Matrix<Rational> identity = Matrix.identity(rationalFactory, 3);
        Assertions.assertEquals(identity, m.mmul(inverse));
        Assertions.assertEquals(identity, inverse.mmul(m));
    }

    @Test
    void addTest() {
        NumericTypeFactory<IntegerValue> integerFactory = IntegerFactory.getInstance();
        Matrix<IntegerValue> m2 = Matrix.of(integerFactory, 3, 3,
                (i, j) -> integerTestMatrix.get(i, j).mul(integerFactory.getMinusOne()));
        Matrix<IntegerValue> expected = Matrix.of(integerFactory, 3, 3,
                (i, j) -> integerFactory.getZero());
        Assertions.assertEquals(expected, integerTestMatrix.add(m2));
    }

    @Test
    void subTest() {
        NumericTypeFactory<IntegerValue> integerFactory = IntegerFactory.getInstance();
        Matrix<IntegerValue> expected = Matrix.of(integerFactory, 3, 3,
                (i, j) -> integerFactory.getZero());
        Assertions.assertEquals(expected, integerTestMatrix.sub(integerTestMatrix));
    }

    @Test
    void mulTest() {
        NumericTypeFactory<IntegerValue> integerFactory = IntegerFactory.getInstance();
        Matrix<IntegerValue> expected = Matrix.of(integerFactory, 3, 3,
                (i, j) -> integerTestMatrix.get(i, j).mul(integerTestMatrix.get(i, j)));
        Assertions.assertEquals(expected, integerTestMatrix.mul(integerTestMatrix));
    }

    @Test
    void divTest() {
        NumericTypeFactory<IntegerValue> integerFactory = IntegerFactory.getInstance();
        Matrix<IntegerValue> expected = Matrix.of(integerFactory, 3, 3,
                (i, j) -> IntegerValue.of(1));
        Assertions.assertEquals(expected, integerTestMatrix.div(integerTestMatrix));
    }

    @Test
    void linProdTest() {
        NumericTypeFactory<IntegerValue> integerFactory = IntegerFactory.getInstance();
        Matrix<IntegerValue> m = Matrix.of(integerFactory, 2, 5,
                IntStream.of(1, 4, 8, 2, 5, 7, 3, 6, 9, 0).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Matrix<IntegerValue> expected = Matrix.of(integerFactory, 2, 2,
                IntStream.of(110, 85, 85, 175).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Assertions.assertEquals(expected, m.mmul(m.transpose()));

        Matrix<IntegerValue> expected2 = Matrix.of(integerFactory, 5, 5,
                IntStream.of(50, 25, 50, 65, 5,
                                25, 25, 50, 35, 20,
                                50, 50, 100, 70, 40,
                                65, 35, 70, 85, 10,
                                5, 20, 40, 10, 25)
                        .mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Assertions.assertEquals(expected2, m.transpose().mmul(m));
    }

    @Test
    void linProdVectorTest() {
        NumericTypeFactory<IntegerValue> integerFactory = IntegerFactory.getInstance();
        Matrix<IntegerValue> m = Matrix.of(integerFactory, 3, 3,
                IntStream.of(1, 2, 3, 4, 5, 6, 8, 7, 9).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Vector<IntegerValue> v = Vector.of(integerFactory,
                IntStream.of(1, 2, 3).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Vector<IntegerValue> expected = Vector.of(integerFactory,
                IntStream.of(14, 32, 49).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Assertions.assertEquals(expected, m.mmul(v));
    }

    @Test
    void determinantTest() {
        NumericTypeFactory<Rational> rationalFactory = RationalFactory.getInstance();
        Matrix<Rational> m = Matrix.of(rationalFactory, 3, 3,
                IntStream.of(1, 2, 3, 4, 5, 6, 8, 7, 9).mapToObj(Rational::of).toArray(Rational[]::new));
        Assertions.assertEquals(Rational.of(-9), m.det());
        Assertions.assertEquals(Rational.of(-9), m.luDet());
        Matrix<Rational> m2 = Matrix.of(rationalFactory, 3, 3,
                IntStream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).mapToObj(Rational::of).toArray(Rational[]::new));
        Assertions.assertEquals(Rational.ZERO, m2.det());
        Assertions.assertEquals(Rational.ZERO, m2.luDet());
    }

    @Test
    void luTest() {
        NumericTypeFactory<Rational> rationalFactory = RationalFactory.getInstance();
        Matrix<Rational> m = Matrix.of(rationalFactory, 3, 3,
                IntStream.of(8, 7, 9, 1, 2, 3, 4, 5, 6).mapToObj(Rational::of).toArray(Rational[]::new));
        Matrix<Rational> lu = m.clone();
        Matrix.Pivot p = lu.lup();
        Matrix<Rational> result = p.mul(lu.tril(Rational.ONE).mmul(lu.triu()));
        Assertions.assertEquals(m, result);
    }

    @Test
    void mmulTest() {
        NumericTypeFactory<IntegerValue> integerFactory = IntegerFactory.getInstance();
        Matrix<IntegerValue> m = Matrix.of(integerFactory, 3, 3,
                IntStream.of(1, 2, 3, 4, 5, 6, 8, 7, 9).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Vector<IntegerValue> v = Vector.of(
                integerFactory, IntStream.of(1, 2, 3)
                        .mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Vector<IntegerValue> expected = Vector.of(integerFactory,
                IntStream.of(33, 33, 42).mapToObj(IntegerValue::of).toArray(IntegerValue[]::new));
        Assertions.assertEquals(expected, v.mul(m));
    }

    @Test
    public void linearSystemTest() {
        Vector<Rational> expectedSolution = Vector.of(RationalFactory.getInstance(),
                Rational.of(9, 14),
                Rational.of(9, 14),
                Rational.of(2, 7),
                Rational.of(3, 14),
                Rational.of(1, 7)
        );

        Vector<Rational> b = Vector.of(RationalFactory.getInstance(),
                Rational.of(1, 2), Rational.of(1, 2), Rational.ZERO, Rational.ZERO, Rational.ZERO
        );

        Vector<Rational> solution = testMatrix.solve(b);

        Assertions.assertEquals(expectedSolution, solution);
        Matrix<Rational> inverse = testMatrix.invert();
        Assertions.assertEquals(expectedSolution, inverse.mmul(b));
    }

    @Test
    public void randomLinearSystemTest() {
        int size = 10;
        Random rnd = new Random(101325);
        NumericTypeFactory<Rational> numericTypeFactory = RationalFactory.getInstance();
        Matrix.ValueGenerator<Rational> init = (i, j) ->
                Rational.of(rnd.nextInt(-1000 * size, 1000 * size), (long) 1000 * size);
        Matrix<Rational> mtx  = Matrix.of(numericTypeFactory, size, size, init);
        Matrix<Rational> lu = mtx.clone();
        Matrix.Pivot pivot = lu.lup();
        IntFunction<Rational> initVector = (i) -> Rational.of(rnd.nextInt(0, size), size);
        Vector<Rational> b = Vector.of(numericTypeFactory, size, initVector);
        Vector<Rational> x = lu.luSolve(b, pivot);
        Vector<Rational> error  = mtx.mmul(x).sub(b);
        Assertions.assertEquals(Rational.ZERO, error.norm());
    }

}