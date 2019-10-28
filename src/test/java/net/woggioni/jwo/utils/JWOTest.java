package net.woggioni.jwo.utils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.woggioni.jwo.Chronometer;
import net.woggioni.jwo.JWO;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JWOTest {

    @Test
    public void flatMapTest() {
        Stream<Integer> s = Stream.of(3, 4);
        List<Integer> l = JWO.flatMap(s, (n) -> {
            if (n > 3) return Optional.of(n);
            else return Optional.empty();
        }).collect(Collectors.toList());
        Assert.assertEquals(Collections.singletonList(4), l);
    }

    @Test
    public void optional2StreamTest() {
        Integer integer = 3;
        Optional<Integer> s = Optional.of(integer);
        JWO.optional2Stream(s).forEach(n -> Assert.assertEquals(integer, n));
        s = Optional.empty();
        JWO.optional2Stream(s).forEach(n -> Assert.fail());
    }

    @Test
    @SneakyThrows
    public void testRenderTemplate() {
        Map valuesMap = new HashMap<String, String>();
        valuesMap.put("author", "John Doe");
        valuesMap.put("date", "2020-03-25 16:22");
        valuesMap.put("adjective", "simple");
        String expected = "This is a simple test made by John Doe on 2020-03-25 16:22. It's really simple!\n";
        try (Reader reader = new InputStreamReader(
                JWOTest.class.getResourceAsStream("/render_template_test.txt"))) {
            String rendered = JWO.renderTemplate(reader, valuesMap);
            Assert.assertEquals(expected, rendered);
        }
        try (Reader reader = new InputStreamReader(
                JWOTest.class.getResourceAsStream("/render_template_test.txt"))) {
            String rendered = JWO.renderTemplate(JWO.readAll(reader), valuesMap);
            Assert.assertEquals(expected, rendered);
        }
    }


    public static String renderTemplateNaive(String template, Map<String, Object> valuesMap){
        StringBuffer formatter = new StringBuffer(template);
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
}
