package net.woggioni.jwo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Application {

    private static boolean validateConfigurationDirectory(Path candidate) {
        try {
            if (!Files.exists(candidate)) {
                Files.createDirectories(candidate);
                return true;
            } else if (!Files.isDirectory(candidate)) {
                log.debug("Configuration directory '{}' discarded because it is not a directory", candidate);
                return false;
            } else if (!Files.isWritable(candidate)) {
                log.debug("Configuration directory '{}' discarded because it is not writable", candidate);
                return false;
            } else {
                log.debug("Using configuration directory '{}'", candidate);
                return true;
            }
        } catch (Exception ioe) {
            log.debug(
                    String.format("configuration directory '%s' discarded: %s", candidate.toString(), ioe.getMessage()),
                    ioe
            );
            return false;
        }
    }

    @SneakyThrows
    private static Path selectCandidate(Stream<Path> candidates, String successMessage, String errorMessage) {
        return candidates
                .filter(Application::validateConfigurationDirectory)
                .peek(p -> log.debug(successMessage, p))
                .findFirst()
                .orElseThrow((Sup<Throwable>)() -> new FileNotFoundException(errorMessage));
    }

    @SneakyThrows
    private static Path computeCacheDirectory(String appName, String jvmPropertyKey) {
        Stream<Path> candidates;
        if(OS.isUnix) {
            candidates = JWO.optional2Stream(
                Optional.ofNullable(System.getProperty(jvmPropertyKey)).map(Paths::get),
                Optional.ofNullable(System.getenv("XDG_CACHE_HOME")).map(prefix -> Paths.get(prefix, appName)),
                Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, ".cache", appName)),
                Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, "." + appName, "cache"))
            );
        } else if(OS.isMac) {
            candidates = JWO.optional2Stream(
                Optional.ofNullable(System.getProperty("user.home"))
                    .map(prefix -> Paths.get(prefix, "Library", "Application support", appName, "cache")),
                Optional.ofNullable(System.getProperty("user.home"))
                    .map(prefix -> Paths.get(prefix, "." + appName, "cache"))
            );
        } else if(OS.isWindows) {
            candidates = JWO.optional2Stream(
                Optional.ofNullable(System.getenv("LOCALAPPDATA"))
                    .map(prefix -> Paths.get(prefix, appName, "cache")),
                Optional.ofNullable(System.getProperty("user.home"))
                    .map(prefix -> Paths.get(prefix, "Application Data", "Local Settings", "Application Data", appName, "cache")));
        } else {
            candidates = JWO.optional2Stream(
                Optional.ofNullable(System.getProperty("user.home"))
                    .map(prefix -> Paths.get(prefix, "." + appName, "cache")));
        }
        return selectCandidate(candidates,
                "Using cache directory '{}'",
                "Unable to find a usable cache directory");
    }


    @SneakyThrows
    private static Path computeConfigurationDirectory(String appName, String jvmPropertyKey) {
        Stream<Path> candidates;
        if(OS.isUnix) {
            candidates = JWO.optional2Stream(
                    Optional.ofNullable(System.getProperty(jvmPropertyKey)).map(Paths::get),
                    Optional.ofNullable(System.getenv("XDG_CONFIG_HOME")).map(prefix -> Paths.get(prefix, appName)),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, ".config", appName)),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, "." + appName, "config"))
            );
        } else if(OS.isMac) {
            candidates = JWO.optional2Stream(
                    Optional.ofNullable(System.getProperty("user.home"))
                        .map(prefix -> Paths.get(prefix, "Library", "Application support", appName, "config")),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, "." + appName, "config"))
            );
        } else if(OS.isWindows) {
            candidates = JWO.optional2Stream(
                    Optional.ofNullable(System.getenv("LOCALAPPDATA"))
                        .map(prefix -> Paths.get(prefix, appName, "config")),
                    Optional.ofNullable(System.getProperty("user.home"))
                        .map(prefix -> Paths.get(prefix, "Application Data", "Local Settings", "Application Data", appName, "config")));
        } else {
            candidates = JWO.optional2Stream(Optional.ofNullable(System.getProperty("user.home"))
                .map(prefix -> Paths.get(prefix, "." + appName, "config")));
        }
        return selectCandidate(candidates,
                "Using configuration directory '{}'",
                "Unable to find a usable configuration directory");
    }

}
