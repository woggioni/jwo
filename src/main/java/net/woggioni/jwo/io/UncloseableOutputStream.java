package net.woggioni.jwo.io;

import java.io.FilterOutputStream;
import java.io.OutputStream;

/**
 * {@link OutputStream} wrapper that prevents it from being closed, useful to pass an {@link OutputStream} instance
 * to a method that closes the stream before it has been finalized by the caller
 */
public class UncloseableOutputStream extends FilterOutputStream {

    public UncloseableOutputStream(OutputStream source) {
        super(source);
    }

    @Override
    public void close() {
    }
}
