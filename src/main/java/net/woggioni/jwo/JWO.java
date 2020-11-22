package net.woggioni.jwo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.woggioni.jwo.tuple.Tuple2;

import javax.net.ssl.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class JWO {
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
        try (OutputStream os = new FileOutputStream(file.toString())) {
            os.write(bytes);
        }
    }

    @SneakyThrows
    public static String readFile2String(File file) {
        StringBuilder builder = new StringBuilder();
        try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file.getPath())))) {
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

    public static <T> Predicate<T> not(Predicate<T> p) {
        return p.negate();
    }

    public static <V, T> Stream<V> flatMap(Stream<T> stream,
                                           Function<? super T, Optional<? extends V>> mappingFunction) {
        return stream.map(mappingFunction).filter(Optional::isPresent).map(Optional::get);
    }

    public static <T> Stream<T> optional2Stream(Optional<T> optional) {
        return optional.map(Stream::of).orElse(Stream.empty());
    }

    public static void setSystemPropertyIfNotDefined(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
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
        Stream<T> result = Stream.empty();
        for (Stream<T> s : streams) {
            result = Stream.concat(result, s);
        }
        return result;
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
        StringBuilder sb = new StringBuilder();
        Object absent = new Object();

        int cursor = 0;
        while (cursor < template.length()) {
            String key;
            char ch = template.charAt(cursor);
            if (ch != '$' || (cursor > 0 && template.charAt(cursor - 1) == '\\')) {
                sb.append(template.charAt(cursor++));
            } else if (cursor + 1 < template.length() && template.charAt(cursor + 1) == '{') {
                int end = template.indexOf('}', cursor + 1);
                key = template.substring(cursor + 2, end);
                Object value = valuesMap.getOrDefault(key, absent);
                if (value != absent) {
                    sb.append(value.toString());
                } else {
                    raise(MissingFormatArgumentException.class, "Missing value for placeholder '%s'", key);
                }
                cursor = end + 1;
            } else {
                sb.append(template.charAt(cursor++));
            }
        }
        return sb.toString();
    }

    /**
     * @param reader    the reader reading the template text
     * @param valuesMap a map containing the values to replace in the template
     *                  {@link #renderTemplate(String, Map)}
     */
    @SneakyThrows
    public static String renderTemplate(Reader reader, Map<String, Object> valuesMap) {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int read;
        while (!((read = reader.read(buf)) < 0)) {
            sb.append(buf, 0, read);
        }
        return renderTemplate(sb.toString(), valuesMap);
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

    @SneakyThrows
    public static Path computeCacheDirectory(String appName) {
        return Stream.of(
                Optional.ofNullable(System.getProperty("user.home"))
                        .map(prefix -> Paths.get(prefix, ".cache", appName)),
                Optional.ofNullable(System.getProperty("java.io.tmpdir")).map(Paths::get).map(p -> p.resolve(appName)),
                Optional.of(Paths.get("/tmp", appName)))
                .flatMap(JWO::optional2Stream)
                .filter(JWO::validateCacheDirectory)
                .findFirst()
                .orElseThrow(() -> newThrowable(FileNotFoundException.class, "Unable to find a usable cache directory"));
    }

    private static boolean validateCacheDirectory(Path candidate) {
        try {
            if (!Files.exists(candidate)) {
                Files.createDirectories(candidate);
                return true;
            } else if (!Files.isDirectory(candidate)) {
                log.debug("Cache directory '{}' discarded because it is not a directory", candidate.toString());
                return false;
            } else if (!Files.isWritable(candidate)) {
                log.debug("Cache directory '{}' discarded because it is not writable", candidate.toString());
                return false;
            } else {
                log.info("Using cache directory '{}'", candidate.toString());
                return true;
            }
        } catch (Exception ioe) {
            log.debug(String.format("Cache directory '%s' discarded: %s", candidate.toString(), ioe.getMessage()), ioe);
            return false;
        }
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
                    new Tuple2<>(fileName.substring(0, index), fileName.substring(index)));
        }
    }

    public static <T, U> T dynamicCast(U obj, Class<T> cls) {
        if(obj == null) return null;
        else if(cls.isInstance(obj.getClass())) {
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
    public void writeZipEntry(
        ZipOutputStream zip,
        Supplier<InputStream> source,
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

    public void writeZipEntry(
            ZipOutputStream zip,
            Supplier<InputStream> source,
            String destinationFileName,
            int compressionMethod) {
        writeZipEntry(zip, source, destinationFileName, compressionMethod, new byte[0x10000]);
    }

    public void writeZipEntry(
            ZipOutputStream zip,
            Supplier<InputStream> source,
            String destinationFileName) {
        writeZipEntry(zip, source, destinationFileName, ZipEntry.DEFLATED);
    }
}
