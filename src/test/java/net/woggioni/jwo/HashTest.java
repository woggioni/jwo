package net.woggioni.jwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.ByteArrayInputStream;
import java.util.Random;
import java.util.stream.Stream;

public class HashTest {

    private byte[] sample;

    @BeforeEach
    public void setup() {
        sample = new byte[0x400];
        Random random = new Random(0xdeadbeef);
        random.nextBytes(sample);
    }

    @Test
    public void checkHashIsRepeatableAndComparesCorrectly() {
        byte[] buffer = new byte[0x1000];
        Hash hash1 = Hash.md5(new ByteArrayInputStream(sample), buffer);
        Hash hash2 = Hash.md5(new ByteArrayInputStream(sample), buffer);
        Assertions.assertEquals(hash1.hashCode(), hash2.hashCode());
        Assertions.assertEquals(hash1, hash2);
        Assertions.assertEquals(hash1.toString(), hash2.toString());
    }

    @Test
    public void checkReversingTheSampleGivesDifferentHash() {
        byte[] reverseSample = new byte[sample.length];
        for(int i = 0; i < sample.length; i++) {
            reverseSample[reverseSample.length - i - 1] = sample[i];
        }

        byte[] buffer = new byte[0x1000];
        Hash hash1 = Hash.md5(new ByteArrayInputStream(sample), buffer);
        Hash hash2 = Hash.md5(new ByteArrayInputStream(reverseSample), buffer);
        Assertions.assertNotEquals(hash1.hashCode(), hash2.hashCode());
        Assertions.assertNotEquals(hash1, hash2);
        Assertions.assertNotEquals(hash1.toString(), hash2.toString());
    }


    private static class HexTestArguments implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(
            ExtensionContext extensionContext
        ) {
            return Stream.of(
                Arguments.of("A41D767EEF6084823F250E954BDD48CF", null),
                Arguments.of("A41D767eeF6084823f250E954bdD48CF", null),
                Arguments.of("A41D767EEF6084823F250E954BDD48C", IllegalArgumentException.class),
                Arguments.of("A41D767xEF6084823F250E954BDD48C", IllegalArgumentException.class),
                Arguments.of("A41D767ZeF6084823f250E954bdD48C", IllegalArgumentException.class),
                Arguments.of("A41D767eeF6084823f250E954bdD4x", IllegalArgumentException.class)
            );
        }
    }

    @ArgumentsSource(HexTestArguments.class)
    @ParameterizedTest
    public void hexTest(String sourceString, Class<? extends Throwable> t) {
        if(t != null) {
            Assertions.assertThrows(t, () ->
                Hash.hexToBytes(sourceString)
            );
        } else {
            byte[] bytes = Hash.hexToBytes(sourceString);
            Assertions.assertEquals(sourceString.length() / 2, bytes.length);
            Assertions.assertEquals(sourceString.toUpperCase(), Hash.bytesToHex(bytes));
        }
    }

    @ArgumentsSource(HexTestArguments.class)
    @ParameterizedTest
    public void hexTest2(String sourceString, Class<? extends Throwable> t) {
        if(t != null) {
            Assertions.assertThrows(t, () ->
                    Hash.hexToBytes2(sourceString)
            );
        } else {
            byte[] bytes = Hash.hexToBytes2(sourceString);
            Assertions.assertEquals(sourceString.length() / 2, bytes.length);
            Assertions.assertEquals(sourceString.toUpperCase(), Hash.bytesToHex(bytes));
        }
    }
}
