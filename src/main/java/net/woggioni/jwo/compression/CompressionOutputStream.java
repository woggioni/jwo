package net.woggioni.jwo.compression;

import lombok.SneakyThrows;

import java.io.OutputStream;

public class CompressionOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final Process process;
    private final Thread writer;
    private final OutputStream processInput;

    public CompressionOutputStream(OutputStream os, CompressionFormat compressionFormat) {
        this(os, compressionFormat, StreamMode.COMPRESSION);
    }

    public CompressionOutputStream(OutputStream os, CompressionFormat compressionFormat, Integer level) {
        this(os, compressionFormat, StreamMode.COMPRESSION, level);
    }

    public CompressionOutputStream(OutputStream os, CompressionFormat compressionFormat, StreamMode mode) {
        this(os, compressionFormat, mode, null);
    }

    @SneakyThrows
    public CompressionOutputStream(OutputStream os, CompressionFormat compressionFormat, StreamMode mode, Integer level) {
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
        ProcessBuilder pb = new ProcessBuilder(cliArgs);
        process = pb.start();
        processInput = process.getOutputStream();
        StreamWriter streamWriter = new StreamWriter(process.getInputStream(), outputStream);
        writer = new Thread(streamWriter);
        writer.start();
    }

    @Override
    @SneakyThrows
    public void write(byte[] buffer, int offset, int size) {
        processInput.write(buffer, offset, size);
    }

    @Override
    @SneakyThrows
    public void write(int i) {
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
