package net.woggioni.jwo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static net.woggioni.jwo.CollectionUtils.immutableList;
import static net.woggioni.jwo.CollectionUtils.newArrayList;
import static net.woggioni.jwo.Misc.CRACKLIB_RESOURCE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JWOTest {

    @Test
    public void flatMapTest() {
        final Stream<Integer> s = Stream.of(3, 4);
        final List<Integer> l = JWO.flatMap(s, (n) -> {
            if (n > 3) return Optional.of(n);
            else return Optional.empty();
        }).collect(Collectors.toList());
        assertEquals(Collections.singletonList(4), l);
    }

    @Test
    public void optional2StreamTest() {
        final Integer integer = 3;
        Optional<Integer> s = Optional.of(integer);
        JWO.optional2Stream(s).forEach(n -> assertEquals(integer, n));
        s = Optional.empty();
        JWO.optional2Stream(s).forEach(n -> Assertions.fail(
            "Stream should have been empty and this piece of code never executed")
        );
    }

    @Test
    public void optional2StreamTest2() {
        final Integer integer = 3;
        final Optional<Integer> o1 = Optional.of(integer);
        final Integer integer2 = 3;
        final Optional<Integer> o2 = Optional.of(integer2);
        final var values = JWO.optional2Stream(o1, Optional.empty(), o2)
            .collect(Collectors.toList());
        assertEquals(Arrays.asList(integer, integer2), values);
    }

    @Test
    public void optionalOrTest() {
        final Integer integer = 3;
        final Optional<Integer> o1 = Optional.of(integer);
        final Optional<Integer> o2 = Optional.of(integer);
        assertEquals(o2, JWO.or(Optional.empty(), o2));
        assertEquals(o1, JWO.or(o1, Optional.empty()));
        assertEquals(Optional.empty(), JWO.or(Optional.empty(), Optional.empty()));
    }


    @RequiredArgsConstructor
    enum IndexOfWithEscapeTestCase {
        SIMPLE(" dsds $sdsa \\$dfivbdsf \\\\$sdgsga", '$', '\\',
            immutableList(6, 25)),
        SIMPLE2("asdasd$$vdfv$", '$', '$',
            immutableList(12)),
        NO_NEEDLE("asdasd$$vdfv$", '#', '\\',
            immutableList()),
        ESCAPED_NEEDLE("asdasd$$vdfv$#sdfs", '#', '$',
            immutableList()),
        NOT_ESCAPED_NEEDLE("asdasd$$#vdfv$#sdfs", '#', '$',
            immutableList(8)),

        SDFSD("\n${sys:user.home}${env:HOME}", ':', '\\',
            immutableList(6, 22));
        final String haystack;
        final Character needle;

        final Character escape;

        final List<Integer> solution;
    }

    @ParameterizedTest
    @EnumSource(IndexOfWithEscapeTestCase.class)
    public void testIndexOfWithEscape(final IndexOfWithEscapeTestCase testCase) {
        final String haystack = testCase.haystack;
        final List<Integer> solution = newArrayList();
        int i = 0;
        while (true) {
            i = JWO.indexOfWithEscape(haystack, testCase.needle, testCase.escape, i, haystack.length());
            if (i < 0) break;
            solution.add(i);
            ++i;
        }
        assertEquals(testCase.solution, solution);
    }

    @Test
    @SneakyThrows
    public void testRenderTemplate() {
        final Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("author", "John Doe");
        valuesMap.put("date", "2020-03-25 16:22");
        valuesMap.put("adjective", "simple");
        final String expected = """
            This is a simple test made by John Doe on 2020-03-25 16:22. It's really simple!
            /home/user
            /home/user
            defaultValue
            ;=%x$!~L+LJr?50l.^{veaS'zLHo=!}wT
            """;
        final Map<String, Map<String, Object>> contextMap = new MapBuilder<String, Map<String, Object>>()
            .entry("env",
                new MapBuilder<String, String>()
                    .entry("HOME", "/home/user")
                    .build(TreeMap::new, Collections::unmodifiableMap)
            )
            .entry("sys",
                new MapBuilder<String, String>()
                    .entry("user.home", "/home/user")
                    .build(TreeMap::new, Collections::unmodifiableMap)
            ).build(TreeMap::new, Collections::unmodifiableMap);
        try (final Reader reader = new InputStreamReader(
            JWOTest.class.getResourceAsStream("/render_template_test.txt"))) {
            final String rendered = JWO.renderTemplate(reader, valuesMap, contextMap);
            assertEquals(expected, rendered);
        }
        try (final Reader reader = new InputStreamReader(
            JWOTest.class.getResourceAsStream("/render_template_test.txt"))) {
            final String rendered = JWO.renderTemplate(JWO.readAll(reader), valuesMap, contextMap);
            assertEquals(expected, rendered);
        }
    }


    public static String renderTemplateNaive(final String template, final Map<String, Object> valuesMap) {
        final StringBuilder formatter = new StringBuilder(template);
        final Object absent = new Object();

        final Matcher matcher = Pattern.compile("\\$\\{(\\w+)}").matcher(template);

        while (matcher.find()) {
            final String key = matcher.group(1);

            final String formatKey = String.format("${%s}", key);
            final int index = formatter.indexOf(formatKey);

            // If the key is present:
            //  - If the value is not null, then replace the variable for the value
            //  - If the value is null, replace the variable for empty string
            // If the key is not present, leave the variable untouched.
            if (index != -1) {
                final Object value = valuesMap.getOrDefault(key, absent);
                if (value != absent) {
                    final String valueStr = value != null ? value.toString() : "";
                    formatter.replace(index, index + formatKey.length(), valueStr);
                }
            }
        }
        return formatter.toString();
    }

    @Test
    @SneakyThrows
    @EnabledOnOs(OS.LINUX)
    public void uidTest(final @TempDir Path testDir) {
        final PosixFileAttributes pfa = Files.readAttributes(testDir, PosixFileAttributes.class);
        final UserPrincipal expectedUser = pfa.owner();
        final Class<? extends UserPrincipal> userClass = expectedUser.getClass();
        final Method m = userClass.getDeclaredMethod("uid");
        m.setAccessible(true);
        final int expectedUserId = (Integer) m.invoke(expectedUser);
        final int uid = (int) JWO.uid();
        assertEquals(expectedUserId, uid);
    }

    @Nested
    class ReplaceFileIfDifferentTest {

        private interface CommonInterface {
            void replaceFileIfDifferent(
                    final Supplier<InputStream> inputStreamSupplier,
                    final Path destination,
                    final FileAttribute<?>... attrs
            );
        }


        @RequiredArgsConstructor
        public enum MethodToTest {
            STREAM_METHOD((Supplier<InputStream> inputStreamSupplier,
                           Path destination,
                           FileAttribute<?>... attrs) -> {
                JWO.replaceFileIfDifferent(inputStreamSupplier.get(), destination, attrs);
            }),
            SUPPLIER_METHOD(JWO::replaceFileIfDifferent);

            private final CommonInterface xface;

            public void replaceFileIfDifferent(
                    final Supplier<InputStream> inputStreamSupplier,
                    final Path destination,
                    final FileAttribute<?>... attrs
            ) {
                xface.replaceFileIfDifferent(inputStreamSupplier, destination, attrs);
            }
        }

        private static final Supplier<InputStream> source = (Sup<InputStream>) () -> Optional.ofNullable(
            ReplaceFileIfDifferentTest.class.getResourceAsStream(CRACKLIB_RESOURCE)
        ).orElseThrow(Assertions::fail);

        @TempDir
        private Path testDir;

        @SneakyThrows
        @ParameterizedTest
        @EnumSource(MethodToTest.class)
        public void ensureFileCopy(final MethodToTest methodToTest) {
            final var dest = testDir.resolve("cracklib-small");
            methodToTest.replaceFileIfDifferent(source, dest);
            final Hash newFileHash, newContentHash;
            try (final var inputStream = source.get()) {
                newContentHash = Hash.md5(inputStream);
            }
            try (final var inputStream = Files.newInputStream(dest)) {
                newFileHash = Hash.md5(inputStream);
            }
            assertEquals(newContentHash, newFileHash);
        }

        @SneakyThrows
        @ParameterizedTest
        @EnumSource(MethodToTest.class)
        public void ensureNoWriteWithNoChange(final MethodToTest methodToTest) {
            final var dest = testDir.resolve("cracklib-small");
            try (final var inputStream = source.get()) {
                Files.copy(inputStream, dest);
            }
            final var initialTime = Files.getLastModifiedTime(dest);
            methodToTest.replaceFileIfDifferent(source, dest);
            final var replacedTime = Files.getLastModifiedTime(dest);
            assertEquals(initialTime, replacedTime);
        }

        @SneakyThrows
        @ParameterizedTest
        @EnumSource(MethodToTest.class)
        public void ensureWriteWithContentChange(final MethodToTest methodToTest) {
            final var dest = testDir.resolve("cracklib-small");
            try (final var inputStream = source.get()) {
                Files.copy(inputStream, dest);
            }
            final var newContent = (Sup<InputStream>) () -> new ByteArrayInputStream(
                "new File content".getBytes(StandardCharsets.UTF_8)
            );
            final var initialTime = Files.getLastModifiedTime(dest);
            methodToTest.replaceFileIfDifferent(newContent, dest);
            final var replacedTime = Files.getLastModifiedTime(dest);
            assertTrue(
                Comparator.<FileTime>naturalOrder()
                    .compare(initialTime, replacedTime) <= 0);
            Hash newFileHash, newContentHash;
            try (final var inputStream = newContent.get()) {
                newContentHash = Hash.md5(inputStream);
            }
            try (final var inputStream = Files.newInputStream(dest)) {
                newFileHash = Hash.md5(inputStream);
            }
            assertEquals(newContentHash, newFileHash);
        }
    }

    @Nested
    class CapitalizeTest {
        private static Stream<TestCase<String, String>> testCases() {
            return Stream.of(
                new TestCase<>("Home", "Home", null),
                new TestCase<>("HOME", "HOME", null),
                new TestCase<>("leilei", "Leilei", null),
                new TestCase<>("4365", "4365", null),
                new TestCase<>("芦 雷雷", "芦 雷雷", null),
                new TestCase<>("ǆ123", "ǅ123", null),
                new TestCase<>(null, null, NullPointerException.class)
            );
        }

        @ParameterizedTest
        @MethodSource("testCases")
        public void capitalizeTest(final TestCase<String, String> testCase) {
            if (testCase.error() == null) {
                assertEquals(testCase.expectedOutput(), JWO.capitalize(testCase.input()));
            } else {
                assertThrows(
                    testCase.error(),
                    JWO.curry(
                        (Consumer<String>) JWO::capitalize,
                        (Supplier<String>) testCase::input
                    )::run
                );
            }
        }
    }


    @Nested
    class DecapitalizeTest {
        private static Stream<TestCase<String, String>> testCases() {
            return Stream.of(
                new TestCase<>("Home", "home", null),
                new TestCase<>("HOME", "hOME", null),
                new TestCase<>("Leilei", "leilei", null),
                new TestCase<>("4365", "4365", null),
                new TestCase<>("芦 雷雷", "芦 雷雷", null),
                new TestCase<>("ǅ123", "ǆ123", null),
                new TestCase<>(null, null, NullPointerException.class)
            );
        }

        @ParameterizedTest
        @MethodSource("testCases")
        public void decapitalizeTest(final TestCase<String, String> testCase) {
            if (testCase.error() == null) {
                assertEquals(testCase.expectedOutput(), JWO.decapitalize(testCase.input()));
            } else {
                assertThrows(
                    testCase.error(),
                    JWO.curry(
                        (Consumer<String>) JWO::decapitalize,
                        (Supplier<String>) testCase::input
                    )::run
                );
            }
        }
    }


    @Nested
    class InstallResourceTest {
        @TempDir
        private Path testDir;

        @SneakyThrows
        @Test
        public void ensureFileCopy() {
            final var dest = testDir.resolve("some/nested/path/cracklib-small");
            JWO.installResource(CRACKLIB_RESOURCE, dest, getClass());
            Hash newFileHash, newContentHash;
            try (final var inputStream = getClass().getResourceAsStream(CRACKLIB_RESOURCE)) {
                newContentHash = Hash.md5(inputStream);
            }
            try (final var inputStream = Files.newInputStream(dest)) {
                newFileHash = Hash.md5(inputStream);
            }
            assertEquals(newContentHash, newFileHash);
        }
    }

    @Nested
    public class ArrayIteratorTest {
        public static Stream<TestCase<Integer[], Void>> test() {
            return Stream.of(
                new TestCase<>(new Integer[]{}, null, null),
                new TestCase<>(new Integer[]{1, 2, 3, 4, 5, null, 6, 7, 8, 9, 10}, null, null)
            );
        }

        @MethodSource
        @ParameterizedTest
        public void test(final TestCase<Integer[], Void> testCase) {
            final var it = JWO.iterator(testCase.input());

            for (final Integer n : testCase.input()) {
                final var m = it.next();
                assertEquals(n, m);
            }
            assertFalse(it.hasNext());
        }
    }

    @Test
    @SneakyThrows
    public void copyTest() {
        final MessageDigest md1 = Hash.Algorithm.MD5.newMessageDigest();
        final Supplier<Reader> source = JWO.compose(
            JWO.compose(
                () -> getClass().getResourceAsStream(CRACKLIB_RESOURCE),
                (InputStream is) -> new DigestInputStream(is, md1)
            ),
            (InputStream is) -> new InputStreamReader(is)
        );
        final MessageDigest md2 = Hash.Algorithm.MD5.newMessageDigest();
        final Supplier<Writer> destination = JWO.compose(
            JWO.compose(
                NullOutputStream::new,
                (OutputStream os) -> new DigestOutputStream(os, md2)
            ),
            (OutputStream is) -> new OutputStreamWriter(is)
        );

        try (final var reader = source.get()) {
            try (final Writer writer = destination.get()) {
                JWO.copy(reader, writer);
            }
        }
        assertArrayEquals(md1.digest(), md2.digest());
    }

    @Test
    @SneakyThrows
    public void extractZipTest(@TempDir Path testDir) {
        final var testBundle = Optional.ofNullable(System.getProperty("zip.test.bundle"))
            .map(Path::of)
            .orElseGet(Assertions::fail);
        final var destination1 = testDir.resolve("bundle");
        final var destination2 = testDir.resolve("bundle2");
        final var reassembledBundle = testDir.resolve("bundle3.zip");
        final var destination3 = testDir.resolve("bundle3");
        try (final var zos = new ZipOutputStream(Files.newOutputStream(reassembledBundle))) {
            JWO.expandZip(testBundle, (zis, zipEntry) -> {
                final var baos = new ByteArrayOutputStream();
                JWO.copy(zis, baos);
                if (zipEntry.isDirectory()) {
                    final var dir = destination1.resolve(zipEntry.getName());
                    Files.createDirectories(dir);
                    JWO.writeZipEntry(zos,
                        () -> new ByteArrayInputStream(baos.toByteArray()),
                        zipEntry.getName(),
                        ZipEntry.STORED
                    );
                } else {
                    final var file = destination1.resolve(zipEntry.getName());
                    Files.createDirectories(file.getParent());
                    try (final var os = Files.newOutputStream(file)) {
                        JWO.copy(new ByteArrayInputStream(baos.toByteArray()), os);
                    }
                    JWO.writeZipEntry(zos,
                        () -> new ByteArrayInputStream(baos.toByteArray()),
                        zipEntry.getName(),
                        ZipEntry.DEFLATED
                    );
                }
            });
        }
        JWO.extractZip(testBundle, destination2);
        JWO.extractZip(reassembledBundle, destination3);
        final var visitor = new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                final var relativePath = destination1.relativize(file);
                final var hashes = Stream.of(
                    destination1.resolve(relativePath),
                    destination2.resolve(relativePath),
                    destination3.resolve(relativePath)
                ).map((Fun<Path, Hash>) p -> {
                    Hash hash;
                    try (final var is = Files.newInputStream(p)) {
                        hash = Hash.md5(is);
                    }
                    return hash;
                }).collect(Collectors.toSet());
                assertTrue(hashes.size() == 1);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(destination1, visitor);
        Stream.of(destination1, destination2, destination3)
            .forEach(p -> {
                JWO.deletePath(p);
                assertFalse(Files.exists(p));
            });
    }

    @Test
    @SneakyThrows
    public void readResource2StringTest() {
        final var hash1 = Optional.of(JWO.readResource2String(CRACKLIB_RESOURCE))
            .map(it -> it.getBytes(StandardCharsets.UTF_8))
            .map(ByteArrayInputStream::new)
            .map(Hash::md5)
            .orElseGet(Assertions::fail);

        Hash hash2;
        try (final var is = getClass().getResourceAsStream(CRACKLIB_RESOURCE)) {
            hash2 = Hash.md5(is);
        }
        assertEquals(hash1, hash2);
    }

    @Nested
    class NewThrowableTest {
        private static class SomeWeirdException extends RuntimeException {
            public SomeWeirdException() {
                super();
            }

            public SomeWeirdException(final String msg) {
                super(msg);
            }

            public SomeWeirdException(final String msg, final Throwable cause) {
                super(msg, cause);
            }
        }
    }

    @Test
    public void newThrowableTest1() {
        final var ex = JWO.newThrowable(NewThrowableTest.SomeWeirdException.class);
        assertThrows(NewThrowableTest.SomeWeirdException.class, () -> {
            throw ex;
        });
        assertThrowsExactly(NewThrowableTest.SomeWeirdException.class, () -> {
            JWO.raise(NewThrowableTest.SomeWeirdException.class);
        });
    }

    @Test
    public void newThrowableTest2() {
        final var ex = JWO.newThrowable(NewThrowableTest.SomeWeirdException.class, "some message without placeholder");
        assertThrowsExactly(NewThrowableTest.SomeWeirdException.class, () -> {
            throw ex;
        });
        assertThrowsExactly(NewThrowableTest.SomeWeirdException.class, () -> {
            JWO.raise(NewThrowableTest.SomeWeirdException.class,
                "some message without placeholder"
            );
        });
    }

    @Test
    public void newThrowableTest3() {
        final var ex = JWO.newThrowable(NewThrowableTest.SomeWeirdException.class,
            "some message with placeholder %d",
            25
        );
        assertThrowsExactly(NewThrowableTest.SomeWeirdException.class, () -> {
            throw ex;
        });
        assertThrowsExactly(NewThrowableTest.SomeWeirdException.class, () -> {
            JWO.raise(NewThrowableTest.SomeWeirdException.class,
                "some message with placeholder %d",
                25
            );
        });
    }

    @Test
    public void newThrowableTest4() {
        final var cause = new RuntimeException();
        final var ex = JWO.newThrowable(NewThrowableTest.SomeWeirdException.class,
            cause,
            "some message with placeholder %d",
            25
        );
        assertThrowsExactly(NewThrowableTest.SomeWeirdException.class, () -> {
            try {
                throw ex;
            } catch (Throwable t) {
                assertTrue(t.getCause() == cause);
                throw t;
            }
        });
        assertThrowsExactly(NewThrowableTest.SomeWeirdException.class, () -> {
            try {
                JWO.raise(NewThrowableTest.SomeWeirdException.class,
                    cause,
                    "some message with placeholder %d",
                    25
                );
            } catch (final Throwable t) {
                assertTrue(t.getCause() == cause);
                throw t;
            }
        });
    }

    @Test
    public void enumerationTest() {
        final var list = immutableList(1, 2, 3, 4, 5, 6, 7);
        final var enumeration = Collections.enumeration(list);
        final var list2 = JWO.iterator2Stream(
            JWO.enumeration2Iterator(enumeration)
        ).collect(Collectors.toList());
        assertEquals(list, list2);
    }

    @Test
    @SneakyThrows
    public void readFile(final @TempDir Path testDir) {
        final var destination = testDir.resolve("cracklib-small");
        final var dis = Hash.Algorithm.MD5.newInputStream(getClass().getResourceAsStream(CRACKLIB_RESOURCE));
        final var md = dis.getMessageDigest();
        try {
            try(final var os = Files.newOutputStream(destination)) {
                JWO.copy(dis, os);
            }
        } finally {
            dis.close();
        }
        final var originalHash = md.digest();
        final var content = JWO.readFile2String(destination.toFile());
        final var readHash = Hash.md5(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        assertArrayEquals(originalHash, readHash.getBytes());
    }
}
