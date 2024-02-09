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

    public static <T> Stream<T> iterable2Stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> Stream<T> iterable2ParallelStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), true);
    }

    public static <T> Stream<T> iterator2Stream(Iterator<T> it) {
        return iterable2Stream(() -> it);
    }

    public static <T> Stream<T> iterator2ParallelStream(Iterator<T> it) {
        return iterable2ParallelStream(() -> it);
    }

    @SneakyThrows
    public static void writeObject2File(Path file, Object o) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file.toString()))) {
            writer.write(o.toString());
        }
    }

    public static void writeObject2File(String fileName, Object o) {
        writeObject2File(new File(fileName), o);
    }

    @SneakyThrows
    public static void writeObject2File(File file, Object o) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file.getPath()))) {
            writer.write(o.toString());
        }
    }

    @SneakyThrows
    public static void writeBytes2File(Path file, byte[] bytes) {
        try (OutputStream os = Files.newOutputStream(file)) {
            os.write(bytes);
        }
    }

    @SneakyThrows
    public static String readFile2String(File file) {
        StringWriter writer = new StringWriter();
        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            char[] buffer = new char[1024];
            JWO.copy(reader, writer, buffer);
        }
        return writer.toString();
    }

    @SneakyThrows
    public static String readResource2String(String classpath) {
        StringBuilder builder = new StringBuilder();
        try (Reader reader = new InputStreamReader(JWO.class.getResourceAsStream(classpath))) {
            char[] buffer = new char[1024];
            while (true) {
                int read = reader.read(buffer);
                builder.append(buffer, 0, read);
                if (read < buffer.length) break;
            }
        }
        return builder.toString();
    }

    @SneakyThrows
    public static <T extends Throwable> T newThrowable(Class<T> cls) {
        Constructor<T> constructor = cls.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    @SneakyThrows
    public static <T extends Throwable> T newThrowable(Class<T> cls, String format, Object... args) {
        Constructor<T> constructor = cls.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(String.format(format, args));
    }

    @SneakyThrows
    public static <T extends Throwable> T newThrowable(Class<T> cls, Throwable throwable, String format, Object... args) {
        Constructor<T> constructor = cls.getConstructor(String.class, Throwable.class);
        return constructor.newInstance(String.format(format, args), throwable);
    }

    @SneakyThrows
    public static <T extends Throwable> T raise(Class<T> cls) {
        throw newThrowable(cls);
    }

    @SneakyThrows
    public static <T extends Throwable> void raise(Class<T> cls, Throwable throwable, String format, Object... args) {
        throw newThrowable(cls, throwable, format, args);
    }

    @SneakyThrows
    public static <T extends Throwable> void raise(Class<T> cls, String format, Object... args) {
        throw newThrowable(cls, format, args);
    }

    private static SSLSocketFactory defaultSSLSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    private static HostnameVerifier defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

    @SneakyThrows
    public static void setSSLVerifyPeerHostName(boolean verify) {
        if (verify) {
            HttpsURLConnection.setDefaultHostnameVerifier(defaultHostnameVerifier);
        } else {
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        }
    }

    @SneakyThrows
    public static void setSSLVerifyPeerCertificate(boolean verify) {
        if (verify) {
            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
        } else {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
    }

    public static <T> Predicate<T> not(Pre<T> p) {
        return p.negate();
    }

    public static <V, T> Stream<V> flatMap(Stream<T> stream,
                                           Fun<? super T, Optional<? extends V>> mappingFunction) {
        return stream.map(mappingFunction).filter(Optional::isPresent).map(Optional::get);
    }

    public static void setSystemPropertyIfNotDefined(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        return Hash.bytesToHex(bytes);
    }

    @SneakyThrows
    public static void deletePath(Path path) {
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
    public static <T> T tail(List<T> list, int negativeOffset) {
        return list.get(list.size() + negativeOffset);
    }

    /**
     * @param list the input list
     * @param <T>  the type parameter of the list
     * @return the last element of the input list
     * @throws IndexOutOfBoundsException if the list is empty
     */
    public static <T> T tail(List<T> list) {
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
    public static <T> T pop(List<T> list) {
        return list.remove(list.size() - 1);
    }

    public static <T> Stream<T> streamCat(Stream<T>... streams) {
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
    public static String renderTemplate(String template, Map<String, Object> valuesMap) {
        return renderTemplate(template, valuesMap, null);
    }

    public static int indexOfWithEscape(String haystack, char needle, char escape, int begin, int end) {
        int result = -1;
        int cursor = begin;
        if(end == 0) {
            end = haystack.length();
        }
        int escapeCount = 0;
        while(cursor < end) {
            char c = haystack.charAt(cursor);
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
            String template,
            Map<String, Object> valuesMap,
            Map<String, Map<String, Object>> dictMap) {
        StringBuilder sb = new StringBuilder();
        Object absent = new Object();

        int cursor = 0;
        while(cursor < template.length()) {
            int nextPlaceHolder = indexOfWithEscape(template, '$', '$', cursor, template.length());
            if (nextPlaceHolder < 0) {
                nextPlaceHolder = template.length();
            }
            while (cursor < nextPlaceHolder) {
                char ch = template.charAt(cursor++);
                sb.append(ch);
            }
            if (cursor + 1 < template.length() && template.charAt(cursor + 1) == '{') {
                String key;
                String context = null;
                String defaultValue = null;
                Object value;
                int end = template.indexOf('}', cursor + 1);
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
                    int secondColon = indexOfWithEscape(template, ':', '\\', colon + 1, end);
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
    public static String renderTemplate(Reader reader,
                                        Map<String, Object> valuesMap,
                                        Map<String, Map<String, Object>> dictMap) {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int read;
        while (!((read = reader.read(buf)) < 0)) {
            sb.append(buf, 0, read);
        }
        return renderTemplate(sb.toString(), valuesMap, dictMap);
    }
    public static String renderTemplate(Reader reader,
                                        Map<String, Object> valuesMap) {
        return renderTemplate(reader, valuesMap, null);
    }

    @SneakyThrows
    public static String readAll(Reader reader) {
        char[] buffer = new char[1024];
        StringBuilder sb = new StringBuilder();
        int read;
        while (!((read = reader.read(buffer, 0, buffer.length)) < 0)) {
            sb.append(buffer, 0, read);
        }
        return sb.toString();
    }

    public static <T, U extends T> Optional<U> cast(T value, Class<U> cls) {
        if (cls.isInstance(value)) {
            return Optional.of((U) value);
        } else {
            return Optional.empty();
        }
    }

    public static <T extends Enum> Map<String, T> enumReverseMap(Class<T> cls) {
        return Arrays.stream(cls.getEnumConstants())
                .collect(CollectionUtils.toUnmodifiableTreeMap(Object::toString, Function.identity()));
    }

    public static <T extends Enum> T enumFromString(Class<T> cls, Map<String, T> reverseMap, String value) {
        T result = reverseMap.get(value);
        if (result == null)
            throw newThrowable(IllegalArgumentException.class,
                    "Unknown value '%s' for enum %s", value, cls.getName());
        return result;
    }

    public static Optional<Path> which(String command) {
        return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(path -> Paths.get(path, command))
                .filter(Files::exists)
                .filter(Files::isExecutable)
                .findFirst();
    }

    public static Optional<Tuple2<String, String>> splitExtension(Path file) {
        String fileName = file.getFileName().toString();
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return Optional.empty();
        } else {
            return Optional.of(
                    Tuple2.newInstance(fileName.substring(0, index), fileName.substring(index)));
        }
    }

    public static <T, U> T dynamicCast(U obj, Class<T> cls) {
        if(obj == null) return null;
        else if(cls.isInstance(obj)) {
            return (T) obj;
        } else {
            return null;
        }
    }


    @SneakyThrows
    private static void computeSizeAndCrc32(
            ZipEntry zipEntry,
            InputStream inputStream,
            byte[] buffer) {
        CRC32 crc32 = new CRC32();
        long sz = 0L;
        while (true) {
            int read = inputStream.read(buffer);
            if (read < 0) break;
            sz += read;
            crc32.update(buffer, 0, read);
        }
        zipEntry.setSize(sz);
        zipEntry.setCompressedSize(sz);
        zipEntry.setCrc(crc32.getValue());
    }

    @SneakyThrows
    public static void write2Stream(OutputStream os,
                           InputStream inputStream,
                           byte[] buffer) {
        while (true) {
            int read = inputStream.read(buffer);
            if (read < 0) break;
            os.write(buffer, 0, read);
        }
    }

    public static void write2Stream(OutputStream os,
                                    InputStream inputStream) {
        write2Stream(os, inputStream, new byte[0x10000]);
    }

    @SneakyThrows
    public static void writeZipEntry(
        ZipOutputStream zip,
        Sup<InputStream> source,
        String destinationFileName,
        int compressionMethod,
        byte[] buffer) {
        ZipEntry zipEntry = new ZipEntry(destinationFileName);
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
        try(InputStream is = source.get()) {
            write2Stream(zip, is, buffer);
        }
        zip.closeEntry();
    }

    public static void writeZipEntry(
            ZipOutputStream zip,
            Sup<InputStream> source,
            String destinationFileName,
            int compressionMethod) {
        writeZipEntry(zip, source, destinationFileName, compressionMethod, new byte[0x10000]);
    }

    public static void writeZipEntry(
            ZipOutputStream zip,
            Sup<InputStream> source,
            String destinationFileName) {
        writeZipEntry(zip, source, destinationFileName, ZipEntry.DEFLATED);
    }

    @SneakyThrows
    public static void extractZip(Path sourceArchive, Path destinationFolder) {
        byte[] buffer = new byte[0x10000];
        expandZip(sourceArchive, (BiCon<ZipInputStream, ZipEntry>)
                (ZipInputStream zipInputStream, ZipEntry zipEntry) -> {
            Path entryPath = destinationFolder.resolve(zipEntry.getName());
            if(zipEntry.isDirectory()) {
                Files.createDirectories(entryPath);
            } else {
                Files.createDirectories(entryPath.getParent());
                try(OutputStream outputStream = Files.newOutputStream(entryPath)) {
                    copy(zipInputStream, outputStream, buffer);
                }
            }
        });
    }

    @SneakyThrows
    public static void expandZip(Path sourceArchive, BiCon<ZipInputStream, ZipEntry> consumer) {
        try(ZipInputStream zis = new ZipInputStream(Files.newInputStream(sourceArchive))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                consumer.accept(zis, zipEntry);
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    @SneakyThrows
    public static void installResource(String resourceName, Path destination, Class<?> cls) {
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
    public static <T> Stream<T> optional2Stream(Optional<T>...opts) {
        return Arrays.stream(opts).filter(Optional::isPresent).map(Optional::get);
    }

    public static <T> Stream<T> optional2Stream(Iterable<Optional<T>> opts) {
        return iterable2Stream(opts).filter(Optional::isPresent).map(Optional::get);
    }
    public static <T> Stream<T> optional2Stream(Optional<T> optional) {
        return optional.map(Stream::of).orElse(Stream.empty());
    }

    public static String decapitalize(String s, Locale locale) {
        if (!s.isEmpty() && !Character.isLowerCase(s.charAt(0)))
            return s.substring(0, 1).toLowerCase(locale) + s.substring(1);
        else return s;
    }

    public static String decapitalize(String s) {
        return decapitalize(s, Locale.getDefault());
    }

    public static String capitalize(String s) {
        return capitalize(s, Locale.getDefault());
    }

    public static String capitalize(String s, Locale locale) {
        if (!s.isEmpty()) {
            char firstChar = s.charAt(0);
            if (Character.isLowerCase(firstChar)) {
                StringBuilder sb = new StringBuilder();
                char titleChar = Character.toTitleCase(firstChar);
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
    public static void copy(InputStream is, OutputStream os, byte[] buffer) {
        while(true) {
            int read = is.read(buffer);
            if(read < 0) break;
            os.write(buffer, 0, read);
        }
    }

    public static void copy(InputStream is, OutputStream os, int bufferSize) {
        byte[] buffer = new byte[bufferSize];
        copy(is, os, buffer);
    }

    public static void copy(InputStream is, OutputStream os) {
        byte[] buffer = new byte[0x10000];
        copy(is, os, buffer);
    }

    @SneakyThrows
    public static void copy(Reader reader, Writer writer, char[] buffer) {
        while (true) {
            int read = reader.read(buffer);
            if (read < 0) break;
            writer.write(buffer, 0, read);
        }
    }

    public static void copy(Reader reader, Writer writer, int bufferSize) {
        char[] buffer = new char[bufferSize];
        copy(reader, writer, buffer);
    }

    public static void copy(Reader reader, Writer writer) {
        char[] buffer = new char[0x10000];
        copy(reader, writer, buffer);
    }


    public static void waitProcess(
            List<String> cmd,
            Path cwd,
            Map<String, String> env) {
        waitProcess(cmd, cwd, env, 0, null);
    }

    @SneakyThrows
    public static Process startProcess(
            List<String> cmd,
            Path cwd, Map<String, String> env) {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        pb.inheritIO();
        pb.directory(cwd.toFile());
        pb.environment().putAll(env);
        if (log.isTraceEnabled()) {
            String cmdLineListString = '[' + cmd.stream().map(s -> '\'' + s + '\'').collect(Collectors.joining(", ")) + ']';
            log.trace("Starting child java process with command line: {}", cmdLineListString);
        }
        return pb.start();
    }

    @SneakyThrows
    public static void waitProcess(
            List<String> cmd,
            Path workingDirectory,
            Map<String, String> env,
            long tout,
            TimeUnit tunit) {
        Process process = startProcess(cmd, workingDirectory, env);
        int rc;
        if (tout > 0) {
            boolean finished = process.waitFor(tout, tunit);
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

    public static <T> Iterator<T> enumeration2Iterator(Enumeration<T> enumeration) {
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

    public static <T> Optional<T> or(Optional<T> ...opts) {
        for(Optional<T> opt : opts) {
            if(opt.isPresent()) return opt;
        }
        return Optional.empty();
    }

    @SneakyThrows
    public static String sh(String... cmdLine) {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmdLine);
        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
        Process process = pb.start();
        int rc = process.waitFor();
        if (rc == 0) {
            try (Reader reader = new CharFilterReader(new InputStreamReader(process.getInputStream()), '\n')) {
                return readAll(reader);
            }
        } else {
            try (Reader reader = new InputStreamReader(process.getErrorStream())) {
                throw new RuntimeException(readAll(reader));
            }
        }
    }

    @SneakyThrows
    public static long uid() {
        return Long.parseLong(sh("id", "-u"));
    }

    @SneakyThrows
    public static void replaceFileIfDifferent(Supplier<InputStream> inputStreamSupplier, Path destination, FileAttribute<?>... attrs) {
        Hash hash;
        try (InputStream inputStream = inputStreamSupplier.get()) {
            hash = Hash.md5(inputStream);
        }
        replaceFileIfDifferent(inputStreamSupplier, destination, hash, attrs);
    }

    public static void replaceFileIfDifferent(InputStream inputStream, Path destination, FileAttribute<?>... attrs) {
        replaceFileIfDifferent(() -> inputStream, destination, null, attrs);
    }

    @SneakyThrows
    private static void replaceFileIfDifferent(
            Supplier<InputStream> inputStreamSupplier,
            Path destination,
            Hash newFileHash,
            FileAttribute<?>... attrs) {
        if (Files.exists(destination)) {
            Hash existingFileHash;
            try (InputStream existingFileStream = Files.newInputStream(destination)) {
                existingFileHash = Hash.md5(existingFileStream);
            }
            if (newFileHash == null) {
                MessageDigest md = MessageDigest.getInstance(Hash.Algorithm.MD5.name());
                Path tmpFile = Files.createTempFile(destination.getParent(), destination.getFileName().toString(), ".tmp", attrs);
                try {
                    try (InputStream inputStream = inputStreamSupplier.get();
                         OutputStream outputStream = new DigestOutputStream(Files.newOutputStream(tmpFile), md)) {
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
                    EnumSet<StandardOpenOption> opts = EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                    try (InputStream inputStream = inputStreamSupplier.get();
                         OutputStream outputStream = Channels.newOutputStream(Files.newByteChannel(destination, opts, attrs))) {
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
            EnumSet<StandardOpenOption> opts = EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            try (InputStream inputStream = inputStreamSupplier.get();
                 OutputStream outputStream = Channels.newOutputStream(Files.newByteChannel(destination, opts, attrs))) {
                copy(inputStream, outputStream);
            }
        }
    }

    public static <T, U> Runnable curry(Consumer<T> original, Supplier<T> sup) {
        return () -> original.accept(sup.get());
    }

    public static <T> Runnable curry(Consumer<T> original, T arg) {
        return () -> original.accept(arg);
    }

    public static <T, U> Supplier<U> curry(Fun<T, U> original, T arg) {
        return () -> original.apply(arg);
    }

    public static <T, U, V> Fun<U, V> curry1(BiFun<T, U, V> original, T arg) {
        return u -> original.apply(arg, u);
    }

    public static <T, U, V> Fun<T, V> curry2(BiFun<T, U, V> original, U arg) {
        return t -> original.apply(t, arg);
    }

    public static <T, U, V> Fun<U, V> curry1(BiFun<T, U, V> original, Supplier<T> sup) {
        return u -> original.apply(sup.get(), u);
    }

    public static <T, U, V> Fun<T, V> curry2(BiFun<T, U, V> original, Supplier<U> sup) {
        return t -> original.apply(t, sup.get());
    }

    public static <T> Stream<T> lazyValue(Supplier<T> valueSupplier) {
        return Stream.generate(valueSupplier).limit(1);
    }

    public static <T, U> Supplier<U> compose(Supplier<T> sup, Function<? super T, ? extends U> fun) {
        return () -> fun.apply(sup.get());
    }

    public static <T, U, V> Function<T, V> compose(Function<T, U> fun1, Function<U, V> fun2) {
        return param -> fun2.apply(fun1.apply(param));
    }

    public static <T, U> Consumer<T> compose(Function<T, U> fun, Consumer<U> con) {
        return param -> con.accept(fun.apply(param));
    }

    public static <T, U> Predicate<T> compose(Function<T, U> fun, Predicate<U> pred) {
        return param -> pred.test(fun.apply(param));
    }

    public static <T> Supplier<Boolean> compose(Supplier<T> fun, Predicate<T> pred) {
        return () -> pred.test(fun.get());
    }

    public static <T> Runnable compose(Supplier<T> sup, Consumer<T> con) {
        return () -> con.accept(sup.get());
    }

    public static <T> T loadService(Class<T> serviceClass) {
        return StreamSupport.stream(ServiceLoader.load(serviceClass).spliterator(), false)
                .findFirst()
                .orElseThrow(
                        () -> newThrowable(
                                ServiceConfigurationError.class,
                                "Unable to find a valid implementation of '%s'",
                                serviceClass.getName()));
    }

    public static <T, U, R> Optional<R> zip(Optional<T> opt1, Optional<U> opt2, BiFun<T, U, R> cb) {
        if (!opt1.isPresent() || !opt2.isPresent()) return Optional.empty();
        else return Optional.ofNullable(cb.apply(opt1.get(), opt2.get()));
    }


    public static Executor delayedExecutor(long delay, TimeUnit unit,
                                            Executor executor) {
        if (unit == null || executor == null)
            throw new NullPointerException();
        return new DelayedExecutor(delay, unit, executor);
    }
    private static final class Delayer {
        private static ScheduledFuture<?> delay(Runnable command, long delay,
                                                TimeUnit unit) {
            return delayer.schedule(command, delay, unit);
        }

        private static final class DaemonThreadFactory implements ThreadFactory {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
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
        private DelayedExecutor(long delay, TimeUnit unit, Executor executor) {
            this.delay = delay; this.unit = unit; this.executor = executor;
        }
        public void execute(Runnable r) {
            Delayer.delay(new TaskSubmitter(executor, r), delay, unit);
        }
    }

    private static final class TaskSubmitter implements Runnable {
        final Executor executor;
        final Runnable action;
        TaskSubmitter(Executor executor, Runnable action) {
            this.executor = executor;
            this.action = action;
        }
        public void run() { executor.execute(action); }
    }

    public static String toUnixPath(Path path) {
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
        String handlers = System.getProperty(PROTOCOL_HANDLER, "");
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
        } catch (Error ex) {
            // Ignore
        }
    }

    @SneakyThrows
    public static void deletePath(Logger log, Path path) {
        if (Files.exists(path)) {
            if (log.isInfoEnabled()) {
                log.info("Wiping '{}'", path);
            }
            deletePath(path);
        }
    }

    public static <T, U extends T> U downCast(T param) {
        return (U) param;
    }

    public static <T extends U, U> U upCast(T param) {
        return param;
    }

    public static <T, U extends T> Optional<U> asInstance(T param, Class<U> cls) {
        return Optional.ofNullable(param)
            .filter(cls::isInstance)
            .map(cls::cast);
    }

    public static <K, V, U> Stream<Map.Entry<K, U>> mapValues(Map<K, V> map, Fun<V, U> xform) {
        return map
            .entrySet()
            .stream()
            .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), xform.apply(entry.getValue())));
    }

    @SneakyThrows
    public static Process startJavaProcess(
        Logger log,
        Path javaHome,
        List<String> args,
        Path cwd,
        Map<String, String> env) {
        Path javaExecutable = javaHome.resolve("bin/java" + (OS.isWindows ? ".exe" : ""));
        ProcessBuilder pb = new ProcessBuilder();
        List<String> cmd = new ArrayList<>();
        cmd.add(javaExecutable.toString());
        cmd.addAll(args);
        pb.command(cmd);
        pb.inheritIO();
        pb.directory(cwd.toFile());
        pb.environment().putAll(env);
        if (log.isTraceEnabled()) {
            String cmdLineListString = '[' + cmd.stream().map(s -> '\'' + s + '\'').collect(Collectors.joining(", ")) + ']';
            log.trace("Starting child java process with command line: {}", cmdLineListString);
        }
        return pb.start();
    }

    public static <T, U> U let(T object, Function<T, U> cb) {
        return cb.apply(object);
    }

    public static <T> T also(T object, Consumer<T> cb) {
        cb.accept(object);
        return object;
    }
}
