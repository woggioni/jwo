package net.woggioni.jwo.compression;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;

public class CompressionInputStream extends InputStream {

    private final InputStream inputStream;
    private final Process process;
    private final Thread writer;
    private final InputStream processOutput;

    public CompressionInputStream(InputStream is, CompressionFormat compressionFormat) {
        this(is, compressionFormat, StreamMode.COMPRESSION);
    }

    public CompressionInputStream(InputStream is, CompressionFormat compressionFormat, Integer level) {
        this(is, compressionFormat, StreamMode.COMPRESSION, level);
    }

    public CompressionInputStream(InputStream is, CompressionFormat compressionFormat, StreamMode mode) {
        this(is, compressionFormat, mode, null);
    }

    @SneakyThrows
    public CompressionInputStream(InputStream is, CompressionFormat compressionFormat, StreamMode mode, Integer level) {
        inputStream = is;
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
        processOutput = process.getInputStream();
        StreamWriter streamWriter = new StreamWriter(inputStream, process.getOutputStream());
        writer = new Thread(streamWriter);
        writer.start();
    }

    @Override
    public int read() throws IOException {
        return processOutput.read();
    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        return processOutput.read(bytes, i, i1);
    }

    @Override
    @SneakyThrows
    public void close() {
        inputStream.close();
        writer.join();
        process.waitFor();
        processOutput.close();
    }
}
