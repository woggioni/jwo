package net.woggioni.jwo.io;

import lombok.RequiredArgsConstructor;

import java.io.InputStream;

@RequiredArgsConstructor
public class CircularInputStream extends InputStream {
    final byte[] monomer;
    final int maxLoops;
    int loops = 0;
    int cursor = 0;

    public CircularInputStream(byte[] monomer) {
        this(monomer, -1);
    }

    @Override
    public int read() {
        if (cursor < 0) {
            return cursor;
        } else {
            int result = monomer[cursor];
            incrementCursor();
            return result;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) {
        int read = 0;
        while (read < len) {
            if(cursor < 0) break;
            int toBeRead = Math.min(monomer.length - cursor, len - read);
            System.arraycopy(monomer, cursor, b, off + read, toBeRead);
            incrementCursor(toBeRead);
            read += toBeRead;
        }
        return read > 0 ? read : -1;
    }

    int incrementCursor() {
        return incrementCursor(1);
    }

    int incrementCursor(int increment) {
        loops = (loops * monomer.length + increment) / monomer.length;
        if (maxLoops < 0 || loops < maxLoops) {
            cursor = (cursor + increment) % monomer.length;
        } else {
            cursor = -1;
        }
        return cursor;
    }
}