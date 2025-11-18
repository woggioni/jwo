package net.woggioni.jwo;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class UncloseableWriter extends FilterWriter {
    public UncloseableWriter(final Writer destination) {
        super(destination);
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}