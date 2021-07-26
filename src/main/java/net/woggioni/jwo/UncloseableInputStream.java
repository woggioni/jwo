package net.woggioni.jwo;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * {@link InputStream} wrapper that prevents it from being closed, useful to pass an {@link InputStream} instance
 * to a method that closes the stream before it has been fully consumed
 * (and whose remaining content is still needed by the caller)
 */
public class UncloseableInputStream extends FilterInputStream {

    public UncloseableInputStream(InputStream source) {
        super(source);
    }

    @Override
    public void close() { }
}

