package net.woggioni.jwo.benchmark;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaiveTemplateRenderer {
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
