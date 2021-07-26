package net.woggioni.jwo;

import lombok.SneakyThrows;
import net.woggioni.jwo.PathClassLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PathClassLoaderTest {

    Path testBundle = Path.of(System.getProperty("path.classloader.test.bundle"));

    @Test
    @SneakyThrows
    void test() {
        FileSystem fs = FileSystems.newFileSystem(testBundle, null);
        List<Path> paths = StreamSupport.stream(fs.getRootDirectories().spliterator(), false).flatMap(new Function<Path, Stream<Path>>() {
            @Override
            @SneakyThrows
            public Stream<Path> apply(Path path) {
                return Files.list(path)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".jar"));
            }
        }).flatMap(new Function<Path, Stream<Path>>() {
            @Override
            @SneakyThrows
            public Stream<Path> apply(Path path) {
                System.out.println(path.getFileName().toString());
                return StreamSupport.stream(FileSystems.newFileSystem(path, null).getRootDirectories().spliterator(), false);
            }
        }).collect(Collectors.toUnmodifiableList());
        PathClassLoader classLoader = new PathClassLoader(paths);
        Class<?>[] cls = new Class[1];
        Assertions.assertDoesNotThrow(() -> {
            cls[0] = classLoader.loadClass("com.google.common.collect.ImmutableMap");
        });
        Assertions.assertNotNull(cls[0]);
    }
}
