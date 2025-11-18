package net.woggioni.jwo;

import java.io.IOException;
import java.io.Reader;

public class NullReader extends Reader {
    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        return -1;
    }

    @Override
    public void close() {}
}
