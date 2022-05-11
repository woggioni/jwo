package net.woggioni.jwo;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum OS {
    WINDOWS("windows"),
    MACOS("mac os x"),
    LINUX("linux"),
    SOLARIS("solaris"),
    BSD("bsd"),
    AIX("aix"),
    HP_UX("hp-ux"),
    UNIX("unix"),
    POSIX("posix"),
    VMS("vms");

    private final String value;

    OS(String value) {
        this.value = value;
    }

    public static final OS current;
    public static final boolean isUnix;
    public static final boolean isWindows;
    public static final boolean isMac;

    static {
        OS currentOs = null;
        String osName = System.getProperty("os.name").toLowerCase();
        for(OS os : values()) {
            if(osName.startsWith(os.value)) {
                currentOs = os;
            }
        }
        if(currentOs == null) throw new IllegalArgumentException(String.format("Unrecognized OS '%s'", osName));
        current = currentOs;
        isUnix = Stream.of(LINUX, SOLARIS, BSD, AIX, HP_UX).collect(Collectors.toSet()).contains(current);
        isWindows = current == WINDOWS;
        isMac = current == MACOS;
    }
}
