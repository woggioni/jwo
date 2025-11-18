package net.woggioni.jwo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Input stream that extract a zip archive in the provided destination while reading it
 */
public class ZipExtractorInputStream extends ZipInputStream {

    public ZipExtractorInputStream(final InputStream source, final Path destination) {
        super(source);
        this.destination = destination;
    }

    private final Path destination;

    private OutputStream currentFile = null;

    @Override
    public ZipEntry getNextEntry() throws IOException {
        final ZipEntry entry = super.getNextEntry();
        if(entry != null) {
            final Path newFileSystemLocation = destination.resolve(entry.getName());
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
        final int result = super.read();
        if(result != -1 && currentFile != null) currentFile.write(result);
        return result;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        final int read = super.read(b, off, len);
        if(read != -1 && currentFile != null) currentFile.write(b, off, read);
        return read;
    }

    @Override
    public void closeEntry() throws IOException{
        super.closeEntry();
        if(currentFile != null) currentFile.close();
    }
}
