package net.woggioni.jmath;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.woggioni.jwo.Requirement.require;

public class Matrix<T extends NumericType<T>> implements Iterable<Matrix.Element<T>> {

    public interface Pivot {
        <U extends NumericType<U>> Matrix<U> mul(Matrix<U> m);
    }

    @RequiredArgsConstructor
    private static class PivotImpl implements Pivot {
        @Getter
        private final int[] values;

        @Getter
        @Setter
        private int permutations = 0;

        @Override
        public <U extends NumericType<U>> Matrix<U> mul(Matrix<U> m) {
            return Matrix.of(m.numericTypeFactory, m.getRows(), m.getColumns(), (i, j) -> m.get(values[i], j));
        }
    }

    public interface ValueGenerator<T extends NumericType<T>> {
        T generate(int row, int column);
    }

    private final NumericTypeFactory<T> numericTypeFactory;
    private final T[] values;
    private final int rows;
    private final int columns;

    @SneakyThrows
    public Matrix(int rows, int columns, NumericTypeFactory<T> numericTypeFactory) {
        this.numericTypeFactory = numericTypeFactory;
        this.rows = rows;
        this.columns = columns;
        this.values = numericTypeFactory.getArray(rows * columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                set(i, j, numericTypeFactory.getZero());
            }
        }
    }

    public static <T extends NumericType<T>> Matrix<T> of(
            NumericTypeFactory<T> numericTypeFactory,
            int rows,
            int columns,
            ValueGenerator<T> generator) {
        Matrix<T> result = new Matrix<>(rows, columns, numericTypeFactory);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result.set(i, j, generator.generate(i, j));
            }
        }
        return result;
    }

    public static <T extends NumericType<T>> Matrix<T> of(
            NumericTypeFactory<T> numericTypeFactory,
            int rows,
            int columns) {
        return of(numericTypeFactory, rows, columns, (i, j) -> numericTypeFactory.getZero());
    }

    public static <T extends NumericType<T>> Matrix<T> of(NumericTypeFactory<T> numericTypeFactory, T[][] values) {
        int rows = values.length;
        int columns = values[0].length;
        return of(numericTypeFactory, rows, columns, (i, j) -> values[i][j]);
    }

    public static <T extends NumericType<T>> Matrix<T> of(
            NumericTypeFactory<T> numericTypeFactory, int rows, int columns, T... values) {
        return of(numericTypeFactory, rows, columns, (i, j) -> values[i * columns + j]);
    }

    public T get(int row, int column) {
        return values[row * columns + column];
    }

    public void set(int row, int column, T value) {
        values[row * columns + column] = value;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    private void requireSameSize(Matrix<T> other) {
        require(() -> getRows() == other.getRows() && getColumns() == other.getColumns())
                .otherwise(SizeException.class, "Matrix dimension mismatch: (%d, %d) vs (%d, %d)",
                        getRows(), getColumns(), other.getRows(), other.getColumns()
                );
    }

    public Matrix<T> map(Matrix<T> other, BiFunction<T, T, T> op) {
        requireSameSize(other);
        return of(numericTypeFactory, getRows(), getColumns(), (i, j) -> op.apply(get(i, j), other.get(i, j)));
    }

    public <U extends NumericType<U>> Matrix<U> map(NumericTypeFactory<U> numericTypeFactory, Function<T, U> op) {
        return of(numericTypeFactory, getRows(), getColumns(), (i, j) -> op.apply(get(i, j)));
    }

    public Matrix<T> map(Function<T, T> op) {
        return of(numericTypeFactory, getRows(), getColumns(), (i, j) -> op.apply(get(i, j)));
    }

    public Matrix<T> add(Matrix<T> other) {
        return map(other, T::add);
    }

    public Matrix<T> sub(Matrix<T> other) {
        return map(other, T::sub);
    }

    public Matrix<T> mul(Matrix<T> other) {
        return map(other, T::mul);
    }

    public Matrix<T> div(Matrix<T> other) {
        return map(other, T::div);
    }

    public Matrix<T> add(T value) {
        return map(numericTypeFactory, (T v) -> v.add(value));
    }

    public Matrix<T> sub(T value) {
        return map(numericTypeFactory, (T v) -> v.sub(value));
    }

    public Matrix<T> mul(T value) {
        return map(numericTypeFactory, (T v) -> v.mul(value));
    }

    public Matrix<T> div(T value) {
        return map(numericTypeFactory, (T v) -> v.div(value));
    }


    public Vector<T> solve(Vector<T> b) {
        Matrix<T> tmp = clone();
        Pivot pivot = tmp.lup();
        return tmp.luSolve(b, pivot);
    }

    private void swapRows(int id1, int id2) {
        for (int i = 0; i < getColumns(); i++) {
            T tmp = get(id1, i);
            set(id1, i, get(id2, i));
            set(id2, i, tmp);
        }
    }

    private void swapRows(int id1, int id2, PivotImpl pivot) {
        swapRows(id1, id2);
        int tmp = pivot.getValues()[id1];
        pivot.getValues()[id1] = pivot.getValues()[id2];
        pivot.getValues()[id2] = tmp;
        pivot.permutations += 1;
    }

    private void swapRows(int id1, int id2, PivotImpl pivot, Matrix<T> other) {
        swapRows(id1, id2, pivot);
        other.swapRows(id1, id2);
    }

    private void luRow(int i) {
        if (Objects.equals(numericTypeFactory.getZero(), get(i, i))) {
            throw new RuntimeException("Matrix is singular");
        }
        for (int j = i; j < getColumns(); j++) {
            for (int k = 0; k < i; k++) {
                set(i, j, get(i, j).sub(get(i, k).mul(get(k, j))));
            }
        }
        for (int j = i + 1; j < getColumns(); j++) {
            for (int k = 0; k < i; k++) {
                set(j, i, get(j, i).sub(get(j, k).mul(get(k, i))));
            }
            set(j, i, get(j, i).div(get(i, i)));
        }
    }

    private void luPivot(int i, Pivot pivot) {
        T max = get(i, i).abs();
        int max_index = i;
        for (int j = i + 1; j < getRows(); j++) {
            if (get(j, i).abs().compareTo(max) > 0) {
                max = get(i, j).abs();
                max_index = j;
            }
        }
        if (max_index != i) {
            swapRows(i, max_index, (PivotImpl) pivot);
        }
    }

    public Pivot lup() {
        if (getRows() != getColumns()) throw new SizeException("Matrix must be square");
        int size = getRows();
        PivotImpl pivot = newPivot();
        for (int i = 0; i < size; i++) {
            luPivot(i, pivot);
            luRow(i);
        }
        return pivot;
    }

    private PivotImpl newPivot() {
        int sz = getRows();
        int[] result = new int[sz];
        for (int i = 0; i < sz; i++) {
            result[i] = i;
        }
        return new PivotImpl(result);
    }

    private void addRow(int sourceIndex, int destIndex, T factor) {
        int columns = getColumns();
        for (int i = 0; i < columns; i++) {
            set(destIndex, i, get(destIndex, i).add(get(sourceIndex, i).mul(factor)));
        }
    }

    private void addRow(int sourceIndex, int destIndex, T factor, Matrix<T> other) {
        addRow(sourceIndex, destIndex, factor);
        other.addRow(sourceIndex, destIndex, factor);
    }

    public void gaussJordanLow() {
        PivotImpl pivot = newPivot();
        int rows = getRows();
        int columns = getColumns();
        for (int i = 0; i < rows; i++) {
            if (Objects.equals(numericTypeFactory.getZero(), get(i, i))) {
                for (int j = i + 1; j < columns; j++) {
                    if (!Objects.equals(numericTypeFactory.getZero(), get(j, i))) {
                        swapRows(i, j, pivot);
                        break;
                    }
                }
            }
            for (int j = i + 1; j < rows; j++) {
                T ii = get(i, i);
                if (!Objects.equals(numericTypeFactory.getZero(), ii)) {
                    T factor = get(j, i).div(ii).mul(numericTypeFactory.getMinusOne());
                    addRow(i, j, factor);
                }
            }
        }
    }

    private void gaussJordanLow(Matrix<T> other) {
        PivotImpl pivot = newPivot();
        int rows = getRows();
        int columns = getColumns();
        for (int i = 0; i < rows; i++) {
            if (Objects.equals(numericTypeFactory.getZero(), get(i, i))) {
                for (int j = i + 1; j < columns; j++) {
                    if (!Objects.equals(numericTypeFactory.getZero(), get(j, i))) {
                        swapRows(i, j, pivot, other);
                        break;
                    }
                }
            }
            for (int j = i + 1; j < rows; j++) {
                T ii = get(i, i);
                if (!Objects.equals(numericTypeFactory.getZero(), ii)) {
                    T factor = get(j, i).div(ii).mul(numericTypeFactory.getMinusOne());
                    addRow(i, j, factor, other);
                }
            }
        }
    }

    private void gaussJordanHigh() {
        PivotImpl pivot = newPivot();
        int i = getRows();
        while (i-- > 0) {
            if (Objects.equals(numericTypeFactory.getZero(), get(i, i))) {
                int j = i;
                while (j-- > 0) {
                    if (!Objects.equals(numericTypeFactory.getZero(), get(j, i))) {
                        swapRows(i, j, pivot);
                        break;
                    }
                }
            }
            int j = i;
            while (j-- > 0) {
                T ii = get(i, i);
                if (!Objects.equals(numericTypeFactory.getZero(), ii)) {
                    T factor = get(j, i).div(ii).mul(numericTypeFactory.getMinusOne());
                    addRow(i, j, factor);
                }
            }
        }
    }

    private void gaussJordanHigh(Matrix<T> other) {
        PivotImpl pivot = newPivot();
        int i = getRows();
        while (i-- > 0) {
            if (Objects.equals(numericTypeFactory.getZero(), get(i, i))) {
                int j = i;
                while (j-- > 0) {
                    if (!Objects.equals(numericTypeFactory.getZero(), get(j, i))) {
                        swapRows(i, j, pivot, other);
                        break;
                    }
                }
            }
            int j = i;
            while (j-- > 0) {
                T ii = get(i, i);
                if (!Objects.equals(numericTypeFactory.getZero(), ii)) {
                    T factor = get(j, i).div(ii).mul(numericTypeFactory.getMinusOne());
                    addRow(i, j, factor, other);
                }
            }
        }
    }

    public T det() {
        require(() -> getRows() == getColumns()).otherwise(SizeException.class, "Matrix must be square");
        Matrix<T> clone = clone();
        clone.gaussJordanLow();
        T result = numericTypeFactory.getOne();
        for (int i = 0; i < getRows(); i++)
            result = result.mul(clone.get(i, i));
        return result;
    }

    public Matrix<T> invert() {
        require(() -> getRows() == getColumns()).otherwise(SizeException.class, "Matrix must be square");
        int sz = getRows();
        int col = getColumns();
        Matrix<T> tmp = clone();
        Matrix<T> result = identity(numericTypeFactory, sz);
        tmp.gaussJordanLow(result);
        tmp.gaussJordanHigh(result);
        for (int i = 0; i < sz; i++) {
            T f = tmp.get(i, i);
            for (int j = 0; j < col; j++) {
                result.set(i, j, result.get(i, j).div(f));
            }
        }
        return result;
    }

    public Matrix<T> triu() {
        return of(numericTypeFactory, getRows(), getColumns(), (i, j) ->
                i <= j ? get(i, j) : numericTypeFactory.getZero()
        );
    }

    public Matrix<T> triu(T diagValue) {
        return of(numericTypeFactory, getRows(), getColumns(), (i, j) -> {
            T result;
            if (i < j) {
                result = get(i, j);
            } else if (i == j) {
                result = diagValue;
            } else {
                result = numericTypeFactory.getZero();
            }
            return result;
        });
    }


    public Matrix<T> tril() {
        return of(numericTypeFactory, getRows(), getColumns(), (i, j) ->
                i >= j ? get(i, j) : numericTypeFactory.getZero()
        );
    }

    public Matrix<T> tril(T diagValue) {
        return of(numericTypeFactory, getRows(), getColumns(), (i, j) -> {
            T result;
            if (i > j) {
                result = get(i, j);
            } else if (i == j) {
                result = diagValue;
            } else {
                result = numericTypeFactory.getZero();
            }
            return result;
        });
    }


    @SneakyThrows
    public Vector<T> luSolve(Vector<T> b, Pivot pivot) {
        Objects.requireNonNull(b);
        PivotImpl pivotImpl = (PivotImpl) pivot;
        int[] pivotValues = pivotImpl.getValues();
        int size = getRows();
        if (pivotValues.length != size) throw new SizeException(
                String.format("Pivot length is %d must be %d instead", pivotValues.length, size));

        for (int i = 0; i < pivotValues.length; i++) pivotValues[i] = i;
        Vector<T> x = Vector.of(numericTypeFactory, size);

        for (int i = 0; i < size; i++) {
            x.set(i, b.get(pivotValues[i]));
            for (int k = 0; k < i; k++) {
                x.set(i, x.get(i).sub(get(i, k).mul(x.get(k))));
            }
        }
        int i = size;
        while (i-- > 0) {
            for (int k = i + 1; k < size; k++) {
                x.set(i, x.get(i).sub(get(i, k).mul(x.get(k))));
            }
            if (!Objects.equals(get(i, i), numericTypeFactory.getZero())) {
                x.set(i, x.get(i).div(get(i, i)));
            } else throw new SingularMatrixException("Matrix is singular");
        }
        return x;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < getRows(); i++) {
            sb.append('[');
            for (int j = 0; j < getRows(); j++) {
                if (j > 0) sb.append(", ");
                sb.append(get(i, j));
            }
            sb.append(']');
            sb.append('\n');
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public Matrix<T> clone() {
        return of(numericTypeFactory, getRows(), getColumns(), this::get);
    }

    public static <T extends NumericType<T>> Matrix<T> identity(NumericTypeFactory<T> numericTypeFactory, int size) {
        return of(numericTypeFactory, size, size, (i, j) -> {
            T result;
            if (i == j) result = numericTypeFactory.getOne();
            else result = numericTypeFactory.getZero();
            return result;
        });
    }

    public T luDet() {
        require(() -> getRows() == getColumns()).otherwise(SizeException.class, "Matrix must be square");
        Matrix<T> clone = clone();
        PivotImpl pivot = (PivotImpl) clone.lup();
        T result = numericTypeFactory.getOne();
        for (int i = 0; i < rows; i++) {
            result = result.mul(clone.get(i, i));
        }
        if (pivot.permutations % 2 != 0) {
            result = result.mul(numericTypeFactory.getMinusOne());
        }
        return result;
    }

    public Matrix<T> transpose() {
        return Matrix.of(numericTypeFactory, getColumns(), getRows(), (i, j) -> get(j, i));
    }

    public Matrix<T> mmul(Matrix<T> m2) {
        return Matrix.of(numericTypeFactory, getRows(), m2.getColumns(), (i, j) -> {
            T result = numericTypeFactory.getZero();
            for (int k = 0; k < getColumns(); k++) {
                result = result.add(get(i, k).mul(m2.get(k, j)));
            }
            return result;
        });
    }

    public Vector<T> mmul(Vector<T> v) {
        return Vector.of(numericTypeFactory, getRows(), i -> {
            int columns = getColumns();
            T result = numericTypeFactory.getZero();
            for (int j = 0; j < columns; j++) {
                result = result.add(get(i, j).mul(v.get(j)));
            }
            return result;
        });
    }
//    public T luDet() {
//        if(getRows() != getColumns()) {
//            throw newThrowable(
//                    SizeException.class,
//                    "Matrix must be square in order to compute the determinant");
//        }
//        int[] pivot = lup();
//        T result = numericTypeFactory.getOne();
//        for(int i=0; i<getRows(); i++) {
//            result = result.mul(get(i, i));
//        if pivot.permutations mod 2 != 0:
//        result *= -1
//    }
//
//    proc lu_det*[T](m : HMatrix[T]) : T =
//            if m.rows != m.columns:
//    raise newException(SizeError, "Matrix must be square in order to compute the determinant")
//    var clone = m.clone()
//    lu_det(clone)

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Matrix<? extends NumericType<?>> other))
            return false;
        if (getRows() != other.getRows() || getColumns() != other.getColumns()) {
            return false;
        }
        int r = getRows();
        int c = getColumns();
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (!Objects.equals(get(i, j), other.get(i, j))) return false;
            }
        }
        return true;
    }

    public T squaredNorm2() {
        T result = numericTypeFactory.getZero();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                T value = get(i, j);
                result = result.add(value.mul(value));
            }
        }
        return result;
    }

    public T norm2() {
        return squaredNorm2().sqrt();
    }

    @Override
    public Iterator<Element<T>> iterator() {
        return new Iterator<>() {
            int i = 0;
            int j = 0;

            @Override
            public boolean hasNext() {
                return i * getColumns() + j < getRows() * getColumns();
            }

            @Override
            public Element<T> next() {
                Element<T> result = new Element<>(i, j, get(i, j));
                ++j;
                if (j == getColumns()) {
                    j = 0;
                    ++i;
                }
                return result;
            }
        };
    }

    @Data
    public static class Element<T> {
        private final int row;
        private final int column;
        private final T value;
    }
}
