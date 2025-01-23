package net.woggioni.jwo.test;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.woggioni.jwo.JavaProcessBuilder;
import net.woggioni.jwo.LockFile;
import net.woggioni.jwo.Run;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LockFileTest {
    private static Logger log = LoggerFactory.getLogger(LockFileTest.class);

    @TempDir
    public Path testDir;

    private Path executablePath = Paths.get(System.getProperty("lockFileTest.executable.jar"));

    @RequiredArgsConstructor
    private static class LockFileTestMainArgs {
        final Path lockFilePath;
        final boolean shared;
        final boolean keep;

        public List<String> getArgs() {
            return Arrays.asList(lockFilePath.toString(), Boolean.toString(shared), Boolean.toString(keep));
        }
    }

    @SneakyThrows
    private static void kill(Process p) {
        if (p != null && p.isAlive()) p.destroyForcibly().waitFor();
    }

    @Test
    @SneakyThrows
    public void testExclusiveLockHeldOnFile() {
        Path lockFilePath = Files.createFile(testDir.resolve("file.lock"));
        // try to acquire an exclusive lock and check that the process returns immediately
        JavaProcessBuilder javaProcessBuilder = new JavaProcessBuilder();
        javaProcessBuilder.setExecutableJar(executablePath);
        javaProcessBuilder.setCliArgs(new LockFileTestMainArgs(lockFilePath, false, false).getArgs());
        Process process = javaProcessBuilder.build()
                .inheritIO()
                .start();
        Assertions.assertTrue(process.waitFor(1, TimeUnit.SECONDS));
        Assertions.assertEquals(0, process.exitValue());

        Process sharedLockProcess = null;
        Process anotherSharedLockProcess = null;
        Process exclusiveLockProcess = null;
        try {
            // try to acquire and keep a shared lock on the file and check that the process does not exit
            javaProcessBuilder.setCliArgs(new LockFileTestMainArgs(lockFilePath, true, true).getArgs());
            sharedLockProcess = javaProcessBuilder.build()
                    .inheritIO()
                    .start();
            Assertions.assertFalse(sharedLockProcess.waitFor(1000, TimeUnit.MILLISECONDS));

            // try to acquire another shared lock on the file and check that the process is able to terminate
            javaProcessBuilder.setCliArgs(new LockFileTestMainArgs(lockFilePath, true, false).getArgs());
            anotherSharedLockProcess = javaProcessBuilder.build()
                    .inheritIO()
                    .start();
            Assertions.assertTrue(anotherSharedLockProcess.waitFor(1, TimeUnit.SECONDS));

            // try to acquire an exclusive lock on the file and check that process hangs
            javaProcessBuilder.setCliArgs(new LockFileTestMainArgs(lockFilePath, false, false).getArgs());
            exclusiveLockProcess = javaProcessBuilder.build()
                    .inheritIO()
                    .start();
            Assertions.assertFalse(exclusiveLockProcess.waitFor(1, TimeUnit.SECONDS));
            // kill the process holding the shared lock and check that the process holding the exclusive lock terminates
            sharedLockProcess.destroyForcibly().waitFor();
            Assertions.assertTrue(exclusiveLockProcess.waitFor(1, TimeUnit.SECONDS));
            Assertions.assertEquals(0, exclusiveLockProcess.exitValue());
        } finally {
            kill(sharedLockProcess);
            kill(anotherSharedLockProcess);
            kill(exclusiveLockProcess);
        }
    }

    @Test
    @SneakyThrows
    public void sameProcessTest(@TempDir Path testDir) {
        ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread::new);
        Path lockfile = testDir.resolve("file.lock");
        AtomicInteger readerRunning = new AtomicInteger(0);
        AtomicBoolean writerRunning = new AtomicBoolean(false);
        Run writerRunnable = () -> {
            try(LockFile lock = LockFile.acquire(lockfile, false)) {
                log.info("Writer start!!!!");
                writerRunning.set(true);
                FileChannel fileChannel = FileChannel.open(lockfile, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE));
                Writer writer = new OutputStreamWriter(Channels.newOutputStream(fileChannel));
                writer.write("asdffdgkhjdhigsdfhuifg");
                Thread.sleep(100);
                log.info("Writer end!!!!");
                writerRunning.set(false);
                Assertions.assertEquals(0, readerRunning.get());
            }
        };
        Run readerRunnable = () -> {
            try(AutoCloseable lock = LockFile.acquire(lockfile, true)) {
                readerRunning.incrementAndGet();
                log.info("reader start");
                Thread.sleep(100);
                log.info("reader end");
                readerRunning.decrementAndGet();
                Assertions.assertEquals(false, writerRunning.get());
            }
        };
        CompletableFuture<?> reader1 = CompletableFuture.runAsync(readerRunnable, executor);
        CompletableFuture<?> reader2 = CompletableFuture.runAsync(readerRunnable, executor);
        CompletableFuture<?> writer = CompletableFuture.runAsync(writerRunnable, executor);
        try {
            CompletableFuture.allOf(reader1, reader2, writer).get();
        } catch (ExecutionException ee) {
            throw ee.getCause();
        }
        log.info("FINISHED");
    }
}