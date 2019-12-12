package net.woggioni.jwo;

import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class Leb128Test {

    @Test
    public void testLong() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Long> numbers = Arrays.asList(0L, 1L, -3L, 5L, 7L, 8L, 125L, 255L, 10325L, -2000L, 1024L * 1024L * 1024L * 12L);

        numbers.forEach(n -> Leb128.encode(baos, n));

        byte[] bytes = baos.toByteArray();

        Leb128.Leb128Decoder decoder = new Leb128.Leb128Decoder(new ByteArrayInputStream(bytes));
        numbers.forEach(n -> Assert.assertEquals((long) n, decoder.decode()));
    }

    @Test
    public void testDouble() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Double> numbers = Arrays.asList(
                0.0, 1.5, -3.0, 0.5, 2.5, 8.25, -125.0, 255.0, 10325.0, -2000.0, 1024.0 * 1024 * 1024 * 12,
                -122.42200352825247, 37.80848009696725);

        numbers.forEach(n -> Leb128.encode(baos, n));

        byte[] bytes = baos.toByteArray();

        Leb128.Leb128Decoder decoder = new Leb128.Leb128Decoder(new ByteArrayInputStream(bytes));
        numbers.forEach(n -> Assert.assertEquals(n, decoder.decodeDouble(), 0.0));
    }


    @Test
    public void reverseTest() {
        long n = 101325;
        Assert.assertEquals(n, Leb128.reverse(Leb128.reverse(n)));
    }

    @Test
    @SneakyThrows
    public void reverseTestDouble() {
        double n = 0.25;
        long doubleLong = Double.doubleToLongBits(n);
        long reverse = Leb128.reverse(doubleLong);
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Leb128.encode(os, reverse);
            byte[] bytes = os.toByteArray();
            Assert.assertEquals(3, bytes.length);
        }
    }
}
