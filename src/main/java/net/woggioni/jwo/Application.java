package net.woggioni.jwo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import static net.woggioni.jwo.JWO.optional2Stream;
import static net.woggioni.jwo.JWO.streamCat;

@Slf4j
@Builder(builderMethodName = "", builderClassName = "Builder")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Application {
    private final String name;
    private final String configurationDirectoryPropertyKey;
    private final String cacheDirectoryPropertyKey;
    private final String dataDirectoryPropertyKey;
    private final String configurationDirectoryEnvVar;
    private final String cacheDirectoryEnvVar;
    private final String dataDirectoryEnvVar;

    private static final String MAC_FOLDER_LIBRARY = "Library";
    private static final String MAC_FOLDER_APPLICATION_SUPPORT = "Application Support";

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    private static boolean validateWritableDirectory(Path candidate) {
        try {
            if (!Files.exists(candidate)) {
                Files.createDirectories(candidate);
                log.trace("Created and selected directory '{}'", candidate);
                return true;
            } else if (!Files.isDirectory(candidate)) {
                log.trace("Directory '{}' discarded because it is not a directory", candidate);
                return false;
            } else if (!Files.isWritable(candidate)) {
                log.trace("Directory '{}' discarded because it is not writable", candidate);
                return false;
            } else {
                log.trace("Selected existing directory '{}'", candidate);
                return true;
            }
        } catch (Exception ioe) {
            log.trace(
                    String.format("Directory '%s' discarded: %s", candidate.toString(), ioe.getMessage()),
                    ioe
            );
            return false;
        }
    }

    @SneakyThrows
    private Path selectWritableDirectory(Stream<Path> candidates, String successMessage, String errorMessage) {
        return candidates
                .filter(Application::validateWritableDirectory)
                .peek(p -> log.debug(successMessage, p))
                .findFirst()
                .orElseThrow((Sup<Throwable>) () -> new FileNotFoundException(errorMessage));
    }

    @SneakyThrows
    public Path computeCacheDirectory() {
        Stream<Path> commonCandidates = optional2Stream(
                Optional.ofNullable(cacheDirectoryPropertyKey).map(System::getProperty).map(Paths::get),
                Optional.ofNullable(cacheDirectoryEnvVar).map(System::getProperty).map(Paths::get)
        );
        Stream<Path> osSpecificCandidates;
        if (OS.isMac) {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getProperty("user.home"))
                            .map(prefix -> Paths.get(prefix, MAC_FOLDER_LIBRARY, MAC_FOLDER_APPLICATION_SUPPORT, name, "cache")),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, "." + name, "cache"))
            );
        } else if (OS.isUnix) {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getenv("XDG_CACHE_HOME")).map(prefix -> Paths.get(prefix, name)),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, ".cache", name)),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, "." + name, "cache"))
            );
        } else if (OS.isWindows) {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getenv("LOCALAPPDATA"))
                            .map(prefix -> Paths.get(prefix, name, "cache")),
                    Optional.ofNullable(System.getProperty("user.home"))
                            .map(prefix -> Paths.get(prefix, "Application Data", "Local Settings", "Application Data", name, "cache")));
        } else {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getProperty("user.home"))
                            .map(prefix -> Paths.get(prefix, "." + name, "cache")));
        }
        return selectWritableDirectory(streamCat(commonCandidates, osSpecificCandidates),
                "Using cache directory '{}'",
                "Unable to find a usable cache directory");
    }

    @SneakyThrows
    public Path computeDataDirectory() {
        Stream<Path> commonCandidates = optional2Stream(
                Optional.ofNullable(dataDirectoryPropertyKey).map(System::getProperty).map(Paths::get),
                Optional.ofNullable(dataDirectoryEnvVar).map(System::getProperty).map(Paths::get)
        );
        Stream<Path> osSpecificCandidates;
        if (OS.isMac) {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getProperty("user.home"))
                            .map(prefix -> Paths.get(prefix, MAC_FOLDER_LIBRARY, MAC_FOLDER_APPLICATION_SUPPORT, name)),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, "." + name))
            );
        } else if (OS.isUnix) {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getenv("XDG_DATA_HOME")).map(prefix -> Paths.get(prefix, name)),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, ".local", "share", name)),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, "." + name))
            );
        } else if (OS.isWindows) {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getenv("LOCALAPPDATA"))
                            .map(prefix -> Paths.get(prefix, name)),
                    Optional.ofNullable(System.getProperty("user.home"))
                            .map(prefix -> Paths.get(prefix, "Application Data", "Local Settings", "Application Data", name)));
        } else {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getProperty("user.home"))
                            .map(prefix -> Paths.get(prefix, "." + name)));
        }
        return selectWritableDirectory(streamCat(commonCandidates, osSpecificCandidates),
                "Using data directory '{}'",
                "Unable to find a usable data directory");
    }

    @SneakyThrows
    public Path computeConfigurationDirectory() {
        Stream<Path> commonCandidates = optional2Stream(
                Optional.ofNullable(configurationDirectoryPropertyKey).map(System::getProperty).map(Paths::get),
                Optional.ofNullable(configurationDirectoryEnvVar).map(System::getProperty).map(Paths::get)
        );
        Stream<Path> osSpecificCandidates;
        if (OS.isMac) {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getProperty("user.home"))
                            .map(prefix -> Paths.get(prefix, MAC_FOLDER_LIBRARY, MAC_FOLDER_APPLICATION_SUPPORT, name, "config")),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, "." + name, "config"))
            );
        } else if (OS.isUnix) {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getenv("XDG_CONFIG_HOME")).map(prefix -> Paths.get(prefix, name)),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, ".config", name)),
                    Optional.ofNullable(System.getProperty("user.home")).map(prefix -> Paths.get(prefix, "." + name, "config"))
            );
        } else if (OS.isWindows) {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getenv("LOCALAPPDATA"))
                            .map(prefix -> Paths.get(prefix, name, "config")),
                    Optional.ofNullable(System.getProperty("user.home"))
                            .map(prefix -> Paths.get(prefix, "Application Data", "Local Settings", "Application Data", name, "config")));
        } else {
            osSpecificCandidates = optional2Stream(
                    Optional.ofNullable(System.getProperty("user.home")
                    ).map(prefix -> Paths.get(prefix, "." + name, "config")));
        }
        return selectWritableDirectory(streamCat(commonCandidates, osSpecificCandidates),
                "Using configuration directory '{}'",
                "Unable to find a usable configuration directory");
    }
}
