package net.woggioni.jwo;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

public class VersionComparatorTest {

    private static class TestCaseProvider implements ArgumentsProvider {
        @Override
        @SneakyThrows
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("", "", 0),
                    Arguments.of("asdfg-2019", "asdfg-2019", 0),
                    Arguments.of("1.2", "1", 1),
                    Arguments.of("1.2", "1.0", 1),
                    Arguments.of("1.2", "1.10", -1),
                    Arguments.of("5.9.12.arch1-1", "5.8.0.arch1-1", 1),
                    Arguments.of("5.9.12.arch1-1", "5.10.0.arch1-1", -1),
                    Arguments.of("5.10.0.arch1-1", "5.10.0.arch1-3", -1),
                    Arguments.of("5.10.0.arch1-10", "5.10.0.arch1-3", 1),
                    Arguments.of("5.9.0.arch1-10", "5.10.0.arch1-3", -1),
                    Arguments.of("20191220.6871bff-1", "20201120.bc9cd0b-1", -1)
            );
        }
    }

    @ParameterizedTest(name="version1: \"{0}\", version2: \"{1}\", expected outcome: {2}")
    @ArgumentsSource(TestCaseProvider.class)
    public void test(String version1, String version2, int expectedOutcome) {
        Assertions.assertEquals(expectedOutcome, VersionComparator.cmp(version1, version2));
    }
}
