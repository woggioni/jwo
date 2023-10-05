package net.woggioni.jmath;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigInteger;
import java.util.stream.Stream;

import static java.math.BigInteger.valueOf;

public class RationalTest {

    private enum Operation {
        ADD, SUBTRACT, MULTIPLY, DIVIDE
    }

    private static class GcdTestCaseProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(valueOf(10), valueOf(25), valueOf(5)),
                    Arguments.of(valueOf(9), valueOf(7), valueOf(1)),
                    Arguments.of(valueOf(101), valueOf(3), valueOf(1)),
                    Arguments.of(valueOf(5), valueOf(105), valueOf(5)),
                    Arguments.of(valueOf(91), valueOf(91), valueOf(91)),
                    Arguments.of(valueOf(45), valueOf(81), valueOf(9)),
                    Arguments.of(valueOf(1), valueOf(1), valueOf(1)),
                    Arguments.of(valueOf(11), valueOf(34), valueOf(1))
            );
        }
    }
    @ParameterizedTest(name="input: {0}, {1}, expected outcome: {2}")
    @ArgumentsSource(GcdTestCaseProvider.class)
    void gcdTest(BigInteger n1, BigInteger n2, BigInteger expected) {
        Assertions.assertEquals(expected, BigIntegerExt.gcd(n1, n2));
    }

    private static class RationalTestCaseProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(
                            Rational.of(3, 4), Rational.of(1, 5),
                            Operation.ADD,
                            Rational.of(19, 20)),
                    Arguments.of(
                            Rational.of(1, 2), Rational.of(1, 2),
                            Operation.ADD,
                            Rational.of(1, 1)),
                    Arguments.of(
                            Rational.of(3, 4), Rational.of(2, 3),
                            Operation.MULTIPLY,
                            Rational.of(1, 2)),
                    Arguments.of(
                            Rational.of(4, 5), Rational.of(3, 2),
                            Operation.DIVIDE,
                            Rational.of(8, 15)),
                    Arguments.of(
                            Rational.of(1, 1), Rational.of(2, 1),
                            Operation.SUBTRACT,
                            Rational.of(1, -1))
            );
        }
    }

    @ParameterizedTest(name="input: {0}, {1}, operation: {2}, expected outcome: {3}")
    @ArgumentsSource(RationalTestCaseProvider.class)
    void rationalTest(Rational r1, Rational r2, Operation operation, Rational expected) {
        Rational result;
        switch (operation) {
            case ADD:
                result = r1.add(r2);
                break;
            case SUBTRACT:
                result = r1.sub(r2);
                break;
            case MULTIPLY:
                result = r1.mul(r2);
                break;
            case DIVIDE:
                result = r1.div(r2);
                break;
            default:
                throw new RuntimeException("This should never happen");
        }
        Assertions.assertEquals(expected, result);
    }


    @Test
    void sumTest() {
        Rational s5 = Rational.of(9, 22);
        Rational s6 = Rational.of(3, 22);
        Rational s4 = Rational.of(5, 22);
        Rational s3 = Rational.of(5, 22);
        Assertions.assertEquals(Rational.ONE, s5.add(s6).add(s4).add(s3));
    }
}