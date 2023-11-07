package net.woggioni.jwo;

import org.junit.jupiter.api.Test;

public class ApplicationTest {

    @Test
    void builderTest() {
        final var app = Application.builder("app")
            .configurationDirectoryPropertyKey("app.conf.dir")
            .dataDirectoryPropertyKey("app.data.dir")
            .cacheDirectoryPropertyKey("app.cache.dir")
            .cacheDirectoryEnvVar("APP_CACHE_DIR")
            .dataDirectoryEnvVar("APP_DATA_DIR")
            .configurationDirectoryEnvVar("APP_CONF_DIR")
            .build();
        final var confDir = app.computeConfigurationDirectory();
        final var cacheDir = app.computeCacheDirectory();
        final var dataDir = app.computeDataDirectory();
    }
}
