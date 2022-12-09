package net.woggioni.jwo.exception;

import net.woggioni.jwo.JWO;

import java.util.stream.Collectors;

public class ChildProcessException extends RuntimeException {

    private static final String cmdLine2str(Iterable<String> cmdLine) {
        return JWO.iterable2Stream(cmdLine)
            .map(it -> it.replace("\"", "\\\""))
            .map(it -> "\"" + it + "\"")
            .collect(Collectors.joining(", "));
    }

    public ChildProcessException(Iterable<String> cmdline, int exitCode) {
        super(String.format("Child process [%s] terminated with exit code %d", cmdLine2str(cmdline), exitCode));
    }
}
