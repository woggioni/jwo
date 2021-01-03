package net.woggioni.jwo.collection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;



public class LexicographicIterableComparatorTest {

    private final static Comparator<Integer> DESCENDING_INTEGER_COMPARATOR = new Comparator<Integer>() {
        public int compare(Integer o1, Integer o2) {
            return -o1.compareTo(o2);
        }
    };

    @Test
    public void equal() {
        final List<Integer> l1 = Arrays.asList(1, 2, 3);
        final List<Integer> l2 = Arrays.asList(1, 2, 3);
        final LexicographicIterableComparator<Integer> c = LexicographicIterableComparator.forClass(Integer.class);
        assertEquals(0,c.compare(l1, l2));
    }

    @Test
    public void sameLengthAscending() {
        final List<Integer> l1 = Arrays.asList(1, 2, 3);
        final List<Integer> l2 = Arrays.asList(1, 2, 4);
        final LexicographicIterableComparator<Integer> c = LexicographicIterableComparator.forClass(Integer.class);
        assertEquals(-1,c.compare(l1, l2));
    }

    @Test
    public void sameLengthDescending() {
        final List<Integer> l1 = Arrays.asList(1, 2, 3);
        final List<Integer> l2 = Arrays.asList(1, 2, 4);
        final LexicographicIterableComparator<Integer> c = new LexicographicIterableComparator<>(DESCENDING_INTEGER_COMPARATOR);
        assertEquals(1,c.compare(l1, l2));
    }

    @Test
    public void differentLengthAscending() {
        final List<Integer> l1 = Arrays.asList(1, 2, 3);
        final List<Integer> l2 = Arrays.asList(1, 2);
        final LexicographicIterableComparator<Integer> c = LexicographicIterableComparator.forClass(Integer.class);
        assertEquals(1,c.compare(l1, l2));
    }

    @Test
    public void differentLengthAndValuesDescending() {
        final List<Integer> l1 = Arrays.asList(1, 3, 3); // we need different values, the shorter will always come first otherwise
        final List<Integer> l2 = Arrays.asList(1, 2);
        final LexicographicIterableComparator<Integer> c = new LexicographicIterableComparator<>(DESCENDING_INTEGER_COMPARATOR);
        assertEquals(-1,c.compare(l1, l2));
    }

    @Test
    public void differentLengthDescending() {
        final List<Integer> l1 = Arrays.asList(1, 2, 3);
        final List<Integer> l2 = Arrays.asList(1, 2);
        final LexicographicIterableComparator<Integer> c = new LexicographicIterableComparator<>(DESCENDING_INTEGER_COMPARATOR);
        assertEquals(1,c.compare(l1, l2)); // this is counterintuitive: the shortest comes first event in this case!
    }
}
