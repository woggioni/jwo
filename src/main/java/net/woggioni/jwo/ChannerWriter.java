package net.woggioni.jwo;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

@RequiredArgsConstructor
public class ChannerWriter extends Writer {

    private final WritableByteChannel ch;

    private final Charset charset;

    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        write(new String(cbuf, off, len));
    }

    @Override
    public void write(final String str) throws IOException {
        ch.write(ByteBuffer.wrap(str.getBytes(charset)));
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws IOException {
        ch.close();
    }
}


