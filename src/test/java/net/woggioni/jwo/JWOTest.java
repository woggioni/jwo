package net.woggioni.jwo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.woggioni.jwo.CollectionUtils.immutableList;
import static net.woggioni.jwo.CollectionUtils.newArrayList;

public class JWOTest {

    @Test
    public void flatMapTest() {
        Stream<Integer> s = Stream.of(3, 4);
        List<Integer> l = JWO.flatMap(s, (n) -> {
            if (n > 3) return Optional.of(n);
            else return Optional.empty();
        }).collect(Collectors.toList());
        Assertions.assertEquals(Collections.singletonList(4), l);
    }

    @Test
    public void optional2StreamTest() {
        Integer integer = 3;
        Optional<Integer> s = Optional.of(integer);
        JWO.optional2Stream(s).forEach(n -> Assertions.assertEquals(integer, n));
        s = Optional.empty();
        JWO.optional2Stream(s).forEach(n -> Assertions.fail(
            "Stream should have been empty and this piece of code never executed")
        );
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
                immutableList(6, 22))

        ;
        final String haystack;
        final Character needle;

        final Character escape;

        final List<Integer> solution;
    }

    @ParameterizedTest
    @EnumSource(IndexOfWithEscapeTestCase.class)
    public void testIndexOfWithEscape(IndexOfWithEscapeTestCase testCase) {
        String haystack = testCase.haystack;
        List<Integer> solution = newArrayList();
        int i = 0;
        while(true) {
            i = JWO.indexOfWithEscape(haystack, testCase.needle, testCase.escape, i, haystack.length());
            if(i < 0) break;
            solution.add(i);
            ++i;
        }
        Assertions.assertEquals(testCase.solution, solution);
    }

    @Test
    @SneakyThrows
    public void testRenderTemplate() {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("author", "John Doe");
        valuesMap.put("date", "2020-03-25 16:22");
        valuesMap.put("adjective", "simple");
        String expected = """
                This is a simple test made by John Doe on 2020-03-25 16:22. It's really simple!
                /home/user
                /home/user
                defaultValue
                """;
        Map<String, Map<String, Object>> contextMap = new MapBuilder<String, Map<String, Object>>()
                .entry("env",
                    new MapBuilder<String, String>()
                        .entry("HOME", "/home/user")
                        .build(TreeMap::new, Collections::unmodifiableMap)
                )
                .entry("sys",
                    new MapBuilder<String, String>()
                        .entry("user.home", "/home/user")
                        .build(TreeMap::new, Collections::unmodifiableMap)                ).build(TreeMap::new, Collections::unmodifiableMap);
        try (Reader reader = new InputStreamReader(
                JWOTest.class.getResourceAsStream("/render_template_test.txt"))) {
            String rendered = JWO.renderTemplate(reader, valuesMap, contextMap);
            Assertions.assertEquals(expected, rendered);
        }
        try (Reader reader = new InputStreamReader(
                JWOTest.class.getResourceAsStream("/render_template_test.txt"))) {
            String rendered = JWO.renderTemplate(JWO.readAll(reader), valuesMap, contextMap);
            Assertions.assertEquals(expected, rendered);
        }
    }


    public static String renderTemplateNaive(String template, Map<String, Object> valuesMap){
        StringBuilder formatter = new StringBuilder(template);
        Object absent = new Object();

        Matcher matcher = Pattern.compile("\\$\\{(\\w+)}").matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);

            String formatKey = String.format("${%s}", key);
            int index = formatter.indexOf(formatKey);

            // If the key is present:
            //  - If the value is not null, then replace the variable for the value
            //  - If the value is null, replace the variable for empty string
            // If the key is not present, leave the variable untouched.
            if (index != -1) {
                Object value = valuesMap.getOrDefault(key, absent);
                if(value != absent) {
                    String valueStr = value != null ? value.toString() : "";
                    formatter.replace(index, index + formatKey.length(), valueStr);
                }
            }
        }
        return formatter.toString();
    }

    @Test
    @SneakyThrows
    @EnabledOnOs(OS.LINUX)
    public void uidTest(@TempDir Path testDir) {
        PosixFileAttributes pfa = Files.readAttributes(testDir, PosixFileAttributes.class);
        UserPrincipal expectedUser = pfa.owner();
        Class<? extends UserPrincipal> userClass = expectedUser.getClass();
        Method m = userClass.getDeclaredMethod("uid");
        m.setAccessible(true);
        int expectedUserId = (Integer) m.invoke(expectedUser);
        int uid = (int) JWO.uid();
        Assertions.assertEquals(expectedUserId, uid);
    }
}
