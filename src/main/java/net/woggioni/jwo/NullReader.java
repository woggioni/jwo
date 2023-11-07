package net.woggioni.jwo;

import java.io.IOException;
import java.io.Reader;

public class NullReader extends Reader {
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return -1;
    }

    @Override
    public void close() {}
}
