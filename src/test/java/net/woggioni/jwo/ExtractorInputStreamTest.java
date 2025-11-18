package net.woggioni.jwo;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ExtractorInputStreamTest {
    private Path testJar;
    private Path referenceExtractionDestination;
    private Path testExtractionDestination;

    @BeforeEach
    void setup(final @TempDir Path testDir) {
        testJar = Paths.get(System.getProperty("junit.jupiter.engine.jar"));
        referenceExtractionDestination = testDir.resolve("referenceExtraction");
        testExtractionDestination = testDir.resolve("testExtraction");
    }


    @SneakyThrows
    private static void referenceUnzipMethod(Path source, Path destination) {
        try(final FileSystem fs = FileSystems.newFileSystem(source, (ClassLoader) null)) {
            for(final Path root : fs.getRootDirectories()) {
                Files.walk(root)
                        .filter(it -> !Files.isDirectory(it)).forEach(new Consumer<Path>() {
                    @Override
                    @SneakyThrows
                    public void accept(final Path path) {
                        final Path newDir = destination.resolve(root.relativize(path).toString());
                        Files.createDirectories(newDir.getParent());
                        Files.copy(path, newDir);
                    }
                });
            }
        }
    }

    @SneakyThrows
    private static NavigableMap<String, Hash> hashFileTree(Path tree) {
        final NavigableMap<String, Hash> result = new TreeMap<>();
        final byte[] buffer = new byte[0x1000];
        final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                final String key = tree.relativize(file).toString();
                if(!Objects.equals(JarFile.MANIFEST_NAME, key)) {
                    try (InputStream is = Files.newInputStream(file)) {
                        result.put(key, Hash.md5(is, buffer));
                    }
                } else {
                    final Manifest manifest = new Manifest();
                    try (final InputStream is = Files.newInputStream(file)) {
                        manifest.read(is);
                    }
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        manifest.write(baos);
                    } finally {
                        baos.close();
                    }
                    result.put(key, Hash.md5(new ByteArrayInputStream(baos.toByteArray()), buffer));
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(tree, visitor);
        return result;
    }

    private static boolean compareFileTree(final Path tree1, final Path tree2) {
        final NavigableMap<String, Hash> hash1 = hashFileTree(tree1);
        final NavigableMap<String, Hash> hash2 = hashFileTree(tree2);
        return Objects.equals(hash1, hash2);
    }

    @SneakyThrows
    public void run(final Supplier<ZipInputStream> zipInputStreamSupplier) {
        referenceUnzipMethod(testJar, referenceExtractionDestination);
        try(final ZipInputStream zipInputStream = zipInputStreamSupplier.get()) {
            while(true) {
                final ZipEntry zipEntry = zipInputStream.getNextEntry();
                if(zipEntry == null) break;
                JWO.write2Stream(new NullOutputStream(), zipInputStream);
                zipInputStream.closeEntry();
            }
        }
        Assertions.assertTrue(compareFileTree(referenceExtractionDestination, testExtractionDestination));
    }

    @Test
    @SneakyThrows
    public void zipExtractorInputStreamTest() {
        final Supplier<ZipInputStream> supplier = new Supplier<ZipInputStream>() {
            @Override
            @SneakyThrows
            public ZipInputStream get() {
                return new ZipExtractorInputStream(Files.newInputStream(testJar), testExtractionDestination);
            }
        };
        run(supplier);
    }

    @Test
    @SneakyThrows
    public void jarExtractorInputStreamTest() {
        final Supplier<ZipInputStream> supplier = new Supplier<ZipInputStream>() {
            @Override
            @SneakyThrows
            public ZipInputStream get() {
                return new JarExtractorInputStream(Files.newInputStream(testJar), testExtractionDestination, true, null);
            }
        };
        run(supplier);
    }
}
