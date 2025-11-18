package net.woggioni.jwo;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaVersionTest {

    @Test
    public void test() {
        final var current = JavaVersion.current();
        assertTrue(current.isCompatibleWith(current));
        assertTrue(current.isCompatibleWith(JavaVersion.VERSION_1_8));
        assertFalse(JavaVersion.VERSION_1_4.isCompatibleWith(current));
        assertFalse(JavaVersion.VERSION_1_4.isCompatibleWith(JavaVersion.VERSION_1_7));
        assertTrue(JavaVersion.VERSION_17.isCompatibleWith(JavaVersion.VERSION_1_8));
    }

    public static Stream<TestCase<Tuple2<JavaVersion, JavaVersion>, Integer>> comparatorTest() {
        return Stream.of(
            new TestCase<>(
                Tuple2.newInstance(JavaVersion.VERSION_1_4, JavaVersion.VERSION_1_4),
                0,
                null
            ),
            new TestCase<>(
                Tuple2.newInstance(JavaVersion.VERSION_1_1, JavaVersion.VERSION_1_4),
                -1,
                null
            ),
            new TestCase<>(
                Tuple2.newInstance(JavaVersion.VERSION_21, JavaVersion.VERSION_1_8),
                1,
                null
            ),
            new TestCase<>(
                Tuple2.newInstance(JavaVersion.VERSION_17, JavaVersion.VERSION_1_5),
                1,
                null
            ),
            new TestCase<>(
                Tuple2.newInstance(JavaVersion.VERSION_17, JavaVersion.VERSION_19),
                -1,
                null
            ),
            new TestCase<>(
                Tuple2.newInstance(JavaVersion.VERSION_21, JavaVersion.VERSION_20),
                1,
                null
            )
        );
    }

    @MethodSource
    @ParameterizedTest
    public void comparatorTest(final TestCase<Tuple2<JavaVersion, JavaVersion>, Integer> testCase) {
        final var comparator = Comparator.<JavaVersion>naturalOrder();
        final var pair = testCase.input();
        final var comparisonResult = Optional.of(comparator.compare(pair.get_1(), pair.get_2()))
            .map(v -> v / (v == 0 ? 1 : Math.abs(v)))
            .orElseGet(Assertions::fail);
        assertEquals(testCase.expectedOutput(), comparisonResult);
    }

    @Test
    @SneakyThrows
    public void parseClassTest() {
        try(final var is = getClass().getResourceAsStream("/net/woggioni/jwo/JWO.class")) {
            final var baos = new ByteArrayOutputStream();
            try (baos) {
                JWO.copy(is, baos);
            }
            final var jwoClassVersion = JavaVersion.forClass(baos.toByteArray());
            assertEquals(JavaVersion.VERSION_11, jwoClassVersion);
        }
    }
}
