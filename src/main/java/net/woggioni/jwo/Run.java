package net.woggioni.jwo;

import lombok.SneakyThrows;

@FunctionalInterface
public interface Run extends Runnable {
    @Override
    @SneakyThrows
    default void run() {
        exec();
    }

    void exec() throws Throwable;
}
