package net.woggioni.jwo.lockfile.test;

import lombok.SneakyThrows;
import net.woggioni.jwo.LockFile;

import java.nio.file.Path;
import java.nio.file.Paths;


public class LockFileTestMain {

    @SneakyThrows
    private static void run(
        Path lockfilePath,
        boolean shared,
        boolean keep
    ) {
        try (AutoCloseable lockfile = LockFile.acquire(lockfilePath, shared)) {
            while (keep) {
                Thread.sleep(1000);
            }
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        Path lockfilePath = Paths.get(args[0]);
        boolean shared = Boolean.parseBoolean(args[1]);
        boolean keep = Boolean.parseBoolean(args[2]);
        run(lockfilePath, shared, keep);
    }
}
