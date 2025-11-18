package net.woggioni.jwo;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Random;

public class CircularBufferTest {

    @Test
    @SneakyThrows
    public void test() {
        final MessageDigest streamDigest = MessageDigest.getInstance("MD5"), outputDigest = MessageDigest.getInstance("MD5");
        final InputStream is = new DigestInputStream(getClass().getResourceAsStream("/render_template_test.txt"), streamDigest);
        final CircularBuffer cb = new CircularBuffer(new InputStreamReader(is), 32);
        final Random rand = new Random();
        while (true) {
            final int b = cb.next();
            if (b < 0) break;
            if (rand.nextInt() % 2 == 0) {
                cb.prev();
            } else {
                final char c = (char) b;
                outputDigest.update((byte) b);
            }
        }
        Assertions.assertArrayEquals(streamDigest.digest(), outputDigest.digest());
    }
}
