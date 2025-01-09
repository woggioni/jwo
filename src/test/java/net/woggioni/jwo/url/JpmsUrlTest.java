package net.woggioni.jwo.url;

import lombok.SneakyThrows;
import net.woggioni.jwo.JWO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JpmsUrlTest {
    @Test
    @SneakyThrows
    public void test() {
        JWO.registerUrlProtocolHandler();
        final var module = getClass().getModule();
        final var url = new URL("jpms://net.woggioni.jwo.test.module/my/test/resource/file.txt");
        final var baos = new ByteArrayOutputStream();
        try(final var is = url.openConnection().getInputStream()) {
            JWO.copy(is, baos);
        }
        final var content = baos.toString(StandardCharsets.UTF_8);
        Assertions.assertEquals("test", content);
    }
}
