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
import java.nio.file.*;
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
    void setup(@TempDir Path testDir) {
        testJar = Paths.get(System.getProperty("junit.jupiter.engine.jar"));
        referenceExtractionDestination = testDir.resolve("referenceExtraction");
        testExtractionDestination = testDir.resolve("testExtraction");
    }


    @SneakyThrows
    private static void referenceUnzipMethod(Path source, Path destination) {
        try(FileSystem fs = FileSystems.newFileSystem(source, (ClassLoader) null)) {
            for(Path root : fs.getRootDirectories()) {
                Files.walk(root)
                        .filter(it -> !Files.isDirectory(it)).forEach(new Consumer<Path>() {
                    @Override
                    @SneakyThrows
                    public void accept(Path path) {
                        Path newDir = destination.resolve(root.relativize(path).toString());
                        Files.createDirectories(newDir.getParent());
                        Files.copy(path, newDir);
                    }
                });
            }
        }
    }

    @SneakyThrows
    private static NavigableMap<String, Hash> hashFileTree(Path tree) {
        NavigableMap<String, Hash> result = new TreeMap<>();
        byte[] buffer = new byte[0x1000];
        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String key = tree.relativize(file).toString();
                if(!Objects.equals(JarFile.MANIFEST_NAME, key)) {
                    try (InputStream is = Files.newInputStream(file)) {
                        result.put(key, Hash.md5(is, buffer));
                    }
                } else {
                    Manifest manifest = new Manifest();
                    try (InputStream is = Files.newInputStream(file)) {
                        manifest.read(is);
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
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

    private static boolean compareFileTree(Path tree1, Path tree2) {
        NavigableMap<String, Hash> hash1 = hashFileTree(tree1);
        NavigableMap<String, Hash> hash2 = hashFileTree(tree2);
        return Objects.equals(hash1, hash2);
    }

    @SneakyThrows
    public void run(Supplier<ZipInputStream> zipInputStreamSupplier) {
        referenceUnzipMethod(testJar, referenceExtractionDestination);
        try(ZipInputStream zipInputStream = zipInputStreamSupplier.get()) {
            while(true) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
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
        Supplier<ZipInputStream> supplier = new Supplier<ZipInputStream>() {
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
        Supplier<ZipInputStream> supplier = new Supplier<ZipInputStream>() {
            @Override
            @SneakyThrows
            public ZipInputStream get() {
                return new JarExtractorInputStream(Files.newInputStream(testJar), testExtractionDestination, true, null);
            }
        };
        run(supplier);
    }
}
