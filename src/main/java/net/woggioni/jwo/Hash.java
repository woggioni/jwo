package net.woggioni.jwo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.security.MessageDigest;

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

    @Override
    public String toString() {
        return bytesToHex(bytes);
    }
}