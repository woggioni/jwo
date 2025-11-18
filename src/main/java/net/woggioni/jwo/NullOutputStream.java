package net.woggioni.jwo;

import java.io.OutputStream;

public class NullOutputStream extends OutputStream {
    @Override
    public void write(final int i) {}

    @Override
    public void write(final byte[] bytes) {}
}
