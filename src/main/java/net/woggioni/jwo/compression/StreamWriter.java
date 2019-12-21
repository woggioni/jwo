package net.woggioni.jwo.compression;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.OutputStream;

@RequiredArgsConstructor
class StreamWriter implements Runnable {
    private final InputStream inputStream;
    private final OutputStream outputStream;

    @SneakyThrows
    public void run() {
        try {
            int c;
            while((c = inputStream.read()) >= 0) {
                outputStream.write(c);
            }
        } finally {
            outputStream.close();
        }
    }
}
