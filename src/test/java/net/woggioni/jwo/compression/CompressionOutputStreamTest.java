package net.woggioni.jwo.compression;

import lombok.SneakyThrows;
import net.woggioni.jwo.JWO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CompressionOutputStreamTest {

    private static Stream<Arguments> testParameters() {
        return Arrays.stream(CompressionFormat.values())
            .filter(format -> JWO.which(format.executable).isPresent())
            .flatMap(v -> IntStream.of(1, 3, 5, 7, 9).mapToObj(l -> Arguments.of(v, l)));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("testParameters")
    public void test(CompressionFormat compressionFormat, int level) {
        MessageDigest inputDigest = MessageDigest.getInstance("MD5");
        byte[] compressed;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try(InputStream is = new BufferedInputStream(new DigestInputStream(
                getClass().getResourceAsStream("/cracklib-small"), inputDigest));
            CompressionOutputStream compressionOutputStream =
                new CompressionOutputStream(new BufferedOutputStream(byteArrayOutputStream), compressionFormat, level)
        ) {
            int read;
            byte[] buffer = new byte[1024];
            while((read = is.read(buffer, 0, buffer.length)) >= 0) {
                compressionOutputStream.write(buffer, 0, read);
            }
        }
        compressed = byteArrayOutputStream.toByteArray();

        MessageDigest outputDigest = MessageDigest.getInstance("MD5");
        try(InputStream is = new DigestInputStream(new CompressionInputStream(
            new ByteArrayInputStream(compressed), compressionFormat, StreamMode.DECOMPRESSION), outputDigest)) {
            byte[] buffer = new byte[1024];
            while(is.read(buffer, 0, buffer.length) >= 0) {}
        }
        Assertions.assertArrayEquals(inputDigest.digest(), outputDigest.digest());
    }
}
