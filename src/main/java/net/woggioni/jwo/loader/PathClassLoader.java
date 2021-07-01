package net.woggioni.jwo.loader;

import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * A classloader that loads classes from a {@link Path} instance
 */
public final class PathClassLoader extends ClassLoader {

    private final Path path;

    static {
        registerAsParallelCapable();
    }

    public PathClassLoader(Path path) {
        this(path, null);
    }

    public PathClassLoader(Path path, ClassLoader parent) {
        super(parent);
        this.path = path;
    }

    @Override
    @SneakyThrows
    protected Class<?> findClass(String name) {
        Path classPath = path.resolve(name.replace('.', '/').concat(".class"));
        if (Files.exists(classPath)) {
            byte[] byteCode = Files.readAllBytes(classPath);
            return defineClass(name, byteCode, 0, byteCode.length);
        } else {
            throw new ClassNotFoundException(name);
        }
    }

    @Override
    @SneakyThrows
    protected URL findResource(String name) {
        Path resolved = path.resolve(name);
        if (Files.exists(resolved)) {
            return toURL(resolved);
        } else {
            return null;
        }
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
        final List<URL> resources = new ArrayList<>(1);

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!name.isEmpty()) {
                    this.addIfMatches(resources, file);
                }
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!name.isEmpty() || path.equals(dir)) {
                    this.addIfMatches(resources, dir);
                }
                return super.preVisitDirectory(dir, attrs);
            }

            void addIfMatches(List<URL> resources, Path file) throws IOException {
                if (path.relativize(file).toString().equals(name)) {
                    resources.add(toURL(file));
                }
            }
        });
        return Collections.enumeration(resources);
    }

    private static URL toURL(Path path) throws IOException {
        return new URL(null, path.toUri().toString(), PathURLStreamHandler.INSTANCE);
    }
}