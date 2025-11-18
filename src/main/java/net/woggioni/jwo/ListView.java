package net.woggioni.jwo;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import static java.lang.Math.min;

@RequiredArgsConstructor
public class ListView<T> implements List<T> {
    private final List<T> delegate;
    private final int start;
    private final int end;

    public ListView(final List<T> delegate, final int start) {
        this(delegate, start, -1);
    }

    @Override
    public int size() {
        return end < 0 ? delegate.size() : min(end, delegate.size()) - start;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(final Object o) {
        final Iterator<T> it = iterator();
        while (it.hasNext()) {
            if(Objects.equals(o, it.next())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int index = start;
            @Override
            public boolean hasNext() {
                return end < 0 ? index < size() : index < min(end, size());
            }

            @Override
            public T next() {
                return get(index++);
            }
        };
    }

    @Override
    public Object[] toArray() {
        final int size = size();
        final Object[] result = new Object[size];
        for(int i = 0; i < size; i++) {
            result[i] = get(i);
        }
        return result;
    }

    @Override
    public <T1> T1[] toArray(final T1[] t1s) {
        final int size = size();
        final T1[] result = Arrays.copyOf(t1s, size);
        for(int i = 0; i < size; i++) {
            result[i] = (T1) get(i);
        }
        return result;
    }

    @Override
    public boolean add(final T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(final Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(final Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final int i, final Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(final int i) {
        final int index = start + i;
        if(end >= 0 && index < end) {
            throw new IndexOutOfBoundsException(Integer.toString(i));
        }
        return delegate.get(start + i);
    }

    @Override
    public T set(final int i, final T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(final int i, final T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(final int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(final Object o) {
        final int size = size();
        for(int i = 0; i < size; i++) {
            if(Objects.equals(o, get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(final Object o) {
        final int size = size();
        for(int i = size - 1; i >= 0; i--) {
            if(Objects.equals(o, get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return new ListViewIterator<>(this, 0);
    }

    @Override
    public ListIterator<T> listIterator(final int i) {
        if(i < 0 || i > size()) {
            throw new IndexOutOfBoundsException(Integer.toString(0));
        } else {
            return new ListViewIterator<>(this, start);
        }
    }

    @Override
    public List<T> subList(final int i, final int i1) {
        if(i < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(0));
        } else if(i1 > size()) {
            throw new IndexOutOfBoundsException(Integer.toString(i1));
        } else {
            return new ListView<>(delegate, start + i, start + i1);
        }
    }

    @RequiredArgsConstructor
    private static class ListViewIterator<T> implements ListIterator<T> {
        private final ListView<T> listView;
        int size;
        int i;

        public ListViewIterator(final ListView<T> listView, final int start) {
            this.listView = listView;
            size = listView.size();
            i = start;
        }

        @Override
        public boolean hasNext() {
            return i < size;
        }

        @Override
        public T next() {
            return listView.get(i++);
        }

        @Override
        public boolean hasPrevious() {
            return i > 0;
        }

        @Override
        public T previous() {
            return listView.get(--i);
        }

        @Override
        public int nextIndex() {
            return  i + 1;
        }

        @Override
        public int previousIndex() {
            return i;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(final T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(final T t) {
            throw new UnsupportedOperationException();
        }
    }
}
