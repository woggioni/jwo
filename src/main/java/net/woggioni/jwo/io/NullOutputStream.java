package net.woggioni.jwo.io;

import java.io.OutputStream;

public class NullOutputStream extends OutputStream {
    @Override
    public void write(int i) {}

    @Override
    public void write(byte[] bytes) {}
}
