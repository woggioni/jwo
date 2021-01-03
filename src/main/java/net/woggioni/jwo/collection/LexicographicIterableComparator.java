package net.woggioni.jwo.collection;


import java.util.Comparator;
import java.util.Iterator;

public class LexicographicIterableComparator<T> implements Comparator<Iterable<T>> {

    private final Comparator<T> elementComparator;

    public LexicographicIterableComparator(final Comparator<T> elementComparator) {
        this.elementComparator = elementComparator;
    }

    @Override
    public int compare(Iterable<T> iterable1, Iterable<T> iterable2) {
        final Iterator<T> it1 = iterable1.iterator(), it2 = iterable2.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            final int cmp = elementComparator.compare(it1.next(),it2.next());
            if (cmp != 0) return cmp;
        }
        if (it1.hasNext()) return 1;
        if (it2.hasNext()) return -1;
        return 0;
    }

    public static <S extends Comparable<S>> LexicographicIterableComparator<S> forClass(Class<S> cls) {
        return new LexicographicIterableComparator<S>(Comparator.naturalOrder());
    }
}