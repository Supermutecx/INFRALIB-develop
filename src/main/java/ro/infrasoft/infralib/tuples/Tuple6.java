package ro.infrasoft.infralib.tuples;

/**
 * Tuplu 6.
 *
 * @param <U>
 * @param <V>
 * @param <K>
 * @param <L>
 * @param <M>
 * @param <N>
 */
public class Tuple6<U, V, K, L, M, N> {
    private U item1;
    private V item2;
    private K item3;
    private L item4;
    private M item5;
    private N item6;

    public Tuple6(U item1, V item2, K item3, L item4, M item5, N item6) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.item4 = item4;
        this.item5 = item5;
        this.item6 = item6;
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

    public M getItem5() {
        return item5;
    }

    public void setItem5(M item5) {
        this.item5 = item5;
    }

    public N getItem6() {
        return item6;
    }

    public void setItem6(N item6) {
        this.item6 = item6;
    }
}
