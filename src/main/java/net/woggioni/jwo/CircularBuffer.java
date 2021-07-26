package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.io.Reader;

public class CircularBuffer {

    private int[] buffer;
    private Reader reader;
    private int delta = 0, cursor = 0;

    public CircularBuffer(Reader reader, int size) {
        this.reader = reader;
        buffer = new int[size];
    }

    @SneakyThrows
    public int next() {
        if (delta < 0)
            return buffer[Math.floorMod(cursor + delta++, buffer.length)];
        else {
            int result = reader.read();
            if (result < 0) return result;
            buffer[cursor] = result;
            cursor = (cursor + 1) % buffer.length;
            return result;
        }
    }

    public int prev() {
        return buffer[cursor + --delta >= 0 ? cursor + delta : cursor + delta + buffer.length];
    }

    public int size() {
        return buffer.length;
    }
}
