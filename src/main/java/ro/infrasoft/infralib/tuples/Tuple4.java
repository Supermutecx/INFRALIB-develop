package ro.infrasoft.infralib.tuples;

/**
 * Tuplu 4.
 *
 * @param <U>
 * @param <V>
 * @param <K>
 * @param <L>
 */
public class Tuple4<U, V, K, L> {
    private U item1;
    private V item2;
    private K item3;
    private L item4;

    public Tuple4(U item1, V item2, K item3, L item4) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.item4 = item4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) o;

        if (item1 != null ? !item1.equals(tuple4.item1) : tuple4.item1 != null) return false;
        if (item2 != null ? !item2.equals(tuple4.item2) : tuple4.item2 != null) return false;
        if (item3 != null ? !item3.equals(tuple4.item3) : tuple4.item3 != null) return false;
        if (item4 != null ? !item4.equals(tuple4.item4) : tuple4.item4 != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = item1 != null ? item1.hashCode() : 0;
        result = 31 * result + (item2 != null ? item2.hashCode() : 0);
        result = 31 * result + (item3 != null ? item3.hashCode() : 0);
        result = 31 * result + (item4 != null ? item4.hashCode() : 0);
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

    public L getItem4() {
        return item4;
    }

    public void setItem4(L item4) {
        this.item4 = item4;
    }
}
