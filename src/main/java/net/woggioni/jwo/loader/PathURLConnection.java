package net.woggioni.jwo.loader;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

final class PathURLConnection extends URLConnection {

    private final Path path;

    PathURLConnection(URL url, Path path) {
        super(url);
        this.path = path;
    }

    @Override
    public void connect() {}

    @Override
    public long getContentLengthLong() {
        try {
            return Files.size(this.path);
        } catch (IOException e) {
            throw new RuntimeException("could not get size of: " + this.path, e);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(this.path);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(this.path);
    }

    @Override
    @SneakyThrows
    public String getContentType() {
        return Files.probeContentType(this.path);
    }

    @Override
    @SneakyThrows
    public long getLastModified() {
        BasicFileAttributes attributes = Files.readAttributes(this.path, BasicFileAttributes.class);
        return attributes.lastModifiedTime().toMillis();
    }
}