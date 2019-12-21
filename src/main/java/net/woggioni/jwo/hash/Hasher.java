package net.woggioni.jwo.hash;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.security.MessageDigest;

public class Hasher {

    private Hasher() {}

    @SneakyThrows
    public static byte[] md5(InputStream is) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[1024];
        int read;
        while((read = is.read(buffer, 0, buffer.length)) >= 0) {
            md.update(buffer, 0, read);
        }
        return md.digest();
    }

    public static String md5String(InputStream is) {
        return bytesToHex(md5(is));
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