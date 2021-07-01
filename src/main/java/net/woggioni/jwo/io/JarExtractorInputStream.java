package net.woggioni.jwo.io;

import net.woggioni.jwo.JWO;

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
 * Input stream that extract a jar archive in the provided {@param destination} while reading it
 */
class JarExtractorInputStream extends JarInputStream {

    private final String sourceLocation;
    private final Path destination;
    private OutputStream currentFile = null;

    public JarExtractorInputStream(InputStream source,
                                   Path destination,
                                   boolean verify,
                                   String sourceLocation) throws IOException {
        super(source, verify);
        this.sourceLocation = sourceLocation;
        this.destination = destination;
        Path newFileSystemLocation = destination.resolve(JarFile.MANIFEST_NAME);
        Files.createDirectories(newFileSystemLocation.getParent());
        try(OutputStream outputStream = Files.newOutputStream(newFileSystemLocation)) {
            Manifest manifest = getManifest();
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
