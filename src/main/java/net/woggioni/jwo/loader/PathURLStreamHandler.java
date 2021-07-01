package net.woggioni.jwo.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Path;
import java.nio.file.Paths;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class PathURLStreamHandler extends URLStreamHandler {

    static final URLStreamHandler INSTANCE = new PathURLStreamHandler();

    @Override
    @SneakyThrows
    protected URLConnection openConnection(URL url) {
        URI uri = url.toURI();
        Path path = Paths.get(uri);
        return new PathURLConnection(url, path);
    }
}
