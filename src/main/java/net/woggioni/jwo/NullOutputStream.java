package net.woggioni.jwo;

import java.io.OutputStream;

public class NullOutputStream extends OutputStream {
    @Override
    public void write(int i) {}

    @Override
    public void write(byte[] bytes) {}
}
