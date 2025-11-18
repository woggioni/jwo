package net.woggioni.jwo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Input stream that extract a jar archive in the provided destination while reading it
 */
public class JarExtractorInputStream extends JarInputStream {

    private final Path destination;
    private OutputStream currentFile = null;

    public JarExtractorInputStream(final InputStream source,
                                   final Path destination,
                                   final boolean verify,
                                   final String sourceLocation) throws IOException {
        super(source, verify);
        this.destination = destination;
        final Path newFileSystemLocation = destination.resolve(JarFile.MANIFEST_NAME);
        Files.createDirectories(newFileSystemLocation.getParent());
        try(final OutputStream outputStream = Files.newOutputStream(newFileSystemLocation)) {
            final Manifest manifest = getManifest();
            if(manifest == null) {
                String location;
                if(sourceLocation == null) {
                    location = "";
                } else {
                    location = String.format("from '%s'", sourceLocation);
                }
                throw JWO.newThrowable(IOException.class,
                        "The source stream %s doesn't represent a valid jar file", location);
            }
            manifest.write(outputStream);
        }
    }

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
