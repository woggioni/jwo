package net.woggioni.jwo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.woggioni.jwo.exception.ChildProcessException;
import net.woggioni.jwo.internal.CharFilterReader;
import org.slf4j.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JWO {
    private static final Logger log = LoggerController.lazyLogger(JWO.class);
    private static final String PROTOCOL_HANDLER = "java.protocol.handler.pkgs";
    private static final String HANDLERS_PACKAGE = "net.woggioni.jwo.url";

    public static <T> Stream<T> iterable2Stream(final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> Stream<T> iterable2ParallelStream(final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), true);
    }

    public static <T> Stream<T> iterator2Stream(final Iterator<T> it) {
        return iterable2Stream(() -> it);
    }

    public static <T> Stream<T> iterator2ParallelStream(final Iterator<T> it) {
        return iterable2ParallelStream(() -> it);
    }

    @SneakyThrows
    public static void writeObject2File(final Path file, final Object o) {
        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file.toString()))) {
            writer.write(o.toString());
        }
    }

    public static void writeObject2File(final String fileName, final Object o) {
        writeObject2File(new File(fileName), o);
    }

    @SneakyThrows
    public static void writeObject2File(final File file, final Object o) {
        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file.getPath()))) {
            writer.write(o.toString());
        }
    }

    @SneakyThrows
    public static void writeBytes2File(final Path file, final byte[] bytes) {
        try (final OutputStream os = Files.newOutputStream(file)) {
            os.write(bytes);
        }
    }

    @SneakyThrows
    public static String readFile2String(final File file) {
        final StringWriter writer = new StringWriter();
        try (final Reader reader = Files.newBufferedReader(file.toPath())) {
            final char[] buffer = new char[1024];
            JWO.copy(reader, writer, buffer);
        }
        return writer.toString();
    }

    @SneakyThrows
    public static String readResource2String(final String classpath) {
        final StringBuilder builder = new StringBuilder();
        try (final Reader reader = new InputStreamReader(JWO.class.getResourceAsStream(classpath))) {
            final char[] buffer = new char[1024];
            while (true) {
                final int read = reader.read(buffer);
                builder.append(buffer, 0, read);
                if (read < buffer.length) break;
            }
        }
        return builder.toString();
    }

    @SneakyThrows
    public static <T extends Throwable> T newThrowable(final Class<T> cls) {
        final Constructor<T> constructor = cls.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    @SneakyThrows
    public static <T extends Throwable> T newThrowable(final Class<T> cls, final String format, final Object... args) {
        final Constructor<T> constructor = cls.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(String.format(format, args));
    }

    @SneakyThrows
    public static <T extends Throwable> T newThrowable(final Class<T> cls, final Throwable throwable, final String format, final Object... args) {
        final Constructor<T> constructor = cls.getConstructor(String.class, Throwable.class);
        return constructor.newInstance(String.format(format, args), throwable);
    }

    @SneakyThrows
    public static <T extends Throwable> T raise(final Class<T> cls) {
        throw newThrowable(cls);
    }

    @SneakyThrows
    public static <T extends Throwable> void raise(final Class<T> cls, final Throwable throwable, final String format, final Object... args) {
        throw newThrowable(cls, throwable, format, args);
    }

    @SneakyThrows
    public static <T extends Throwable> void raise(final Class<T> cls, final String format, final Object... args) {
        throw newThrowable(cls, format, args);
    }

    private static SSLSocketFactory defaultSSLSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    private static HostnameVerifier defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

    @SneakyThrows
    public static void setSSLVerifyPeerHostName(final boolean verify) {
        if (verify) {
            HttpsURLConnection.setDefaultHostnameVerifier(defaultHostnameVerifier);
        } else {
            final HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(final String hostname, final SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        }
    }

    @SneakyThrows
    public static void setSSLVerifyPeerCertificate(final boolean verify) {
        if (verify) {
            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
        } else {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                        }

                        public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                        }
                    }
            };
            // Install the all-trusting trust manager
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
    }

    public static <T> Predicate<T> not(final Pre<T> p) {
        return p.negate();
    }

    public static <V, T> Stream<V> flatMap(final Stream<T> stream,
                                           final Fun<? super T, Optional<? extends V>> mappingFunction) {
        return stream.map(mappingFunction).filter(Optional::isPresent).map(Optional::get);
    }

    public static void setSystemPropertyIfNotDefined(final String key, final String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    public static String bytesToHex(final byte[] bytes) {
        return Hash.bytesToHex(bytes);
    }

    public static byte[] hexToBytes(final String hexStr) {
        return Hash.hexToBytes(hexStr);
    }

    @SneakyThrows
    public static void deletePath(final Path path) {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * This method returns an element of a list at the given position counting from the end of the list,
     * inspiration comes from the Python programming language where the element
     * at position -1 is the last element of the list, the one at position -2 is the element before the last and so on
     *
     * @param list           the list from which the element will be retrieved
     * @param negativeOffset a negative integer representing the offset from the end of the list
     * @param <T>            the type parameter of the list
     * @return the designated list element
     * @throws IndexOutOfBoundsException if the negativeOffset is out of range
     *                                   (negativeOffset &ge; 0 || negativeOffset &lt; -size())
     */
    public static <T> T tail(final List<T> list, final int negativeOffset) {
        return list.get(list.size() + negativeOffset);
    }

    /**
     * @param list the input list
     * @param <T>  the type parameter of the list
     * @return the last element of the input list
     * @throws IndexOutOfBoundsException if the list is empty
     */
    public static <T> T tail(final List<T> list) {
        return tail(list, -1);
    }


    /**
     * This methods simply removes the last element of the list and returns it
     *
     * @param list the input list
     * @param <T>  the type parameter of the {@link List}
     * @return the input list
     * @throws IndexOutOfBoundsException     if the list is empty
     * @throws UnsupportedOperationException if the remove operation is not supported by this list
     */
    public static <T> T pop(final List<T> list) {
        return list.remove(list.size() - 1);
    }

    public static <T> Stream<T> streamCat(final Stream<T>... streams) {
        return Stream.of(streams).flatMap(Function.identity());
    }

    /**
     * @param template  Template text containing the variables to be replaced by this method. <br>
     *                  Variables follow the format ${variable_name}. <br>
     *                  Example: <br>
     *                  "This template was created by ${author}."
     * @param valuesMap A hashmap with the values of the variables to be replaced. <br>
     *                  The key is the variable name and the value is the value to be replaced in the template. <br>
     *                  Example: <br>
     *                  {"author" =&gt; "John Doe"}
     * @return The template text (String) with the variable names replaced by the values passed in the map. <br>
     * If any of the variable names is not contained in the map it will be replaced by an empty string. <br>
     * Example: <br>
     * "This template was created by John Doe."
     */
    public static String renderTemplate(final String template, final Map<String, Object> valuesMap) {
        return renderTemplate(template, valuesMap, null);
    }

    public static int indexOfWithEscape(final String haystack, final char needle, final char escape, final int begin, int end) {
        int result = -1;
        int cursor = begin;
        if(end == 0) {
            end = haystack.length();
        }
        int escapeCount = 0;
        while(cursor < end) {
            final char c = haystack.charAt(cursor);
            if(escapeCount > 0) {
                --escapeCount;
                if(c == escape) {
                    result = -1;
                }
            } else if(escapeCount == 0) {
                if (c == escape) {
                    ++escapeCount;
                }
                if (c == needle) {
                    result = cursor;
                }
            }
            if(result >= 0 && escapeCount == 0) {
                break;
            }
            ++cursor;
        }
        return result;
    }

    public static String renderTemplate(
            final String template,
            final Map<String, Object> valuesMap,
            final Map<String, Map<String, Object>> dictMap) {
        final StringBuilder sb = new StringBuilder();
        final Object absent = new Object();

        int cursor = 0;
        while(cursor < template.length()) {
            int nextPlaceHolder = template.indexOf('$', cursor);
            if (nextPlaceHolder < 0) {
                nextPlaceHolder = template.length();
            }
            int nextPlaceHolderWithEscape = indexOfWithEscape(template, '$', '$', cursor, template.length());
            if (nextPlaceHolderWithEscape < 0) {
                nextPlaceHolderWithEscape = template.length();
            }
            while (cursor < nextPlaceHolderWithEscape) {
                final char ch = template.charAt(cursor);
                if (nextPlaceHolder == nextPlaceHolderWithEscape || cursor != nextPlaceHolder) {
                    sb.append(ch);
                }
                ++cursor;
            }
            if (cursor + 1 < template.length() && template.charAt(cursor + 1) == '{') {
                String key;
                String context = null;
                String defaultValue = null;
                Object value;
                final int end = template.indexOf('}', cursor + 1);
                int colon;
                if (dictMap == null)
                    colon = -1;
                else {
                    colon = indexOfWithEscape(template, ':', '\\', cursor + 1, template.length());
                    if (colon >= end) colon = -1;
                }
                if (colon < 0) {
                    key = template.substring(cursor + 2, end);
                    value = valuesMap.getOrDefault(key, absent);
                } else {
                    context = template.substring(cursor + 2, colon);
                    final int secondColon = indexOfWithEscape(template, ':', '\\', colon + 1, end);
                    if(secondColon < 0) {
                        key = template.substring(colon + 1, end);
                    } else {
                        key = template.substring(colon + 1, secondColon);
                        defaultValue = template.substring(secondColon + 1, end);
                    }
                    value = Optional.ofNullable(dictMap.get(context))
                            .map(m -> m.get(key))
                            .orElse(absent);
                }
                if (value != absent) {
                    sb.append(value.toString());
                } else {
                    if (defaultValue != null) {
                        sb.append(defaultValue);
                    } else {
                        raise(MissingFormatArgumentException.class, "Missing value for placeholder '%s'",
                                context == null ? key : context + ':' + key);
                    }
                }
                cursor = end + 1;
            }
        }
        return sb.toString();
    }

    /**
     * @param reader    the reader reading the template text
     * @param valuesMap a map containing the values to replace in the template
     *                  {@link #renderTemplate(String, Map)}
     * @return The template text (String) with the variable names replaced by the values passed in the map. <br>
     * If any of the variable names is not contained in the map it will be replaced by an empty string. <br>
     * Example: <br>
     * "This template was created by John Doe."
     */
    @SneakyThrows
    public static String renderTemplate(final Reader reader,
                                        final Map<String, Object> valuesMap,
                                        final Map<String, Map<String, Object>> dictMap) {
        final StringBuilder sb = new StringBuilder();
        final char[] buf = new char[1024];
        int read;
        while (!((read = reader.read(buf)) < 0)) {
            sb.append(buf, 0, read);
        }
        return renderTemplate(sb.toString(), valuesMap, dictMap);
    }
    public static String renderTemplate(final Reader reader,
                                        final Map<String, Object> valuesMap) {
        return renderTemplate(reader, valuesMap, null);
    }

    @SneakyThrows
    public static String readAll(final Reader reader) {
        final char[] buffer = new char[1024];
        final StringBuilder sb = new StringBuilder();
        int read;
        while (!((read = reader.read(buffer, 0, buffer.length)) < 0)) {
            sb.append(buffer, 0, read);
        }
        return sb.toString();
    }

    public static <T, U extends T> Optional<U> cast(final T value, final Class<U> cls) {
        if (cls.isInstance(value)) {
            return Optional.of((U) value);
        } else {
            return Optional.empty();
        }
    }

    public static <T extends Enum> Map<String, T> enumReverseMap(final Class<T> cls) {
        return Arrays.stream(cls.getEnumConstants())
                .collect(CollectionUtils.toUnmodifiableTreeMap(Object::toString, Function.identity()));
    }

    public static <T extends Enum> T enumFromString(final Class<T> cls, final Map<String, T> reverseMap, final String value) {
        final T result = reverseMap.get(value);
        if (result == null)
            throw newThrowable(IllegalArgumentException.class,
                    "Unknown value '%s' for enum %s", value, cls.getName());
        return result;
    }

    public static Optional<Path> which(final String command) {
        return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(path -> Paths.get(path, command))
                .filter(Files::exists)
                .filter(Files::isExecutable)
                .findFirst();
    }

    public static Optional<Tuple2<String, String>> splitExtension(final Path file) {
        final String fileName = file.getFileName().toString();
        final int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return Optional.empty();
        } else {
            return Optional.of(
                    Tuple2.newInstance(fileName.substring(0, index), fileName.substring(index)));
        }
    }

    public static <T, U> T dynamicCast(final U obj, final Class<T> cls) {
        if(obj == null) return null;
        else if(cls.isInstance(obj)) {
            return (T) obj;
        } else {
            return null;
        }
    }


    @SneakyThrows
    private static void computeSizeAndCrc32(
            final ZipEntry zipEntry,
            final InputStream inputStream,
            final byte[] buffer) {
        final CRC32 crc32 = new CRC32();
        long sz = 0L;
        while (true) {
            final int read = inputStream.read(buffer);
            if (read < 0) break;
            sz += read;
            crc32.update(buffer, 0, read);
        }
        zipEntry.setSize(sz);
        zipEntry.setCompressedSize(sz);
        zipEntry.setCrc(crc32.getValue());
    }

    @SneakyThrows
    public static void write2Stream(final OutputStream os,
                                    final InputStream inputStream,
                                    final byte[] buffer) {
        while (true) {
            final int read = inputStream.read(buffer);
            if (read < 0) break;
            os.write(buffer, 0, read);
        }
    }

    public static void write2Stream(final OutputStream os,
                                    final InputStream inputStream) {
        write2Stream(os, inputStream, new byte[0x10000]);
    }

    @SneakyThrows
    public static void writeZipEntry(
            final ZipOutputStream zip,
            final Sup<InputStream> source,
            final String destinationFileName,
            final int compressionMethod,
            final byte[] buffer) {
        final ZipEntry zipEntry = new ZipEntry(destinationFileName);
        switch(compressionMethod) {
            case ZipEntry.STORED:
                // A stored ZipEntry requires computing the size and CRC32 in advance
                try(InputStream is = source.get()) {
                    computeSizeAndCrc32(zipEntry, is, buffer);
                }
                break;
            case ZipEntry.DEFLATED:
                break;
            default:
                throw newThrowable(IllegalArgumentException.class,
                        "Unsupported zip entry compression method value: %s", compressionMethod);
        }
        zipEntry.setMethod(compressionMethod);
        zip.putNextEntry(zipEntry);
        try(final InputStream is = source.get()) {
            write2Stream(zip, is, buffer);
        }
        zip.closeEntry();
    }

    public static void writeZipEntry(
            final ZipOutputStream zip,
            final Sup<InputStream> source,
            final String destinationFileName,
            final int compressionMethod) {
        writeZipEntry(zip, source, destinationFileName, compressionMethod, new byte[0x10000]);
    }

    public static void writeZipEntry(
            final ZipOutputStream zip,
            final Sup<InputStream> source,
            final String destinationFileName) {
        writeZipEntry(zip, source, destinationFileName, ZipEntry.DEFLATED);
    }

    @SneakyThrows
    public static void extractZip(final Path sourceArchive, final Path destinationFolder) {
        final byte[] buffer = new byte[0x10000];
        expandZip(sourceArchive, (BiCon<ZipInputStream, ZipEntry>)
                (ZipInputStream zipInputStream, ZipEntry zipEntry) -> {
                    final Path entryPath = destinationFolder.resolve(zipEntry.getName());
            if(zipEntry.isDirectory()) {
                Files.createDirectories(entryPath);
            } else {
                Files.createDirectories(entryPath.getParent());
                try(final OutputStream outputStream = Files.newOutputStream(entryPath)) {
                    copy(zipInputStream, outputStream, buffer);
                }
            }
        });
    }

    @SneakyThrows
    public static void expandZip(final Path sourceArchive, final BiCon<ZipInputStream, ZipEntry> consumer) {
        try(final ZipInputStream zis = new ZipInputStream(Files.newInputStream(sourceArchive))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                consumer.accept(zis, zipEntry);
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    @SneakyThrows
    public static void installResource(final String resourceName, Path destination, final Class<?> cls) {
        Path outputFile;
        if (Files.isSymbolicLink(destination)) {
            destination = destination.toRealPath();
        }
        if(!Files.exists(destination)) {
            Files.createDirectories(destination.getParent());
            outputFile = destination;
        } else if(Files.isDirectory(destination)) {
            outputFile = destination.resolve(resourceName.substring(1 + resourceName.lastIndexOf('/')));
        } else if(Files.isRegularFile(destination)) {
            outputFile = destination;
        } else {
            throw newThrowable(IllegalStateException.class,
                    "Path '%s' is neither a file nor a directory",
                    destination
            );
        }
        InputStream is = cls.getResourceAsStream(resourceName);
        if(is == null) is = cls.getClassLoader().getResourceAsStream(resourceName);
        if(is == null) throw new FileNotFoundException(resourceName);
        try {
            Files.copy(is, outputFile, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            is.close();
        }
    }

    public static <T> Iterator<T> iterator(T[] array) {
        return new Iterator<T>() {
            private int i = 0;
            @Override
            public boolean hasNext() {
                return i < array.length;
            }

            @Override
            public T next() {
                return array[i++];
            }
        };
    }

    @SafeVarargs
    public static <T> Stream<T> optional2Stream(final Optional<T>...opts) {
        return Arrays.stream(opts).filter(Optional::isPresent).map(Optional::get);
    }

    public static <T> Stream<T> optional2Stream(final Iterable<Optional<T>> opts) {
        return iterable2Stream(opts).filter(Optional::isPresent).map(Optional::get);
    }
    public static <T> Stream<T> optional2Stream(final Optional<T> optional) {
        return optional.map(Stream::of).orElse(Stream.empty());
    }

    public static String decapitalize(final String s, final Locale locale) {
        if (!s.isEmpty() && !Character.isLowerCase(s.charAt(0)))
            return s.substring(0, 1).toLowerCase(locale) + s.substring(1);
        else return s;
    }

    public static String decapitalize(final String s) {
        return decapitalize(s, Locale.getDefault());
    }

    public static String capitalize(final String s) {
        return capitalize(s, Locale.getDefault());
    }

    public static String capitalize(final String s, final Locale locale) {
        if (!s.isEmpty()) {
            final char firstChar = s.charAt(0);
            if (Character.isLowerCase(firstChar)) {
                final StringBuilder sb = new StringBuilder();
                final char titleChar = Character.toTitleCase(firstChar);
                if (titleChar != Character.toUpperCase(firstChar)) {
                    sb.append(titleChar);
                } else {
                    sb.append(s.substring(0, 1).toUpperCase(locale));
                }
                sb.append(s.substring(1));
                return  sb.toString();
            }
        }
        return s;
    }

    @SneakyThrows
    public static void copy(final InputStream is, final OutputStream os, final byte[] buffer) {
        while(true) {
            final int read = is.read(buffer);
            if(read < 0) break;
            os.write(buffer, 0, read);
        }
    }

    public static void copy(final InputStream is, final OutputStream os, final int bufferSize) {
        final byte[] buffer = new byte[bufferSize];
        copy(is, os, buffer);
    }

    public static void copy(final InputStream is, final OutputStream os) {
        final byte[] buffer = new byte[0x10000];
        copy(is, os, buffer);
    }

    @SneakyThrows
    public static void copy(final Reader reader, final Writer writer, final char[] buffer) {
        while (true) {
            final int read = reader.read(buffer);
            if (read < 0) break;
            writer.write(buffer, 0, read);
        }
    }

    public static void copy(final Reader reader, final Writer writer, final int bufferSize) {
        final char[] buffer = new char[bufferSize];
        copy(reader, writer, buffer);
    }

    public static void copy(final Reader reader, final Writer writer) {
        final char[] buffer = new char[0x10000];
        copy(reader, writer, buffer);
    }


    public static void waitProcess(
            final List<String> cmd,
            final Path cwd,
            final Map<String, String> env) {
        waitProcess(cmd, cwd, env, 0, null);
    }

    @SneakyThrows
    public static Process startProcess(
            final List<String> cmd,
            final Path cwd,
            final Map<String, String> env) {
        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        pb.inheritIO();
        pb.directory(cwd.toFile());
        pb.environment().putAll(env);
        if (log.isTraceEnabled()) {
            final String cmdLineListString = '[' + cmd.stream().map(s -> '\'' + s + '\'').collect(Collectors.joining(", ")) + ']';
            log.trace("Starting child java process with command line: {}", cmdLineListString);
        }
        return pb.start();
    }

    @SneakyThrows
    public static void waitProcess(
            final List<String> cmd,
            final Path workingDirectory,
            final Map<String, String> env,
            final long tout,
            final TimeUnit tunit) {
        final Process process = startProcess(cmd, workingDirectory, env);
        int rc;
        if (tout > 0) {
            final boolean finished = process.waitFor(tout, tunit);
            if (finished)
                rc = process.exitValue();
            else
                throw newThrowable(TimeoutException.class, "Timeout waiting for process [%s]", "");

        } else {
            rc = process.waitFor();
        }
        if (rc != 0) {
            throw new ChildProcessException(cmd, rc);
        }
    }

    public static <T> Iterator<T> enumeration2Iterator(final Enumeration<T> enumeration) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            @Override
            public T next() {
                return enumeration.nextElement();
            }
        };
    }

    public static <T> Optional<T> or(final Optional<T> ...opts) {
        for(final Optional<T> opt : opts) {
            if(opt.isPresent()) return opt;
        }
        return Optional.empty();
    }

    @SneakyThrows
    public static String sh(final String... cmdLine) {
        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmdLine);
        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
        final Process process = pb.start();
        final int rc = process.waitFor();
        if (rc == 0) {
            try (final Reader reader = new CharFilterReader(new InputStreamReader(process.getInputStream()), '\n')) {
                return readAll(reader);
            }
        } else {
            try (final Reader reader = new InputStreamReader(process.getErrorStream())) {
                throw new RuntimeException(readAll(reader));
            }
        }
    }

    @SneakyThrows
    public static long uid() {
        return Long.parseLong(sh("id", "-u"));
    }

    @SneakyThrows
    public static void replaceFileIfDifferent(final Supplier<InputStream> inputStreamSupplier, final Path destination, final FileAttribute<?>... attrs) {
        Hash hash;
        try (final InputStream inputStream = inputStreamSupplier.get()) {
            hash = Hash.md5(inputStream);
        }
        replaceFileIfDifferent(inputStreamSupplier, destination, hash, attrs);
    }

    public static void replaceFileIfDifferent(final InputStream inputStream, final Path destination, final FileAttribute<?>... attrs) {
        replaceFileIfDifferent(() -> inputStream, destination, null, attrs);
    }

    @SneakyThrows
    private static void replaceFileIfDifferent(
            final Supplier<InputStream> inputStreamSupplier,
            final Path destination,
            Hash newFileHash,
            final FileAttribute<?>... attrs) {
        if (Files.exists(destination)) {
            Hash existingFileHash;
            try (final InputStream existingFileStream = Files.newInputStream(destination)) {
                existingFileHash = Hash.md5(existingFileStream);
            }
            if (newFileHash == null) {
                final MessageDigest md = MessageDigest.getInstance(Hash.Algorithm.MD5.name());
                final Path tmpFile = Files.createTempFile(destination.getParent(), destination.getFileName().toString(), ".tmp", attrs);
                try {
                    try (final InputStream inputStream = inputStreamSupplier.get();
                        final OutputStream outputStream = new DigestOutputStream(Files.newOutputStream(tmpFile), md)) {
                        copy(inputStream, outputStream);
                    }
                    newFileHash = new Hash(Hash.Algorithm.MD5, md.digest());
                    if (!Objects.equals(existingFileHash, newFileHash)) {
                        Files.move(tmpFile, destination, StandardCopyOption.ATOMIC_MOVE);
                    }
                } finally {
                    if (Files.exists(tmpFile)) {
                        Files.delete(tmpFile);
                    }
                }
            } else {
                if (!Objects.equals(existingFileHash, newFileHash)) {
                    final EnumSet<StandardOpenOption> opts = EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                    try (final InputStream inputStream = inputStreamSupplier.get();
                         final OutputStream outputStream = Channels.newOutputStream(Files.newByteChannel(destination, opts, attrs))) {
                        copy(inputStream, outputStream);
                    }
                    if (log.isTraceEnabled()) {
                        log.trace("File '{}' rewritten", destination);
                    }
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("File '{}' unchanged", destination);
                    }
                }
            }
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Creating file '{}'", destination);
            }
            final EnumSet<StandardOpenOption> opts = EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            try (final InputStream inputStream = inputStreamSupplier.get();
                final OutputStream outputStream = Channels.newOutputStream(Files.newByteChannel(destination, opts, attrs))) {
                copy(inputStream, outputStream);
            }
        }
    }

    public static <T, U> Runnable curry(final Consumer<T> original, final Supplier<T> sup) {
        return () -> original.accept(sup.get());
    }

    public static <T> Runnable curry(final Consumer<T> original, final T arg) {
        return () -> original.accept(arg);
    }

    public static <T, U> Supplier<U> curry(final Fun<T, U> original, final T arg) {
        return () -> original.apply(arg);
    }

    public static <T, U, V> Fun<U, V> curry1(final BiFun<T, U, V> original, final T arg) {
        return u -> original.apply(arg, u);
    }

    public static <T, U, V> Fun<T, V> curry2(final BiFun<T, U, V> original, final U arg) {
        return t -> original.apply(t, arg);
    }

    public static <T, U, V> Fun<U, V> curry1(final BiFun<T, U, V> original, final Supplier<T> sup) {
        return u -> original.apply(sup.get(), u);
    }

    public static <T, U, V> Fun<T, V> curry2(final BiFun<T, U, V> original, final Supplier<U> sup) {
        return t -> original.apply(t, sup.get());
    }

    public static <T> Stream<T> lazyValue(final Supplier<T> valueSupplier) {
        return Stream.generate(valueSupplier).limit(1);
    }

    public static <T, U> Supplier<U> compose(final Supplier<T> sup, final Function<? super T, ? extends U> fun) {
        return () -> fun.apply(sup.get());
    }

    public static <T, U, V> Function<T, V> compose(final Function<T, U> fun1, final Function<U, V> fun2) {
        return param -> fun2.apply(fun1.apply(param));
    }

    public static <T, U> Consumer<T> compose(final Function<T, U> fun, final Consumer<U> con) {
        return param -> con.accept(fun.apply(param));
    }

    public static <T, U> Predicate<T> compose(final Function<T, U> fun, final Predicate<U> pred) {
        return param -> pred.test(fun.apply(param));
    }

    public static <T> Supplier<Boolean> compose(final Supplier<T> fun, final Predicate<T> pred) {
        return () -> pred.test(fun.get());
    }

    public static <T> Runnable compose(final Supplier<T> sup, final Consumer<T> con) {
        return () -> con.accept(sup.get());
    }

    public static <T> T loadService(final Class<T> serviceClass) {
        return StreamSupport.stream(ServiceLoader.load(serviceClass).spliterator(), false)
                .findFirst()
                .orElseThrow(
                        () -> newThrowable(
                                ServiceConfigurationError.class,
                                "Unable to find a valid implementation of '%s'",
                                serviceClass.getName()));
    }

    public static <T, U, R> Optional<R> zip(final Optional<T> opt1, final Optional<U> opt2, final BiFun<T, U, R> cb) {
        if (!opt1.isPresent() || !opt2.isPresent()) return Optional.empty();
        else return Optional.ofNullable(cb.apply(opt1.get(), opt2.get()));
    }


    public static Executor delayedExecutor(final long delay, final TimeUnit unit,
                                           final Executor executor) {
        if (unit == null || executor == null)
            throw new NullPointerException();
        return new DelayedExecutor(delay, unit, executor);
    }
    private static final class Delayer {
        private static ScheduledFuture<?> delay(final Runnable command, final long delay,
                                                final TimeUnit unit) {
            return delayer.schedule(command, delay, unit);
        }

        private static final class DaemonThreadFactory implements ThreadFactory {
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("CompletableFutureDelayScheduler");
                return t;
            }
        }

        private static final ScheduledThreadPoolExecutor delayer;
        static {
            (delayer = new ScheduledThreadPoolExecutor(
                1, new DaemonThreadFactory())).
                setRemoveOnCancelPolicy(true);
        }
    }

    private static final class DelayedExecutor implements Executor {
        final long delay;
        final TimeUnit unit;
        final Executor executor;

        private DelayedExecutor(final long delay, final TimeUnit unit, final Executor executor) {
            this.delay = delay; this.unit = unit; this.executor = executor;
        }

        public void execute(final Runnable r) {
            Delayer.delay(new TaskSubmitter(executor, r), delay, unit);
        }
    }

    private static final class TaskSubmitter implements Runnable {
        final Executor executor;
        final Runnable action;

        TaskSubmitter(final Executor executor, final Runnable action) {
            this.executor = executor;
            this.action = action;
        }
        public void run() { executor.execute(action); }
    }

    public static String toUnixPath(final Path path) {
        String result;
        if (OS.isUnix) {
            result = path.toString();
        } else {
            result = (path.isAbsolute() ? "/" : "") +
                iterable2Stream(path)
                    .map(Path::toString)
                    .collect(Collectors.joining("/"));
        }
        return result;
    }

    public static void registerUrlProtocolHandler() {
        final String handlers = System.getProperty(PROTOCOL_HANDLER, "");
        System.setProperty(PROTOCOL_HANDLER,
            ((handlers == null || handlers.isEmpty()) ? HANDLERS_PACKAGE : handlers + "|" + HANDLERS_PACKAGE));
        resetCachedUrlHandlers();
    }

    /**
     * Reset any cached handlers just in case a jar protocol has already been used. We
     * reset the handler by trying to set a null {@link URLStreamHandlerFactory} which
     * should have no effect other than clearing the handlers cache.
     */
    private static void resetCachedUrlHandlers() {
        try {
            URL.setURLStreamHandlerFactory(null);
        } catch (final Error ex) {
            // Ignore
        }
    }

    @SneakyThrows
    public static void deletePath(final Logger log, final Path path) {
        if (Files.exists(path)) {
            if (log.isInfoEnabled()) {
                log.info("Wiping '{}'", path);
            }
            deletePath(path);
        }
    }

    public static <T, U extends T> U downCast(final T param) {
        return (U) param;
    }

    public static <T extends U, U> U upCast(final T param) {
        return param;
    }

    public static <T, U extends T> Optional<U> asInstance(final T param, final Class<U> cls) {
        return Optional.ofNullable(param)
            .filter(cls::isInstance)
            .map(cls::cast);
    }

    public static <K, V, U> Stream<Map.Entry<K, U>> mapValues(final Map<K, V> map, final Fun<V, U> xform) {
        return map
            .entrySet()
            .stream()
            .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), xform.apply(entry.getValue())));
    }

    @SneakyThrows
    public static Process startJavaProcess(
            final Logger log,
            final Path javaHome,
            final List<String> args,
            final Path cwd,
            final Map<String, String> env) {
        final Path javaExecutable = javaHome.resolve("bin/java" + (OS.isWindows ? ".exe" : ""));
        final ProcessBuilder pb = new ProcessBuilder();
        final List<String> cmd = new ArrayList<>();
        cmd.add(javaExecutable.toString());
        cmd.addAll(args);
        pb.command(cmd);
        pb.inheritIO();
        pb.directory(cwd.toFile());
        pb.environment().putAll(env);
        if (log.isTraceEnabled()) {
            final String cmdLineListString = '[' + cmd.stream().map(s -> '\'' + s + '\'').collect(Collectors.joining(", ")) + ']';
            log.trace("Starting child java process with command line: {}", cmdLineListString);
        }
        return pb.start();
    }

    public static <T, U> U let(final T object, final Function<T, U> cb) {
        return cb.apply(object);
    }

    public static <T> T also(final T object, final Consumer<T> cb) {
        cb.accept(object);
        return object;
    }
}
