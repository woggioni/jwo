package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LockFile implements AutoCloseable {

    private final Path path;
    private final FileLock lock;
    private final RandomAccessFile randomAccessFile;

    public LockFile(Path path) {
        this(path, false);
    }

    @SneakyThrows
    public LockFile(Path path, boolean shared) {
        this.path = path;
        try {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        } catch(FileAlreadyExistsException faee) {
        }
        randomAccessFile = new RandomAccessFile(path.toFile(), "rw");
        lock = randomAccessFile.getChannel().lock(0L, Long.MAX_VALUE, shared);
    }

    @Override
    @SneakyThrows
    public void close() {
        lock.release();
        randomAccessFile.close();
    }
}
