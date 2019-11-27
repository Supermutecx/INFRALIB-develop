package ro.infrasoft.infralib.tuples;

/**
 * Tuplu 2.
 *
 * @param <U>
 * @param <V>
 */
public class Tuple2<U, V> {
    private U item1;
    private V item2;

    public Tuple2(U item1, V item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;

        if (item1 != null ? !item1.equals(tuple2.item1) : tuple2.item1 != null) return false;
        return !(item2 != null ? !item2.equals(tuple2.item2) : tuple2.item2 != null);
    }

    @Override
    public int hashCode() {
        int result = item1 != null ? item1.hashCode() : 0;
        result = 31 * result + (item2 != null ? item2.hashCode() : 0);
        return result;
    }

    public U getItem1() {
        return item1;
    }

    public void setItem1(U item1) {
        this.item1 = item1;
    }

    public V getItem2() {
        return item2;
    }

    public void setItem2(V item2) {
        this.item2 = item2;
    }
}
