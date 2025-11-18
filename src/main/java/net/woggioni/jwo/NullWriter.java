package net.woggioni.jwo;

import java.io.IOException;
import java.io.Writer;

public class NullWriter extends Writer {
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
