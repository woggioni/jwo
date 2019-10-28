package net.woggioni.jwo;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class JavaProcessBuilder {

    private final Class<?> mainClass;

    private String javaHome = System.getProperty("java.home");
    private String classPath = System.getProperty("java.class.path");
    private Properties properties = new Properties();
    private String[] cliArgs = null;

    public JavaProcessBuilder javaHome(String javaHome) {
        this.javaHome = javaHome;
        return this;
    }

    public JavaProcessBuilder classPath(String classPath) {
        this.classPath = classPath;
        return this;
    }

    public JavaProcessBuilder properties(Properties properties) {
        this.properties = properties;
        return this;
    }

    public JavaProcessBuilder cliArgs(String ...cliArgs) {
        this.cliArgs = cliArgs;
        return this;
    }

    @SneakyThrows
    public ProcessBuilder exec() {
        Path javaBin = Paths.get(javaHome, "bin", "java");
        Stream<String> propertyStream = Optional.ofNullable(properties)
            .map(p -> p.entrySet().stream())
            .orElse(Stream.empty())
            .map(entry -> String.format("-D%s=%s", entry.getKey(), entry.getValue()));
        List<String> cmd = JWO.streamCat(
            Stream.of(javaBin.toString(), "-cp", classPath),
            propertyStream,
            Stream.of(mainClass.getCanonicalName()),
            Optional.ofNullable(cliArgs).map(Arrays::stream).orElse(Stream.empty()))
            .collect(Collectors.toList());
        return new ProcessBuilder(cmd);
    }
}
