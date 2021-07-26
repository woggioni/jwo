package net.woggioni.jwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Random;

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
}
