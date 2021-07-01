package net.woggioni.jwo.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Input stream that extract a zip archive in the provided {@param destination} while reading it
 */
class ZipExtractorInputStream extends ZipInputStream {

    public ZipExtractorInputStream(InputStream source, Path destination) {
        super(source);
        this.destination = destination;
    }

    private final Path destination;

    private OutputStream currentFile = null;

    @Override
    public ZipEntry getNextEntry() throws IOException {
        ZipEntry entry = super.getNextEntry();
        if(entry != null) {
            Path newFileSystemLocation = destination.resolve(entry.getName());
            if(entry.isDirectory()) {
                Files.createDirectories(newFileSystemLocation);
            } else {
                Files.createDirectories(newFileSystemLocation.getParent());
                currentFile = Files.newOutputStream(newFileSystemLocation);
            }
        }
        return entry;
    }

    @Override
    public int read() throws IOException {
        int result = super.read();
        if(result != -1 && currentFile != null) currentFile.write(result);
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if(read != -1 && currentFile != null) currentFile.write(b, off, read);
        return read;
    }

    @Override
    public void closeEntry() throws IOException{
        super.closeEntry();
        if(currentFile != null) currentFile.close();
    }
}
