package net.woggioni.jwo.internal;

import net.woggioni.jwo.CollectionUtils;
import net.woggioni.jwo.JWO;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Set;

public class CharFilterReader extends FilterReader {

    private boolean finished = false;

    private final Set<Character> forbidden;

    public CharFilterReader(final Reader source, final Iterable<Character> filteredChars) {
        super(source);
        forbidden = JWO.iterable2Stream(filteredChars).collect(CollectionUtils.toUnmodifiableTreeSet());
    }

    public CharFilterReader(final Reader source, final Character ...filteredChars) {
        super(source);
        forbidden = Arrays.stream(filteredChars).collect(CollectionUtils.toUnmodifiableTreeSet());
    }


    @Override
    public int read() throws IOException {
        while(true) {
            final int res = super.read();
            if(!forbidden.contains((char) res)) {
                return res;
            }
        }
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        if(finished) return -1;
        int i = 0;
        final int lim = Math.min(len, cbuf.length - off);
        while(i < lim) {
            final int c = read();
            if(c < 0) {
                break;
            }
            cbuf[off + i] = (char) c;
            ++i;
        }
        if(i == 0) finished = true;
        return i == 0 ? -1 : i;
    }

    @Override
    public int read(final CharBuffer target) throws IOException {
        return read(target.array());
    }

    @Override
    public int read(final char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }
}
