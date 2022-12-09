package net.woggioni.jwo.internal;

import net.woggioni.jwo.CollectionUtils;
import net.woggioni.jwo.JWO;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class ByteFilterInputStream extends FilterInputStream {

    private boolean finished = false;

    private final Set<Byte> forbidden;

    public ByteFilterInputStream(InputStream source, Iterable<Byte> filteredChars) {
        super(source);
        forbidden = JWO.iterable2Stream(filteredChars).collect(CollectionUtils.toUnmodifiableTreeSet());
    }


    @Override
    public int read() throws IOException {
        while(true) {
            int res = super.read();
            if(!forbidden.contains(res)) {
                return res;
            }
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if(finished) return -1;
        int i = 0;
        int lim = Math.min(len, b.length - off);
        while(i < lim) {
            int c = read();
            if(c < 0) {
                break;
            }
            b[off + i] = (byte) c;
            ++i;
        }
        if(i == 0) finished = true;
        return i == 0 ? -1 : i;
    }
}
