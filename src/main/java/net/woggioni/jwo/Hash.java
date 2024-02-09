package net.woggioni.jwo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import static net.woggioni.jwo.JWO.newThrowable;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Hash {

    @RequiredArgsConstructor
    public enum Algorithm {
        MD2("MD2"),
        MD5("MD5"),
        SHA1("SHA-1"),
        SHA256("SHA-256"),
        SHA384("SHA-384"),
        SHA512("SHA-512");

        private final String key;

        @SneakyThrows
        public MessageDigest newMessageDigest() {
            return MessageDigest.getInstance(key);
        }
        @SneakyThrows
        public DigestOutputStream newOutputStream(OutputStream delegate) {
            return new DigestOutputStream(delegate, MessageDigest.getInstance(key));
        }
        @SneakyThrows
        public DigestInputStream newInputStream(InputStream delegate) {
            return new DigestInputStream(delegate, MessageDigest.getInstance(key));
        }
    }

    @Getter
    private final Algorithm algorithm;

    @Getter
    private final byte[] bytes;

    @SneakyThrows
    public static Hash hash(Algorithm algo, InputStream is, byte[] buffer) {
        MessageDigest md = MessageDigest.getInstance(algo.key);
        int read;
        while((read = is.read(buffer, 0, buffer.length)) >= 0) {
            md.update(buffer, 0, read);
        }
        return new Hash(algo, md.digest());
    }

    @SneakyThrows
    public static Hash hash(Algorithm algo, InputStream is) {
        return hash(algo, is, new byte[0x1000]);
    }

    @SneakyThrows
    public static Hash md5(InputStream is) {
        return md5(is, new byte[0x1000]);
    }

    @SneakyThrows
    public static Hash md5(InputStream is, byte[] buffer) {
        return hash(Algorithm.MD5, is, buffer);
    }

    public static String md5String(InputStream is) {
        return bytesToHex(md5(is).bytes);
    }

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for(int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hexString) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (hexString.length() % 2 != 0) {
            throw newThrowable(IllegalArgumentException.class, "Hex string length must be even," +
                " string has length '%d' instead", hexString.length());
        }
        int lim = hexString.length() / 2;
        for(int i = 0; i < lim; i++) {
            int tmp = 0;
            for (int j = 0; j < 2; j++) {
                int c = hexString.charAt(i * 2 + j);
                if (c >= '0' && c <= '9') {
                    c -= '0';
                } else if (c >= 'A' && c <= 'F') {
                    c -= 'A' - 10;
                } else if(c >= 'a' && c <= 'f') {
                    c -= 'a' - 10;
                } else {
                    throw newThrowable(IllegalArgumentException.class, "Illegal hex char '%c'", c);
                }
                tmp |= (c << 4 * (1 - j));
            }
            baos.write(tmp);
        }
        return baos.toByteArray();
    }

    @Override
    public String toString() {
        return bytesToHex(bytes);
    }
}