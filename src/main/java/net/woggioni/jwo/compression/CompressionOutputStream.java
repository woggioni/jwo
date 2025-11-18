package net.woggioni.jwo.compression;

import lombok.SneakyThrows;

import java.io.OutputStream;

public class CompressionOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final Process process;
    private final Thread writer;
    private final OutputStream processInput;

    public CompressionOutputStream(final OutputStream os, final CompressionFormat compressionFormat) {
        this(os, compressionFormat, StreamMode.COMPRESSION);
    }

    public CompressionOutputStream(final OutputStream os, final CompressionFormat compressionFormat, final Integer level) {
        this(os, compressionFormat, StreamMode.COMPRESSION, level);
    }

    public CompressionOutputStream(final OutputStream os, final CompressionFormat compressionFormat, final StreamMode mode) {
        this(os, compressionFormat, mode, null);
    }

    @SneakyThrows
    public CompressionOutputStream(final OutputStream os, final CompressionFormat compressionFormat, final StreamMode mode, final Integer level) {
        outputStream = os;
        String[] cliArgs;
        switch(mode) {
            case COMPRESSION:
                if(level == null) {
                    cliArgs = new String[] {compressionFormat.executable};
                } else {
                    cliArgs = new String[] {compressionFormat.executable, "-" + level};
                }
                break;
            case DECOMPRESSION:
                cliArgs = new String[] {compressionFormat.executable, "-d"};
                break;
            default:
                throw new NullPointerException();
        }
        final ProcessBuilder pb = new ProcessBuilder(cliArgs);
        process = pb.start();
        processInput = process.getOutputStream();
        final StreamWriter streamWriter = new StreamWriter(process.getInputStream(), outputStream);
        writer = new Thread(streamWriter);
        writer.start();
    }

    @Override
    @SneakyThrows
    public void write(final byte[] buffer, final int offset, final int size) {
        processInput.write(buffer, offset, size);
    }

    @Override
    @SneakyThrows
    public void write(final int i) {
        processInput.write(i);
    }

    @Override
    @SneakyThrows
    public void close() {
        processInput.close();
        process.waitFor();
        writer.join();
    }
}
