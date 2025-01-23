package net.woggioni.jwo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@RequiredArgsConstructor
public abstract class LockFile implements AutoCloseable {

    @Getter
    private static class LockFileMapValue {
        private final ReadWriteLock threadLock;
        private final AtomicInteger readerCount;

        @Setter
        private FileLock fileLock;

        public LockFileMapValue(Path path) {
            threadLock = new ReentrantReadWriteLock();
            readerCount = new AtomicInteger(0);
            fileLock = null;
        }
    }

    private static Map<Path, LockFileMapValue> map = Collections.synchronizedMap(new HashMap<>());

    private static FileChannel openFileChannel(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        return FileChannel.open(path, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE));
    }

    private static LockFile acquireInternal(Path path, boolean shared, Boolean blocking) throws IOException {
        LockFileMapValue lockFileMapValue = map.computeIfAbsent(path, LockFileMapValue::new);
        if(shared) {
            Lock lock = lockFileMapValue.getThreadLock().readLock();
            if(blocking) {
                lock.lock();
            } else {
                if(!lock.tryLock()) {
                    return null;
                }
            }

            int readers = lockFileMapValue.getReaderCount().incrementAndGet();
            if(readers == 1) {
                FileChannel channel = openFileChannel(path);
                FileLock fileLock;
                if(blocking) {
                    fileLock = channel.lock(0L, Long.MAX_VALUE, true);
                } else {
                    fileLock = channel.tryLock(0L, Long.MAX_VALUE, true);
                    if(fileLock == null) return null;
                }
                lockFileMapValue.setFileLock(fileLock);
            }
            return new LockFile() {
                @Override
                public void close() throws IOException {
                    int remainingReaders = lockFileMapValue.getReaderCount().decrementAndGet();
                    if(remainingReaders == 0) {
                        FileLock fileLock = lockFileMapValue.getFileLock();
                        fileLock.release();
                        fileLock.channel().close();
                        lockFileMapValue.setFileLock(null);
                    }
                    lock.unlock();
                }
            };
        } else {
            Lock lock = lockFileMapValue.getThreadLock().writeLock();
            if(blocking) {
                lock.lock();
            } else {
                if(!lock.tryLock()) {
                    return null;
                }
            }
            FileLock fileLock;
            FileChannel channel = openFileChannel(path);
            if(blocking) {
                fileLock = channel.lock(0L, Long.MAX_VALUE, false);
            } else {
                fileLock = channel.tryLock(0L, Long.MAX_VALUE, false);
                if(fileLock == null) {
                    lock.unlock();
                    return null;
                }
            }
            lockFileMapValue.setFileLock(fileLock);
            final FileLock fl = fileLock;
            return new LockFile() {
                @Override
                public void close() throws IOException {
                    fl.release();
                    fl.channel().close();
                    lockFileMapValue.setFileLock(null);
                    lock.unlock();
                }
            };
        }
    }

    public static LockFile tryAcquire(Path path, boolean shared) throws IOException {
        return acquireInternal(path, shared, false);
    }

    public static LockFile acquire(Path path, boolean shared) throws IOException {
        return acquireInternal(path, shared, true);
    }
}
