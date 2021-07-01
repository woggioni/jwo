package net.woggioni.jwo.hash;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;

@RequiredArgsConstructor
public class Hash {

    @RequiredArgsConstructor
    enum Algorithm {
        MD2("MD2"),
        MD5("MD5"),
        SHA1("SHA-1"),
        SHA256("SHA-256"),
        SHA384("SHA-384"),
        SHA512("SHA-512");

        private final String key;
    }

    final Algorithm algorithm;
    final byte[] bytes;

    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        else if(getClass() != other.getClass()) return false;
        Hash otherHash = (Hash) other;
        if(algorithm != otherHash.algorithm) return false;
        return Arrays.equals(bytes, otherHash.bytes);
    }

    @Override
    public int hashCode() {
        int result = algorithm.hashCode();
        for(byte b : bytes) {
            result ^= b;
        }
        return result;
    }

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
}