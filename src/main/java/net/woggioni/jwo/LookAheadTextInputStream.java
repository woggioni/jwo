package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.Reader;

public class LookAheadTextInputStream extends InputStream {

    private final char[] buffer = new char[1024];
    private final Reader reader;
    private int bufferFill = -1;
    private int cursor = -1;
    private int currentChar;


    public LookAheadTextInputStream(Reader reader) {
        this.reader = reader;
    }

    @Override
    @SneakyThrows
    public int read() {
        if (cursor > bufferFill) {
            return -1;
        } else if (cursor == bufferFill) {
            do {
                bufferFill = reader.read(buffer, 0, buffer.length) - 1;
                cursor = 0;
            } while(bufferFill == -1);
            currentChar = bufferFill == -2 ? -1 : buffer[0];
        } else {
            currentChar = buffer[++cursor];
        }
        return currentChar;
    }

    public int getCurrentByte() {
        return currentChar;
    }
}
