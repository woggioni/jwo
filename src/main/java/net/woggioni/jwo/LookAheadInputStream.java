package net.woggioni.jwo;

import lombok.SneakyThrows;

import java.io.InputStream;

public class LookAheadInputStream extends InputStream {

    private final byte[] buffer = new byte[1024];
    private final InputStream stream;
    private int bufferFill = -1;
    private int cursor = -1;
    private int currentByte;

    public LookAheadInputStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    @SneakyThrows
    public int read() {
        if (cursor > bufferFill) {
            return -1;
        } else if (cursor == bufferFill) {
            do {
                bufferFill = stream.read(buffer, 0, buffer.length) - 1;
                cursor = 0;
            } while(bufferFill == -1);
            currentByte = bufferFill == -2 ? -1 : Math.floorMod(buffer[0], 256);
        } else {
            currentByte = Math.floorMod(buffer[++cursor], 256);
        }
        return currentByte;
    }

    public int getCurrentByte() {
        return currentByte;
    }
}
