package net.woggioni.jwo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * A classloader that loads classes from a {@link Path} instance
 */
public final class PathClassLoader extends ClassLoader {

    private final Iterable<Path> paths;

    static {
        registerAsParallelCapable();
    }

    public PathClassLoader(final Path ...path) {
        this(Arrays.asList(path), null);
    }

    public PathClassLoader(final Iterable<Path> paths) {
        this(paths, null);
    }

    public PathClassLoader(final Iterable<Path> paths, final ClassLoader parent) {
        super(parent);
        this.paths = paths;
    }

    @Override
    @SneakyThrows
    protected Class<?> findClass(final String name) {
        for(final Path path : paths) {
            final Path classPath = path.resolve(name.replace('.', '/').concat(".class"));
            if (Files.exists(classPath)) {
                final byte[] byteCode = Files.readAllBytes(classPath);
                return defineClass(name, byteCode, 0, byteCode.length);
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    @SneakyThrows
    protected URL findResource(final String name) {
        for(final Path path : paths) {
            final Path resolved = path.resolve(name);
            if (Files.exists(resolved)) {
                return toURL(resolved);
            }
        }
        return null;
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
        final List<URL> resources = new ArrayList<>(1);
        for(final Path path : paths) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    if (!name.isEmpty()) {
                        this.addIfMatches(resources, file);
                    }
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                    if (!name.isEmpty() || path.equals(dir)) {
                        this.addIfMatches(resources, dir);
                    }
                    return super.preVisitDirectory(dir, attrs);
                }

                void addIfMatches(final List<URL> resources, final Path file) throws IOException {
                    if (path.relativize(file).toString().equals(name)) {
                        resources.add(toURL(file));
                    }
                }
            });
        }
        return Collections.enumeration(resources);
    }

    private static URL toURL(final Path path) throws IOException {
        return new URL(null, path.toUri().toString(), PathURLStreamHandler.INSTANCE);
    }

    private static final class PathURLConnection extends URLConnection {

        private final Path path;

        PathURLConnection(final URL url, final Path path) {
            super(url);
            this.path = path;
        }

        @Override
        public void connect() {}

        @Override
        public long getContentLengthLong() {
            try {
                return Files.size(this.path);
            } catch (final IOException e) {
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
            final BasicFileAttributes attributes = Files.readAttributes(this.path, BasicFileAttributes.class);
            return attributes.lastModifiedTime().toMillis();
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PathURLStreamHandler extends URLStreamHandler {

        static final URLStreamHandler INSTANCE = new PathURLStreamHandler();

        @Override
        @SneakyThrows
        protected URLConnection openConnection(final URL url) {
            final URI uri = url.toURI();
            final Path path = Paths.get(uri);
            return new PathURLConnection(url, path);
        }
    }
}