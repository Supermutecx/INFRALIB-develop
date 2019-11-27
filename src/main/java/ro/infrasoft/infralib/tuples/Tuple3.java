package ro.infrasoft.infralib.tuples;

/**
 * Tuplu 3.
 *
 * @param <U>
 * @param <V>
 * @param <K>
 */
public class Tuple3<U, V, K> {
    private U item1;
    private V item2;
    private K item3;

    public Tuple3(U item1, V item2, K item3) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>) o;

        if (item1 != null ? !item1.equals(tuple3.item1) : tuple3.item1 != null) return false;
        if (item2 != null ? !item2.equals(tuple3.item2) : tuple3.item2 != null) return false;
        if (item3 != null ? !item3.equals(tuple3.item3) : tuple3.item3 != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = item1 != null ? item1.hashCode() : 0;
        result = 31 * result + (item2 != null ? item2.hashCode() : 0);
        result = 31 * result + (item3 != null ? item3.hashCode() : 0);
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

    public K getItem3() {
        return item3;
    }

    public void setItem3(K item3) {
        this.item3 = item3;
    }
}
